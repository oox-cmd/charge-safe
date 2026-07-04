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
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class ChargingServiceImpl implements ChargingService {

    private final ChargingReservationMapper reservationMapper;
    private final ChargingOrderMapper orderMapper;
    private final ChargingPortMapper portMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final NotificationMapper notificationMapper;

    private static final AtomicLong ORDER_SEQ = new AtomicLong(1);

    @Override
    @Transactional
    public ChargingReservation createReservation(ReservationDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 检查用户是否已有有效预约
        Long activeCount = reservationMapper.selectCount(
                new LambdaQueryWrapper<ChargingReservation>()
                        .eq(ChargingReservation::getUserId, userId)
                        .eq(ChargingReservation::getStatus, ReservationStatus.PENDING.getCode())
        );
        if (activeCount > 0) {
            throw new BusinessException(ResultCode.RESERVATION_ALREADY_EXIST);
        }

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

        // 获取端口所属社区ID
        ChargingDevice device = deviceMapper.selectById(port.getDeviceId());
        Long communityId = null;
        if (device != null) {
            ParkingArea parkingArea = parkingAreaMapper.selectById(device.getParkingAreaId());
            if (parkingArea != null) {
                communityId = parkingArea.getCommunityId();
            }
        }

        // 创建预约
        ChargingReservation reservation = new ChargingReservation();
        reservation.setUserId(userId);
        reservation.setPortId(dto.getPortId());
        reservation.setCommunityId(communityId);
        reservation.setReserveTime(LocalDateTime.now());
        reservation.setExpireTime(LocalDateTime.now().plusMinutes(30)); // 30分钟有效期
        reservation.setStatus(ReservationStatus.PENDING.getCode());
        reservationMapper.insert(reservation);

        // 更新端口状态为已预约
        port.setStatus(PortStatus.RESERVED.getCode());
        portMapper.updateById(port);

        // 发送预约成功通知
        createNotification(userId, "预约成功通知",
                "您已成功预约" + port.getPortName() + "，请在30分钟内到达扫码充电。",
                "RESERVATION", "RESERVATION", reservation.getId());

        return reservation;
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Long userId = UserContext.getUserId();
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

        // 更新预约状态
        reservation.setStatus(ReservationStatus.CANCELLED.getCode());
        reservation.setCancelReason("用户主动取消");
        reservationMapper.updateById(reservation);

        // 恢复端口状态为空闲
        ChargingPort port = portMapper.selectById(reservation.getPortId());
        if (port != null && PortStatus.RESERVED.getCode().equals(port.getStatus())) {
            port.setStatus(PortStatus.IDLE.getCode());
            portMapper.updateById(port);
        }

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

        // 更新预约状态为过期
        reservation.setStatus(ReservationStatus.EXPIRED.getCode());
        reservationMapper.updateById(reservation);

        // 恢复端口状态为空闲
        ChargingPort port = portMapper.selectById(reservation.getPortId());
        if (port != null && PortStatus.RESERVED.getCode().equals(port.getStatus())) {
            port.setStatus(PortStatus.IDLE.getCode());
            portMapper.updateById(port);
        }

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

        // 更新预约状态为设备故障取消
        reservation.setStatus(ReservationStatus.FAULT_CANCELLED.getCode());
        reservation.setCancelReason(reason != null ? reason : "设备故障自动取消");
        reservationMapper.updateById(reservation);

        // 恢复端口状态为故障
        ChargingPort port = portMapper.selectById(reservation.getPortId());
        if (port != null) {
            port.setStatus(PortStatus.FAULT.getCode());
            portMapper.updateById(port);
        }

        // 发送故障取消通知
        createNotification(reservation.getUserId(), "预约故障取消通知",
                "因设备故障，您的预约已被自动取消。",
                "RESERVATION", "RESERVATION", reservationId);
    }

    @Override
    public ChargingReservation getMyActiveReservation() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return null;
        }
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
        Page<ChargingReservation> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ChargingReservation> wrapper = new LambdaQueryWrapper<ChargingReservation>()
                .eq(userId != null, ChargingReservation::getUserId, userId)
                .eq(StringUtils.hasText(status), ChargingReservation::getStatus, status)
                .orderByDesc(ChargingReservation::getCreatedTime);

        Page<ChargingReservation> result = reservationMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    @Transactional
    public ChargingOrder startCharging(Long reservationId) {
        Long userId = UserContext.getUserId();
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

        // 检查预约是否过期
        if (LocalDateTime.now().isAfter(reservation.getExpireTime())) {
            reservation.setStatus(ReservationStatus.EXPIRED.getCode());
            reservationMapper.updateById(reservation);
            throw new BusinessException(ResultCode.RESERVATION_EXPIRED);
        }

        // 检查端口状态
        ChargingPort port = portMapper.selectById(reservation.getPortId());
        if (port == null || !PortStatus.RESERVED.getCode().equals(port.getStatus())) {
            throw new BusinessException(ResultCode.PORT_NOT_IDLE);
        }

        // 更新预约状态为已转订单
        reservation.setStatus(ReservationStatus.CONVERTED.getCode());
        reservationMapper.updateById(reservation);

        // 更新端口状态为充电中
        port.setStatus(PortStatus.CHARGING.getCode());
        portMapper.updateById(port);

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
        orderMapper.insert(order);

        return order;
    }

    @Override
    @Transactional
    public ChargingOrder startChargingDirectly(Long portId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 检查端口状态
        ChargingPort port = portMapper.selectById(portId);
        if (port == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!PortStatus.IDLE.getCode().equals(port.getStatus())) {
            throw new BusinessException(ResultCode.PORT_NOT_IDLE.getCode(), "端口当前不可用，请先预约");
        }

        // 获取社区ID
        ChargingDevice device = deviceMapper.selectById(port.getDeviceId());
        Long communityId = null;
        if (device != null) {
            ParkingArea parkingArea = parkingAreaMapper.selectById(device.getParkingAreaId());
            if (parkingArea != null) {
                communityId = parkingArea.getCommunityId();
            }
        }

        // 更新端口状态为充电中
        port.setStatus(PortStatus.CHARGING.getCode());
        portMapper.updateById(port);

        // 创建充电订单（无预约关联）
        ChargingOrder order = new ChargingOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPortId(portId);
        order.setCommunityId(communityId);
        order.setStartTime(LocalDateTime.now());
        order.setAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.CHARGING.getCode());
        orderMapper.insert(order);

        return order;
    }

    @Override
    @Transactional
    public ChargingOrder finishCharging(Long orderId) {
        Long userId = UserContext.getUserId();
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

        // 更新订单
        order.setEndTime(now);
        order.setDurationMinutes((int) minutes);
        order.setAmount(amount);
        order.setStatus(OrderStatus.COMPLETED.getCode());
        orderMapper.updateById(order);

        // 恢复端口状态为空闲
        ChargingPort port = portMapper.selectById(order.getPortId());
        if (port != null) {
            port.setStatus(PortStatus.IDLE.getCode());
            portMapper.updateById(port);
        }

        // 发送充电完成通知
        createNotification(userId, "充电完成通知",
                "您的充电已完成，时长" + minutes + "分钟，费用" + amount + "元。",
                "CHARGING", "CHARGING_ORDER", orderId);

        return order;
    }

    @Override
    public PageResult<ChargingOrder> pageOrders(Long userId, String status, PageQuery pageQuery) {
        Page<ChargingOrder> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ChargingOrder> wrapper = new LambdaQueryWrapper<ChargingOrder>()
                .eq(userId != null, ChargingOrder::getUserId, userId)
                .eq(StringUtils.hasText(status), ChargingOrder::getStatus, status)
                .orderByDesc(ChargingOrder::getCreatedTime);

        Page<ChargingOrder> result = orderMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public ChargingOrder getOrderById(Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
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
        notificationMapper.insert(notification);
    }
}
