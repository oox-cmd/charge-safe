package com.chargesafe.modules.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import com.chargesafe.modules.charging.entity.ChargingReservation;
import com.chargesafe.modules.charging.enums.OrderStatus;
import com.chargesafe.modules.charging.enums.ReservationStatus;
import com.chargesafe.modules.charging.mapper.ChargingOrderMapper;
import com.chargesafe.modules.charging.mapper.ChargingReservationMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.inspection.entity.SafetyHazard;
import com.chargesafe.modules.inspection.enums.HazardStatus;
import com.chargesafe.modules.inspection.mapper.SafetyHazardMapper;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.repair.entity.RepairTicket;
import com.chargesafe.modules.repair.enums.TicketStatus;
import com.chargesafe.modules.repair.mapper.RepairTicketMapper;
import com.chargesafe.modules.statistics.service.StatisticsService;
import com.chargesafe.modules.statistics.vo.DashboardVO;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ChargingPortMapper portMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final ChargingReservationMapper reservationMapper;
    private final ChargingOrderMapper orderMapper;
    private final RepairTicketMapper ticketMapper;
    private final SafetyHazardMapper hazardMapper;
    private final NotificationMapper notificationMapper;

    @Override
    public DashboardVO getDashboard(Long communityId) {
        DashboardVO vo = new DashboardVO();

        // 获取社区下的设备ID列表
        List<Long> deviceIds = null;
        if (communityId != null) {
            List<ParkingArea> parkingAreas = parkingAreaMapper.selectList(
                    new LambdaQueryWrapper<ParkingArea>().eq(ParkingArea::getCommunityId, communityId)
            );
            List<Long> parkingAreaIds = parkingAreas.stream()
                    .map(ParkingArea::getId)
                    .collect(Collectors.toList());

            if (!parkingAreaIds.isEmpty()) {
                List<ChargingDevice> devices = deviceMapper.selectList(
                        new LambdaQueryWrapper<ChargingDevice>()
                                .in(ChargingDevice::getParkingAreaId, parkingAreaIds)
                );
                deviceIds = devices.stream()
                        .map(ChargingDevice::getId)
                        .collect(Collectors.toList());
            }
        }

        // 端口统计
        LambdaQueryWrapper<ChargingPort> portWrapper = new LambdaQueryWrapper<>();
        if (deviceIds != null && !deviceIds.isEmpty()) {
            portWrapper.in(ChargingPort::getDeviceId, deviceIds);
        }

        List<ChargingPort> allPorts = portMapper.selectList(portWrapper);
        vo.setTotalPorts((long) allPorts.size());

        vo.setIdlePorts(allPorts.stream()
                .filter(p -> PortStatus.IDLE.getCode().equals(p.getStatus()))
                .count());

        vo.setChargingPorts(allPorts.stream()
                .filter(p -> PortStatus.CHARGING.getCode().equals(p.getStatus()))
                .count());

        vo.setFaultPorts(allPorts.stream()
                .filter(p -> PortStatus.FAULT.getCode().equals(p.getStatus()))
                .count());

        // 今日预约数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayReservations(reservationMapper.selectCount(
                new LambdaQueryWrapper<ChargingReservation>()
                        .ge(ChargingReservation::getCreatedTime, todayStart)
                        .eq(communityId != null, ChargingReservation::getCommunityId, communityId)
        ));

        // 今日充电订单数
        vo.setTodayOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<ChargingOrder>()
                        .ge(ChargingOrder::getCreatedTime, todayStart)
                        .eq(communityId != null, ChargingOrder::getCommunityId, communityId)
        ));

        // 待处理工单数
        vo.setPendingTickets(ticketMapper.selectCount(
                new LambdaQueryWrapper<RepairTicket>()
                        .in(RepairTicket::getStatus,
                                TicketStatus.PENDING.getCode(),
                                TicketStatus.DISPATCHED.getCode(),
                                TicketStatus.PROCESSING.getCode())
                        .eq(communityId != null, RepairTicket::getCommunityId, communityId)
        ));

        // 待整改隐患数
        vo.setPendingHazards(hazardMapper.selectCount(
                new LambdaQueryWrapper<SafetyHazard>()
                        .in(SafetyHazard::getStatus,
                                HazardStatus.PENDING.getCode(),
                                HazardStatus.RECTIFYING.getCode())
                        .eq(communityId != null, SafetyHazard::getCommunityId, communityId)
        ));

        // 未读消息数
        Long userId = UserContext.getUserId();
        if (userId != null) {
            vo.setUnreadNotifications(notificationMapper.selectCount(
                    new LambdaQueryWrapper<com.chargesafe.modules.notification.entity.Notification>()
                            .eq(com.chargesafe.modules.notification.entity.Notification::getUserId, userId)
                            .eq(com.chargesafe.modules.notification.entity.Notification::getIsRead, 0)
            ));
        } else {
            vo.setUnreadNotifications(0L);
        }

        return vo;
    }
}
