package com.chargesafe.modules.statistics.controller;

import com.chargesafe.common.Result;
import com.chargesafe.modules.statistics.service.StatisticsService;
import com.chargesafe.modules.statistics.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard(@RequestParam(required = false) Long communityId) {
        return Result.success(statisticsService.getDashboard(communityId));
    }

    @Operation(summary = "获取端口状态统计")
    @GetMapping("/port-status")
    public Result<PortStatusStatisticsVO> getPortStatus(@RequestParam(required = false) Long communityId) {
        return Result.success(statisticsService.getPortStatusStatistics(communityId));
    }

    @Operation(summary = "获取订单趋势统计")
    @GetMapping("/order-trend")
    public Result<OrderTrendVO> getOrderTrend(@RequestParam(required = false) Long communityId) {
        return Result.success(statisticsService.getOrderTrend(communityId));
    }

    @Operation(summary = "获取维修工单状态汇总")
    @GetMapping("/repair-summary")
    public Result<RepairSummaryVO> getRepairSummary(@RequestParam(required = false) Long communityId) {
        return Result.success(statisticsService.getRepairSummary(communityId));
    }

    @Operation(summary = "获取安全隐患状态汇总")
    @GetMapping("/hazard-summary")
    public Result<HazardSummaryVO> getHazardSummary(@RequestParam(required = false) Long communityId) {
        return Result.success(statisticsService.getHazardSummary(communityId));
    }

    @Operation(summary = "获取车棚使用率统计")
    @GetMapping("/parking-area-usage")
    public Result<ParkingAreaUsageVO> getParkingAreaUsage(@RequestParam(required = false) Long communityId) {
        return Result.success(statisticsService.getParkingAreaUsage(communityId));
    }
}
