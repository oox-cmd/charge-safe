package com.chargesafe.modules.statistics.controller;

import com.chargesafe.common.Result;
import com.chargesafe.modules.statistics.service.StatisticsService;
import com.chargesafe.modules.statistics.vo.DashboardVO;
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
}
