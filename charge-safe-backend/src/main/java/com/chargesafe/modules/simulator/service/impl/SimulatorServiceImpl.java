package com.chargesafe.modules.simulator.service.impl;

import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SimulatorServiceImpl implements SimulatorService {

    private final ChargingPortMapper portMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final RepairReportMapper reportMapper;
    private final NotificationMapper notificationMapper;
    private final ChargingOrderMapper orderMapper;

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

        // 设置端口为故障状态
        port.setStatus(PortStatus.FAULT.getCode());
        portMapper.updateById(port);

        // 获取设备所属社区ID
        Long communityId = null;
        ChargingDevice device = deviceMapper.selectById(port.getDeviceId());
        if (device != null) {
            ParkingArea parkingArea = parkingAreaMapper.selectById(device.getParkingAreaId());
            if (parkingArea != null) {
                communityId = parkingArea.getCommunityId();
            }
        }

        // 自动生成报修记录
        RepairReport report = new RepairReport();
        report.setReporterId(1L); // 系统自动上报
        report.setCommunityId(communityId);
        report.setDeviceId(port.getDeviceId());
        report.setPortId(portId);
        report.setFaultType("SYSTEM_ALARM");
        report.setFaultDesc("系统检测到端口故障，自动生成报修记录。");
        report.setStatus(ReportStatus.PENDING.getCode());
        reportMapper.insert(report);

        // 通知物业管理员
        Notification notification = new Notification();
        notification.setUserId(2L); // 默认物业管理员
        notification.setTitle("设备故障告警");
        notification.setContent("端口 " + port.getPortName() + " 发生故障，请及时处理。");
        notification.setType("REPAIR");
        notification.setBusinessType("REPAIR_REPORT");
        notification.setBusinessId(report.getId());
        notification.setIsRead(0);
        notificationMapper.insert(notification);

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
}
