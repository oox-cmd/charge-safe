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
import com.chargesafe.modules.notification.mapper.AlarmRecordMapper;
import com.chargesafe.modules.notification.entity.AlarmRecord;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
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
    private final AlarmRecordMapper alarmRecordMapper;

    @Override
    public ManagementBigscreenVO getManagementBigscreen(Long communityId) {
        ManagementBigscreenVO vo = new ManagementBigscreenVO();
        vo.setDashboard(getDashboard(communityId));
        vo.setPortStatus(getPortStatusStatistics(communityId));
        vo.setOrderTrend(getOrderTrend(communityId).getTrends());
        vo.setParkingAreaUsage(getParkingAreaUsage(communityId));
        vo.setAlarms(getAlarms(communityId));
        vo.setCommunities(getSceneCommunities(communityId));
        vo.setDataSource("SIMULATED_DATABASE");
        vo.setDataDisclaimer("演示环境：数据来自业务数据库与设备模拟器，不代表真实硬件实时采集。");
        return vo;
    }

    @Override
    public DashboardVO getDashboard(Long communityId) {
        DashboardVO vo = new DashboardVO();

        // 获取社区下的设备ID列表
        List<Long> deviceIds = getDeviceIdsByCommunity(communityId);

        // 端口统计
        List<ChargingPort> allPorts = getPortsByDeviceScope(deviceIds);
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
        vo.setTotalParkingAreas(parkingAreaMapper.selectCount(
                new LambdaQueryWrapper<ParkingArea>()
                        .eq(communityId != null, ParkingArea::getCommunityId, communityId)
        ));
        vo.setTotalDevices(deviceIds == null ? deviceMapper.selectCount(null) : (long) deviceIds.size());

        return vo;
    }

    @Override
    public PortStatusStatisticsVO getPortStatusStatistics(Long communityId) {
        List<Long> deviceIds = getDeviceIdsByCommunity(communityId);

        List<ChargingPort> allPorts = getPortsByDeviceScope(deviceIds);

        // 按状态分组统计
        Map<String, Long> statusCountMap = allPorts.stream()
                .collect(Collectors.groupingBy(ChargingPort::getStatus, Collectors.counting()));

        // 构建 statusList（统计页使用）
        List<PortStatusStatisticsVO.StatusCount> statusList = new ArrayList<>();
        for (PortStatus ps : PortStatus.values()) {
            PortStatusStatisticsVO.StatusCount sc = new PortStatusStatisticsVO.StatusCount();
            sc.setStatus(ps.getCode());
            sc.setStatusName(ps.getDesc());
            sc.setCount(statusCountMap.getOrDefault(ps.getCode(), 0L));
            statusList.add(sc);
        }

        // 构建扁平 portCounts（运维 dashboard 使用）
        Map<String, Long> portCounts = new LinkedHashMap<>();
        for (PortStatus ps : PortStatus.values()) {
            portCounts.put(ps.getCode(), statusCountMap.getOrDefault(ps.getCode(), 0L));
        }

        PortStatusStatisticsVO vo = new PortStatusStatisticsVO();
        vo.setStatusList(statusList);
        vo.setPortCounts(portCounts);
        return vo;
    }

    @Override
    public OrderTrendVO getOrderTrend(Long communityId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        List<String> dates = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        List<Long> completedCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<OrderTrendVO.DayTrend> trends = new ArrayList<>();

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

            // 当日订单金额
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

            OrderTrendVO.DayTrend trend = new OrderTrendVO.DayTrend();
            trend.setDate(date.format(formatter));
            trend.setCount(count);
            trend.setAmount(dayAmount);
            trends.add(trend);
        }

        OrderTrendVO vo = new OrderTrendVO();
        vo.setDates(dates);
        vo.setOrderCounts(orderCounts);
        vo.setCompletedCounts(completedCounts);
        vo.setAmounts(amounts);
        vo.setTrends(trends);
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

            // 按端口号升序排序，保证大屏端口矩阵展示有序
            List<ChargingPort> sortedPorts = ports.stream()
                    .sorted(Comparator.comparing(ChargingPort::getPortNumber,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            ParkingAreaUsageVO.ParkingAreaUsage usage = new ParkingAreaUsageVO.ParkingAreaUsage();
            usage.setCommunityId(area.getCommunityId());
            usage.setId(area.getId());
            usage.setName(area.getName());
            usage.setPorts(sortedPorts);
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

    private List<ChargingPort> getPortsByDeviceScope(List<Long> deviceIds) {
        if (deviceIds == null) {
            return portMapper.selectList(new LambdaQueryWrapper<>());
        }
        if (deviceIds.isEmpty()) {
            return List.of();
        }
        return portMapper.selectList(
                new LambdaQueryWrapper<ChargingPort>().in(ChargingPort::getDeviceId, deviceIds)
        );
    }

    private List<CommunitySceneVO> getSceneCommunities(Long communityId) {
        List<Community> communities = communityMapper.selectList(
                new LambdaQueryWrapper<Community>()
                        .eq(Community::getStatus, 1)
                        .eq(communityId != null, Community::getId, communityId)
                        .orderByAsc(Community::getId)
        );

        return communities.stream().map(community -> {
            CommunitySceneVO scene = new CommunitySceneVO();
            scene.setId(community.getId());
            scene.setName(community.getName());
            scene.setLat(community.getLat());
            scene.setLng(community.getLng());
            return scene;
        }).toList();
    }

    /**
     * 按状态计数
     */
    private long countByStatus(List<ChargingPort> ports, String status) {
        return ports.stream()
                .filter(p -> status.equals(p.getStatus()))
                .count();
    }
    /**
     * 获取统一告警记录
     */
    private List<AlarmRecordVO> getAlarms(Long communityId) {
        List<AlarmRecordVO> alarms = new ArrayList<>();

        // 1. 获取 alarm_record 表中的实际告警记录
        List<AlarmRecord> realAlarms = alarmRecordMapper.selectList(
                new LambdaQueryWrapper<AlarmRecord>()
                        .eq(communityId != null, AlarmRecord::getCommunityId, communityId)
                        .orderByDesc(AlarmRecord::getCreatedTime)
                        .last("LIMIT 20")
        );
        for (AlarmRecord record : realAlarms) {
            AlarmRecordVO alarm = new AlarmRecordVO();
            alarm.setId("ALARM-" + record.getId());
            alarm.setDeviceId(record.getDeviceId());
            alarm.setCommunityId(record.getCommunityId());
            alarm.setAlarmType(record.getAlarmType());
            alarm.setAlarmContent(record.getAlarmContent());
            alarm.setAlarmLevel(record.getAlarmLevel());
            alarm.setCreatedTime(record.getCreatedTime() != null ? record.getCreatedTime() : LocalDateTime.now());
            alarms.add(alarm);
        }

        // 2. 获取未闭环的安全隐患
        List<SafetyHazard> hazards = hazardMapper.selectList(
                new LambdaQueryWrapper<SafetyHazard>()
                        .in(SafetyHazard::getStatus,
                                HazardStatus.PENDING.getCode(),
                                HazardStatus.RECTIFYING.getCode(),
                                HazardStatus.WAITING_REVIEW.getCode(),
                                HazardStatus.REVIEW_FAILED.getCode())
                        .eq(communityId != null, SafetyHazard::getCommunityId, communityId)
        );
        for (SafetyHazard hazard : hazards) {
            AlarmRecordVO alarm = new AlarmRecordVO();
            alarm.setId("HAZARD-" + hazard.getId());
            alarm.setCommunityId(hazard.getCommunityId());
            alarm.setAlarmType("HAZARD");
            alarm.setAlarmContent(hazard.getDescription() != null ? hazard.getDescription() : "安全隐患待处理");
            alarm.setAlarmLevel(hazard.getSeverity() != null ? hazard.getSeverity() : "WARNING");
            alarm.setCreatedTime(hazard.getCreatedTime() != null ? hazard.getCreatedTime() : LocalDateTime.now());
            alarms.add(alarm);
        }

        // 按时间倒序
        alarms.sort(Comparator.comparing(AlarmRecordVO::getCreatedTime).reversed());
        return alarms;
    }
}
