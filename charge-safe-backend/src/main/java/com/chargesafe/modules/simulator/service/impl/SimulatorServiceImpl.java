package com.chargesafe.modules.simulator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.notification.entity.AlarmRecord;
import com.chargesafe.modules.notification.mapper.AlarmRecordMapper;
import com.chargesafe.modules.repair.entity.RepairReport;
import com.chargesafe.modules.repair.enums.ReportStatus;
import com.chargesafe.modules.repair.mapper.RepairReportMapper;
import com.chargesafe.modules.simulator.service.SimulatorService;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import com.chargesafe.modules.charging.enums.OrderStatus;
import com.chargesafe.modules.charging.mapper.ChargingOrderMapper;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.user.entity.SysRole;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.entity.SysUserRole;
import com.chargesafe.modules.user.mapper.SysRoleMapper;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.mapper.SysUserRoleMapper;
import com.chargesafe.security.DataScope;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulatorServiceImpl implements SimulatorService {

    private final ChargingPortMapper portMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final RepairReportMapper reportMapper;
    private final NotificationMapper notificationMapper;
    private final ChargingOrderMapper orderMapper;
    private final AlarmRecordMapper alarmRecordMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public ChargingPort simulatePortStatus(Long portId, String status) {
        ChargingPort port = portMapper.selectById(portId);
        if (port == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 验证状态值是否合法
        try {
            PortStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的端口状态: " + status);
        }

        port.setStatus(status);
        portMapper.updateById(port);
        return port;
    }

    @Override
    @Transactional
    public ChargingPort simulatePortFault(Long portId) {
        ChargingPort port = portMapper.selectById(portId);
        if (port == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        ChargingDevice device = deviceMapper.selectById(port.getDeviceId());
        if (device == null) {
            throw new BusinessException("端口未关联有效设备，无法模拟故障");
        }
        ParkingArea parkingArea = parkingAreaMapper.selectById(device.getParkingAreaId());
        if (parkingArea == null || parkingArea.getCommunityId() == null) {
            throw new BusinessException("设备未关联有效车棚或社区，无法模拟故障");
        }
        Long communityId = parkingArea.getCommunityId();
        Long actorId = UserContext.getUserId();
        if (actorId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 所有依赖均通过后才改变端口状态，避免发生半完成模拟操作。
        port.setStatus(PortStatus.FAULT.getCode());
        portMapper.updateById(port);

        // 当前有权限的模拟操作者作为报修记录发起人，避免伪造固定系统用户。
        RepairReport report = new RepairReport();
        report.setReporterId(actorId);
        report.setCommunityId(communityId);
        report.setDeviceId(port.getDeviceId());
        report.setPortId(portId);
        report.setFaultType("SYSTEM_ALARM");
        report.setFaultDesc("系统检测到端口故障，自动生成报修记录。");
        report.setStatus(ReportStatus.PENDING.getCode());
        reportMapper.insert(report);

        createNotificationsForProperty(communityId, "设备故障模拟告警",
                "模拟器将端口 " + port.getPortName() + " 设置为故障，请及时处理。",
                "REPAIR", "REPAIR_REPORT", report.getId());

        // 告警记录来源是模拟器，供大屏明确显示为演示告警而非硬件实时告警。
        AlarmRecord alarm = new AlarmRecord();
        alarm.setDeviceId(port.getDeviceId());
        alarm.setPortId(portId);
        alarm.setCommunityId(communityId);
        alarm.setAlarmType("SIMULATOR_FAULT");
        alarm.setAlarmContent("模拟器将端口 " + port.getPortName() + " 设置为故障状态");
        alarm.setAlarmLevel("ERROR");
        alarm.setStatus("UNHANDLED");
        alarmRecordMapper.insert(alarm);

        return port;
    }

    @Override
    public ChargingPort simulatePortRecover(Long portId) {
        ChargingPort port = portMapper.selectById(portId);
        if (port == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        port.setStatus(PortStatus.IDLE.getCode());
        portMapper.updateById(port);

        // 关闭关联的未处理告警
        LambdaQueryWrapper<AlarmRecord> query = new LambdaQueryWrapper<>();
        query.eq(AlarmRecord::getPortId, portId)
             .eq(AlarmRecord::getStatus, "UNHANDLED");
        for (AlarmRecord record : alarmRecordMapper.selectList(query)) {
            record.setStatus("HANDLED");
            record.setHandleTime(LocalDateTime.now());
            record.setHandledBy(UserContext.getUserId());
            alarmRecordMapper.updateById(record);
        }

        return port;
    }

    @Override
    public void simulateChargingComplete(Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!OrderStatus.CHARGING.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不是充电中");
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(order.getStartTime(), now).toMinutes();
        if (minutes < 1) minutes = 1;

        // 计算费用
        BigDecimal hourlyRate = new BigDecimal("1.50");
        BigDecimal hours = new BigDecimal(minutes).divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
        BigDecimal amount = hours.multiply(hourlyRate).setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(new BigDecimal("0.50")) < 0) {
            amount = new BigDecimal("0.50");
        }

        // 更新订单
        order.setEndTime(now);
        order.setDurationMinutes((int) minutes);
        order.setAmount(amount);
        order.setStatus(OrderStatus.COMPLETED.getCode());
        orderMapper.updateById(order);

        // 恢复端口状态
        ChargingPort port = portMapper.selectById(order.getPortId());
        if (port != null) {
            port.setStatus(PortStatus.IDLE.getCode());
            portMapper.updateById(port);
        }

        // 发送通知
        Notification notification = new Notification();
        notification.setUserId(order.getUserId());
        notification.setTitle("充电完成通知");
        notification.setContent("您的充电已完成，时长" + minutes + "分钟，费用" + amount + "元。");
        notification.setType("CHARGING");
        notification.setBusinessType("CHARGING_ORDER");
        notification.setBusinessId(orderId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    private void createNotificationsForProperty(Long communityId, String title, String content,
                                                String type, String businessType, Long businessId) {
        SysRole propertyRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, DataScope.ROLE_PROPERTY_ADMIN)
                .eq(SysRole::getStatus, 1));
        if (propertyRole == null) {
            return;
        }
        List<Long> roleUserIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, propertyRole.getId()))
                .stream().map(SysUserRole::getUserId).distinct().toList();
        if (roleUserIds.isEmpty()) {
            return;
        }
        userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, roleUserIds)
                        .eq(SysUser::getCommunityId, communityId)
                        .eq(SysUser::getStatus, 1))
                .forEach(user -> createNotification(user.getId(), title, content, type, businessType, businessId));
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
