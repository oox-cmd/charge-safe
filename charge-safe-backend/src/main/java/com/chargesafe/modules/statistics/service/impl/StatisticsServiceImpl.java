package com.chargesafe.modules.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import com.chargesafe.modules.charging.entity.ChargingReservation;
import com.chargesafe.modules.charging.enums.OrderStatus;
import com.chargesafe.modules.charging.enums.ReservationStatus;
import com.chargesafe.modules.charging.mapper.ChargingOrderMapper;
import com.chargesafe.modules.charging.mapper.ChargingReservationMapper;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.CommunityMapper;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.inspection.entity.SafetyHazard;
import com.chargesafe.modules.inspection.enums.HazardStatus;
import com.chargesafe.modules.inspection.mapper.SafetyHazardMapper;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.repair.entity.RepairTicket;
import com.chargesafe.modules.repair.enums.TicketStatus;
import com.chargesafe.modules.repair.mapper.RepairTicketMapper;
import com.chargesafe.modules.statistics.service.StatisticsService;
import com.chargesafe.modules.statistics.vo.*;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ChargingPortMapper portMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final CommunityMapper communityMapper;
    private final ChargingReservationMapper reservationMapper;
    private final ChargingOrderMapper orderMapper;
    private final RepairTicketMapper ticketMapper;
    private final SafetyHazardMapper hazardMapper;
    private final NotificationMapper notificationMapper;

    @Override
    public DashboardVO getDashboard(Long communityId) {
        DashboardVO vo = new DashboardVO();

        // 获取社区下的设备ID列表
        List<Long> deviceIds = getDeviceIdsByCommunity(communityId);

        // 端口统计
        LambdaQueryWrapper<ChargingPort> portWrapper = new LambdaQueryWrapper<>();
        if (deviceIds != null && !deviceIds.isEmpty()) {
            portWrapper.in(ChargingPort::getDeviceId, deviceIds);
        }

        List<ChargingPort> allPorts = portMapper.selectList(portWrapper);
        vo.setTotalPorts((long) allPorts.size());

        vo.setIdlePorts(countByStatus(allPorts, PortStatus.IDLE.getCode()));
        vo.setReservedPorts(countByStatus(allPorts, PortStatus.RESERVED.getCode()));
        vo.setChargingPorts(countByStatus(allPorts, PortStatus.CHARGING.getCode()));
        vo.setFaultPorts(countByStatus(allPorts, PortStatus.FAULT.getCode()));
        vo.setMaintenancePorts(countByStatus(allPorts, PortStatus.MAINTENANCE.getCode()));
        vo.setDisabledPorts(countByStatus(allPorts, PortStatus.DISABLED.getCode()));
        vo.setOfflinePorts(countByStatus(allPorts, PortStatus.OFFLINE.getCode()));

        // 今日统计
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayReservations(reservationMapper.selectCount(
                new LambdaQueryWrapper<ChargingReservation>()
                        .ge(ChargingReservation::getCreatedTime, todayStart)
                        .eq(communityId != null, ChargingReservation::getCommunityId, communityId)
        ));

        vo.setTodayOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<ChargingOrder>()
                        .ge(ChargingOrder::getCreatedTime, todayStart)
                        .eq(communityId != null, ChargingOrder::getCommunityId, communityId)
        ));

        vo.setTodayCompletedOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<ChargingOrder>()
                        .ge(ChargingOrder::getCreatedTime, todayStart)
                        .eq(ChargingOrder::getStatus, OrderStatus.COMPLETED.getCode())
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

        // 总数统计
        vo.setTotalCommunities(communityMapper.selectCount(null));
        vo.setTotalParkingAreas(parkingAreaMapper.selectCount(null));
        vo.setTotalDevices(deviceMapper.selectCount(null));

        return vo;
    }

    @Override
    public PortStatusStatisticsVO getPortStatusStatistics(Long communityId) {
        List<Long> deviceIds = getDeviceIdsByCommunity(communityId);

        LambdaQueryWrapper<ChargingPort> portWrapper = new LambdaQueryWrapper<>();
        if (deviceIds != null && !deviceIds.isEmpty()) {
            portWrapper.in(ChargingPort::getDeviceId, deviceIds);
        }

        List<ChargingPort> allPorts = portMapper.selectList(portWrapper);

        // 按状态分组统计
        Map<String, Long> statusCountMap = allPorts.stream()
                .collect(Collectors.groupingBy(ChargingPort::getStatus, Collectors.counting()));

        // 构建结果（包含所有状态）
        List<PortStatusStatisticsVO.StatusCount> statusList = new ArrayList<>();
        for (PortStatus ps : PortStatus.values()) {
            PortStatusStatisticsVO.StatusCount sc = new PortStatusStatisticsVO.StatusCount();
            sc.setStatus(ps.getCode());
            sc.setStatusName(ps.getDesc());
            sc.setCount(statusCountMap.getOrDefault(ps.getCode(), 0L));
            statusList.add(sc);
        }

        PortStatusStatisticsVO vo = new PortStatusStatisticsVO();
        vo.setStatusList(statusList);
        return vo;
    }

    @Override
    public OrderTrendVO getOrderTrend(Long communityId) {
        List<String> dates = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        List<Long> completedCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // 最近7天
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            dates.add(date.format(formatter));

            // 当日订单数
            Long count = orderMapper.selectCount(
                    new LambdaQueryWrapper<ChargingOrder>()
                            .ge(ChargingOrder::getCreatedTime, dayStart)
                            .le(ChargingOrder::getCreatedTime, dayEnd)
                            .eq(communityId != null, ChargingOrder::getCommunityId, communityId)
            );
            orderCounts.add(count);

            // 当日完成订单数
            Long completedCount = orderMapper.selectCount(
                    new LambdaQueryWrapper<ChargingOrder>()
                            .ge(ChargingOrder::getCreatedTime, dayStart)
                            .le(ChargingOrder::getCreatedTime, dayEnd)
                            .eq(ChargingOrder::getStatus, OrderStatus.COMPLETED.getCode())
                            .eq(communityId != null, ChargingOrder::getCommunityId, communityId)
            );
            completedCounts.add(completedCount);

            // 当日订单金额（简化：查询完成订单的金额总和）
            List<ChargingOrder> dayOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<ChargingOrder>()
                            .ge(ChargingOrder::getCreatedTime, dayStart)
                            .le(ChargingOrder::getCreatedTime, dayEnd)
                            .eq(ChargingOrder::getStatus, OrderStatus.COMPLETED.getCode())
                            .eq(communityId != null, ChargingOrder::getCommunityId, communityId)
            );
            BigDecimal dayAmount = dayOrders.stream()
                    .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amounts.add(dayAmount);
        }

        OrderTrendVO vo = new OrderTrendVO();
        vo.setDates(dates);
        vo.setOrderCounts(orderCounts);
        vo.setCompletedCounts(completedCounts);
        vo.setAmounts(amounts);
        return vo;
    }

    @Override
    public RepairSummaryVO getRepairSummary(Long communityId) {
        // 按状态分组统计工单
        List<TicketStatus> allStatuses = List.of(
                TicketStatus.PENDING, TicketStatus.DISPATCHED, TicketStatus.PROCESSING,
                TicketStatus.WAITING_ACCEPTANCE, TicketStatus.FINISHED, TicketStatus.CLOSED, TicketStatus.REJECTED
        );

        List<RepairSummaryVO.StatusCount> statusList = new ArrayList<>();
        for (TicketStatus ts : allStatuses) {
            Long count = ticketMapper.selectCount(
                    new LambdaQueryWrapper<RepairTicket>()
                            .eq(RepairTicket::getStatus, ts.getCode())
                            .eq(communityId != null, RepairTicket::getCommunityId, communityId)
            );

            RepairSummaryVO.StatusCount sc = new RepairSummaryVO.StatusCount();
            sc.setStatus(ts.getCode());
            sc.setStatusName(ts.getDesc());
            sc.setCount(count);
            statusList.add(sc);
        }

        RepairSummaryVO vo = new RepairSummaryVO();
        vo.setStatusList(statusList);
        return vo;
    }

    @Override
    public HazardSummaryVO getHazardSummary(Long communityId) {
        // 按状态分组统计隐患
        List<HazardStatus> allStatuses = List.of(
                HazardStatus.PENDING, HazardStatus.RECTIFYING, HazardStatus.WAITING_REVIEW,
                HazardStatus.REVIEW_PASSED, HazardStatus.REVIEW_FAILED, HazardStatus.CLOSED
        );

        List<HazardSummaryVO.StatusCount> statusList = new ArrayList<>();
        for (HazardStatus hs : allStatuses) {
            Long count = hazardMapper.selectCount(
                    new LambdaQueryWrapper<SafetyHazard>()
                            .eq(SafetyHazard::getStatus, hs.getCode())
                            .eq(communityId != null, SafetyHazard::getCommunityId, communityId)
            );

            HazardSummaryVO.StatusCount sc = new HazardSummaryVO.StatusCount();
            sc.setStatus(hs.getCode());
            sc.setStatusName(hs.getDesc());
            sc.setCount(count);
            statusList.add(sc);
        }

        HazardSummaryVO vo = new HazardSummaryVO();
        vo.setStatusList(statusList);
        return vo;
    }

    @Override
    public ParkingAreaUsageVO getParkingAreaUsage(Long communityId) {
        // 查询车棚列表
        LambdaQueryWrapper<ParkingArea> areaWrapper = new LambdaQueryWrapper<>();
        if (communityId != null) {
            areaWrapper.eq(ParkingArea::getCommunityId, communityId);
        }
        List<ParkingArea> parkingAreas = parkingAreaMapper.selectList(areaWrapper);

        List<ParkingAreaUsageVO.ParkingAreaUsage> areaUsages = new ArrayList<>();

        for (ParkingArea area : parkingAreas) {
            // 查询该车棚下的设备
            List<ChargingDevice> devices = deviceMapper.selectList(
                    new LambdaQueryWrapper<ChargingDevice>()
                            .eq(ChargingDevice::getParkingAreaId, area.getId())
            );
            List<Long> deviceIds = devices.stream()
                    .map(ChargingDevice::getId)
                    .collect(Collectors.toList());

            // 查询端口
            List<ChargingPort> ports = new ArrayList<>();
            if (!deviceIds.isEmpty()) {
                ports = portMapper.selectList(
                        new LambdaQueryWrapper<ChargingPort>()
                                .in(ChargingPort::getDeviceId, deviceIds)
                );
            }

            int total = ports.size();
            int idle = (int) countByStatus(ports, PortStatus.IDLE.getCode());
            int charging = (int) countByStatus(ports, PortStatus.CHARGING.getCode());
            int fault = (int) countByStatus(ports, PortStatus.FAULT.getCode());

            // 使用率 = (充电中 + 已预约) / 总数 * 100
            int reserved = (int) countByStatus(ports, PortStatus.RESERVED.getCode());
            double usageRate = total > 0
                    ? BigDecimal.valueOf(charging + reserved)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
                    .doubleValue()
                    : 0.0;

            ParkingAreaUsageVO.ParkingAreaUsage usage = new ParkingAreaUsageVO.ParkingAreaUsage();
            usage.setId(area.getId());
            usage.setName(area.getName());
            usage.setTotalPorts(total);
            usage.setIdlePorts(idle);
            usage.setChargingPorts(charging);
            usage.setFaultPorts(fault);
            usage.setUsageRate(usageRate);
            areaUsages.add(usage);
        }

        ParkingAreaUsageVO vo = new ParkingAreaUsageVO();
        vo.setAreas(areaUsages);
        return vo;
    }

    // ==================== 辅助方法 ====================

    /**
     * 根据社区ID获取设备ID列表
     */
    private List<Long> getDeviceIdsByCommunity(Long communityId) {
        if (communityId == null) {
            return null;
        }

        List<ParkingArea> parkingAreas = parkingAreaMapper.selectList(
                new LambdaQueryWrapper<ParkingArea>().eq(ParkingArea::getCommunityId, communityId)
        );
        List<Long> parkingAreaIds = parkingAreas.stream()
                .map(ParkingArea::getId)
                .collect(Collectors.toList());

        if (parkingAreaIds.isEmpty()) {
            return List.of();
        }

        List<ChargingDevice> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<ChargingDevice>()
                        .in(ChargingDevice::getParkingAreaId, parkingAreaIds)
        );
        return devices.stream()
                .map(ChargingDevice::getId)
                .collect(Collectors.toList());
    }

    /**
     * 按状态计数
     */
    private long countByStatus(List<ChargingPort> ports, String status) {
        return ports.stream()
                .filter(p -> status.equals(p.getStatus()))
                .count();
    }
}
