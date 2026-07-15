package com.chargesafe.modules.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.charging.dto.ReservationDTO;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import com.chargesafe.modules.charging.entity.ChargingReservation;
import com.chargesafe.modules.charging.enums.OrderStatus;
import com.chargesafe.modules.charging.enums.ReservationStatus;
import com.chargesafe.modules.charging.exception.ReservationExpiredException;
import com.chargesafe.modules.charging.mapper.ChargingOrderMapper;
import com.chargesafe.modules.charging.mapper.ChargingReservationMapper;
import com.chargesafe.modules.charging.service.ChargingService;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.security.DataScope;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargingServiceImpl implements ChargingService {

    private final ChargingReservationMapper reservationMapper;
    private final ChargingOrderMapper orderMapper;
    private final ChargingPortMapper portMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final NotificationMapper notificationMapper;
    private final SysUserMapper userMapper;

    private static final AtomicLong ORDER_SEQ = new AtomicLong(1);

    @Override
    @Transactional
    public ChargingReservation createReservation(ReservationDTO dto) {
        Long userId = requireResidentUser();
        lockCurrentUser(userId);

        Long activeCount = reservationMapper.selectCount(
                new LambdaQueryWrapper<ChargingReservation>()
                        .eq(ChargingReservation::getUserId, userId)
                        .eq(ChargingReservation::getStatus, ReservationStatus.PENDING.getCode())
        );
        if (activeCount > 0) {
            throw new BusinessException(ResultCode.RESERVATION_ALREADY_EXIST);
        }

        ensureNoActiveChargingOrder(userId);

        // 检查端口是否已有有效预约（同一端口不能同时被预约）
        Long portActiveCount = reservationMapper.selectCount(
                new LambdaQueryWrapper<ChargingReservation>()
                        .eq(ChargingReservation::getPortId, dto.getPortId())
                        .eq(ChargingReservation::getStatus, ReservationStatus.PENDING.getCode())
        );
        if (portActiveCount > 0) {
            throw new BusinessException(ResultCode.PORT_NOT_IDLE.getCode(), "该端口已有有效预约");
        }

        // 检查端口状态
        ChargingPort port = portMapper.selectById(dto.getPortId());
        if (port == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!PortStatus.IDLE.getCode().equals(port.getStatus())) {
            throw new BusinessException(ResultCode.PORT_NOT_IDLE);
        }

        Long communityId = resolvePortCommunity(port);
        ensureCurrentCommunity(communityId);

        transitionPort(dto.getPortId(), PortStatus.IDLE, PortStatus.RESERVED);

        // 创建预约
        ChargingReservation reservation = new ChargingReservation();
        reservation.setUserId(userId);
        reservation.setPortId(dto.getPortId());
        reservation.setCommunityId(communityId);
        reservation.setReserveTime(LocalDateTime.now());
        reservation.setExpireTime(LocalDateTime.now().plusMinutes(30)); // 30分钟有效期
        reservation.setStatus(ReservationStatus.PENDING.getCode());
        requireWrite(reservationMapper.insert(reservation), "创建预约失败");

        // 发送预约成功通知
        createNotification(userId, "预约成功通知",
                "您已成功预约" + port.getPortName() + "，请在30分钟内到达扫码充电。",
                "RESERVATION", "RESERVATION", reservation.getId());

        return reservation;
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Long userId = requireResidentUser();
        lockCurrentUser(userId);
        ChargingReservation reservation = reservationMapper.selectById(reservationId);

        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!ReservationStatus.PENDING.getCode().equals(reservation.getStatus())) {
            throw new BusinessException("预约状态不允许取消");
        }
        ensureCurrentCommunity(reservation.getCommunityId());

        transitionReservation(reservationId, ReservationStatus.PENDING,
                ReservationStatus.CANCELLED, "用户主动取消");
        transitionPort(reservation.getPortId(), PortStatus.RESERVED, PortStatus.IDLE);

        // 发送取消通知
        createNotification(userId, "预约取消通知",
                "您已成功取消预约。",
                "RESERVATION", "RESERVATION", reservationId);
    }

    @Override
    @Transactional
    public void expireReservation(Long reservationId) {
        ChargingReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (!ReservationStatus.PENDING.getCode().equals(reservation.getStatus())) {
            return; // 已处理过，无需重复操作
        }
        ensureSystemOrScopedCommunity(reservation.getCommunityId());

        transitionReservation(reservationId, ReservationStatus.PENDING,
                ReservationStatus.EXPIRED, "预约超时");
        transitionPort(reservation.getPortId(), PortStatus.RESERVED, PortStatus.IDLE);

        // 发送过期通知
        createNotification(reservation.getUserId(), "预约过期通知",
                "您的预约已超时自动取消。",
                "RESERVATION", "RESERVATION", reservationId);
    }

    @Override
    @Transactional
    public void faultCancelReservation(Long reservationId, String reason) {
        ChargingReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (!ReservationStatus.PENDING.getCode().equals(reservation.getStatus())) {
            return; // 已处理过
        }
        ensureSystemOrScopedCommunity(reservation.getCommunityId());

        transitionReservation(reservationId, ReservationStatus.PENDING,
                ReservationStatus.FAULT_CANCELLED,
                reason != null ? reason : "设备故障自动取消");
        transitionPort(reservation.getPortId(), PortStatus.RESERVED, PortStatus.FAULT);

        // 发送故障取消通知
        createNotification(reservation.getUserId(), "预约故障取消通知",
                "因设备故障，您的预约已被自动取消。",
                "RESERVATION", "RESERVATION", reservationId);
    }

    @Override
    public ChargingReservation getMyActiveReservation() {
        Long userId = requireResidentUser();
        return reservationMapper.selectOne(
                new LambdaQueryWrapper<ChargingReservation>()
                        .eq(ChargingReservation::getUserId, userId)
                        .eq(ChargingReservation::getStatus, ReservationStatus.PENDING.getCode())
                        .orderByDesc(ChargingReservation::getCreatedTime)
                        .last("LIMIT 1")
        );
    }

    @Override
    public PageResult<ChargingReservation> pageReservations(Long userId, String status, PageQuery pageQuery) {
        Long effectiveUserId = DataScope.isResident() ? requireResidentUser() : userId;
        Long communityId = scopedCommunityId();
        Page<ChargingReservation> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ChargingReservation> wrapper = new LambdaQueryWrapper<ChargingReservation>()
                .eq(effectiveUserId != null, ChargingReservation::getUserId, effectiveUserId)
                .eq(communityId != null, ChargingReservation::getCommunityId, communityId)
                .eq(StringUtils.hasText(status), ChargingReservation::getStatus, status)
                .orderByDesc(ChargingReservation::getCreatedTime);

        Page<ChargingReservation> result = reservationMapper.selectPage(page, wrapper);

        // Fill userName and portName
        Set<Long> userIds = result.getRecords().stream().map(ChargingReservation::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> portIds = result.getRecords().stream().map(ChargingReservation::getPortId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> userNameMap = userIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));
        Map<Long, String> portNameMap = portIds.isEmpty() ? Map.of() : portMapper.selectBatchIds(portIds).stream().collect(Collectors.toMap(ChargingPort::getId, ChargingPort::getPortName));
        result.getRecords().forEach(r -> {
            r.setUserName(userNameMap.get(r.getUserId()));
            r.setPortName(portNameMap.get(r.getPortId()));
        });

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public PageResult<ChargingReservation> pageMyReservations(String status, PageQuery pageQuery) {
        return pageReservations(requireResidentUser(), status, pageQuery);
    }

    @Override
    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public ChargingOrder startCharging(Long reservationId) {
        Long userId = requireResidentUser();
        lockCurrentUser(userId);
        ChargingReservation reservation = reservationMapper.selectById(reservationId);

        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!ReservationStatus.PENDING.getCode().equals(reservation.getStatus())) {
            throw new BusinessException("预约状态不允许开始充电");
        }
        ensureCurrentCommunity(reservation.getCommunityId());
        ensureNoActiveChargingOrder(userId);

        if (LocalDateTime.now().isAfter(reservation.getExpireTime())) {
            transitionReservation(reservationId, ReservationStatus.PENDING,
                    ReservationStatus.EXPIRED, "预约超时");
            transitionPort(reservation.getPortId(), PortStatus.RESERVED, PortStatus.IDLE);
            createNotification(userId, "预约过期通知", "您的预约已超时自动取消。",
                    "RESERVATION", "RESERVATION", reservationId);
            throw new ReservationExpiredException();
        }

        // 检查端口状态
        ChargingPort port = portMapper.selectById(reservation.getPortId());
        if (port == null || !PortStatus.RESERVED.getCode().equals(port.getStatus())) {
            throw new BusinessException(ResultCode.PORT_NOT_IDLE);
        }

        transitionReservation(reservationId, ReservationStatus.PENDING,
                ReservationStatus.CONVERTED, null);
        transitionPort(reservation.getPortId(), PortStatus.RESERVED, PortStatus.CHARGING);

        // 创建充电订单
        ChargingOrder order = new ChargingOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPortId(reservation.getPortId());
        order.setReservationId(reservationId);
        order.setCommunityId(reservation.getCommunityId());
        order.setStartTime(LocalDateTime.now());
        order.setAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.CHARGING.getCode());
        requireWrite(orderMapper.insert(order), "创建充电订单失败");

        return order;
    }

    @Override
    @Transactional
    public ChargingOrder startChargingDirectly(Long portId) {
        Long userId = requireResidentUser();
        lockCurrentUser(userId);

        Long reservationCount = reservationMapper.selectCount(
                new LambdaQueryWrapper<ChargingReservation>()
                        .eq(ChargingReservation::getUserId, userId)
                        .eq(ChargingReservation::getStatus, ReservationStatus.PENDING.getCode()));
        if (reservationCount > 0) {
            throw new BusinessException(ResultCode.RESERVATION_ALREADY_EXIST);
        }
        ensureNoActiveChargingOrder(userId);

        // 检查端口状态
        ChargingPort port = portMapper.selectById(portId);
        if (port == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!PortStatus.IDLE.getCode().equals(port.getStatus())) {
            throw new BusinessException(ResultCode.PORT_NOT_IDLE.getCode(), "端口当前不可用，请先预约");
        }

        Long communityId = resolvePortCommunity(port);
        ensureCurrentCommunity(communityId);

        transitionPort(portId, PortStatus.IDLE, PortStatus.CHARGING);

        // 创建充电订单（无预约关联）
        ChargingOrder order = new ChargingOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPortId(portId);
        order.setCommunityId(communityId);
        order.setStartTime(LocalDateTime.now());
        order.setAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.CHARGING.getCode());
        requireWrite(orderMapper.insert(order), "创建充电订单失败");

        return order;
    }

    @Override
    @Transactional
    public ChargingOrder finishCharging(Long orderId) {
        Long userId = requireResidentUser();
        lockCurrentUser(userId);
        ChargingOrder order = orderMapper.selectById(orderId);

        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!OrderStatus.CHARGING.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.CHARGING_ALREADY_FINISHED);
        }
        ensureCurrentCommunity(order.getCommunityId());

        ChargingPort port = portMapper.selectById(order.getPortId());
        if (port == null || !PortStatus.CHARGING.getCode().equals(port.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "订单与端口状态不一致");
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(order.getStartTime(), now).toMinutes();
        if (minutes < 1) minutes = 1; // 最少1分钟

        // 计算费用（简化计算：1.5元/小时）
        BigDecimal hourlyRate = new BigDecimal("1.50");
        BigDecimal hours = new BigDecimal(minutes).divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
        BigDecimal amount = hours.multiply(hourlyRate).setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(new BigDecimal("0.50")) < 0) {
            amount = new BigDecimal("0.50"); // 最低消费
        }

        order.setEndTime(now);
        order.setDurationMinutes((int) minutes);
        order.setAmount(amount);
        order.setStatus(OrderStatus.COMPLETED.getCode());
        requireWrite(orderMapper.finishOrder(orderId, OrderStatus.CHARGING.getCode(),
                OrderStatus.COMPLETED.getCode(), now, (int) minutes, amount), "结束充电失败");
        transitionPort(order.getPortId(), PortStatus.CHARGING, PortStatus.IDLE);

        // 发送充电完成通知
        createNotification(userId, "充电完成通知",
                "您的充电已完成，时长" + minutes + "分钟，费用" + amount + "元。",
                "CHARGING", "CHARGING_ORDER", orderId);

        return order;
    }

    @Override
    public PageResult<ChargingOrder> pageOrders(Long userId, String status, PageQuery pageQuery) {
        Long effectiveUserId = DataScope.isResident() ? requireResidentUser() : userId;
        Long communityId = scopedCommunityId();
        Page<ChargingOrder> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ChargingOrder> wrapper = new LambdaQueryWrapper<ChargingOrder>()
                .eq(effectiveUserId != null, ChargingOrder::getUserId, effectiveUserId)
                .eq(communityId != null, ChargingOrder::getCommunityId, communityId)
                .eq(StringUtils.hasText(status), ChargingOrder::getStatus, status)
                .orderByDesc(ChargingOrder::getCreatedTime);

        Page<ChargingOrder> result = orderMapper.selectPage(page, wrapper);

        // Fill userName, portName, portCode
        Set<Long> userIds = result.getRecords().stream().map(ChargingOrder::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> portIds = result.getRecords().stream().map(ChargingOrder::getPortId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> userNameMap = userIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));
        Map<Long, ChargingPort> portMap = portIds.isEmpty() ? Map.of() : portMapper.selectBatchIds(portIds).stream().collect(Collectors.toMap(ChargingPort::getId, p -> p));
        result.getRecords().forEach(o -> {
            o.setUserName(userNameMap.get(o.getUserId()));
            ChargingPort port = portMap.get(o.getPortId());
            if (port != null) {
                o.setPortName(port.getPortName());
                o.setPortCode(port.getPortName());
            }
        });

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public PageResult<ChargingOrder> pageMyOrders(String status, PageQuery pageQuery) {
        return pageOrders(requireResidentUser(), status, pageQuery);
    }

    @Override
    public ChargingOrder getOrderById(Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (DataScope.isResident()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!DataScope.isPlatformAdmin() && !Objects.equals(order.getCommunityId(), scopedCommunityId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return order;
    }

    @Override
    public ChargingOrder getMyOrderById(Long orderId) {
        Long userId = requireResidentUser();
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(order.getUserId(), userId)
                || !Objects.equals(order.getCommunityId(), UserContext.getCommunityId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return order;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = ORDER_SEQ.getAndIncrement() % 10000;
        return "ORD" + timestamp + String.format("%04d", seq);
    }

    private void createNotification(Long userId, String title, String content,
                                    String type, String businessType, Long businessId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notification.setIsRead(0);
        requireWrite(notificationMapper.insert(notification), "创建通知失败");
    }

    private Long requireResidentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (!DataScope.isResident()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return userId;
    }

    private void lockCurrentUser(Long userId) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .last("FOR UPDATE"));
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    private void ensureNoActiveChargingOrder(Long userId) {
        Long activeOrders = orderMapper.selectCount(new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getUserId, userId)
                .eq(ChargingOrder::getStatus, OrderStatus.CHARGING.getCode()));
        if (activeOrders > 0) {
            throw new BusinessException(ResultCode.CHARGING_ALREADY_ACTIVE);
        }
    }

    private Long resolvePortCommunity(ChargingPort port) {
        ChargingDevice device = deviceMapper.selectById(port.getDeviceId());
        if (device == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        ParkingArea parkingArea = parkingAreaMapper.selectById(device.getParkingAreaId());
        if (parkingArea == null || parkingArea.getCommunityId() == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return parkingArea.getCommunityId();
    }

    private void ensureCurrentCommunity(Long communityId) {
        if (communityId == null || !Objects.equals(communityId, UserContext.getCommunityId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private void ensureSystemOrScopedCommunity(Long communityId) {
        if (UserContext.getUserId() == null || DataScope.isPlatformAdmin()) {
            return;
        }
        if (!Objects.equals(communityId, UserContext.getCommunityId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private Long scopedCommunityId() {
        if (DataScope.isPlatformAdmin()) {
            return null;
        }
        Long communityId = UserContext.getCommunityId();
        if (communityId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return communityId;
    }

    private void transitionPort(Long portId, PortStatus expected, PortStatus target) {
        requireWrite(portMapper.transitionStatus(portId, expected.getCode(), target.getCode()),
                "端口状态已变化，请刷新后重试");
    }

    private void transitionReservation(Long reservationId, ReservationStatus expected,
                                       ReservationStatus target, String cancelReason) {
        requireWrite(reservationMapper.transitionStatus(reservationId, expected.getCode(),
                target.getCode(), cancelReason), "预约状态已变化，请刷新后重试");
    }

    private void requireWrite(int affectedRows, String message) {
        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), message);
        }
    }
}
