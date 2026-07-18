package com.chargesafe.modules.resident.controller;

import com.chargesafe.common.Result;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.resident.service.ResidentDashboardService;
import com.chargesafe.modules.resident.service.ResidentPortService;
import com.chargesafe.modules.resident.vo.ResidentDashboardVO;
import com.chargesafe.security.DataScope;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.chargesafe.modules.device.entity.ChargingPort;

@Tag(name = "居民首页")
@RestController
@RequestMapping("/api/resident")
@RequiredArgsConstructor
public class ResidentDashboardController {

    private final ResidentDashboardService residentDashboardService;
    private final ResidentPortService residentPortService;

    @Operation(summary = "获取当前居民所属社区的充电位概览")
    @RequiresPermission("resident:dashboard:view")
    @GetMapping("/dashboard")
    public Result<ResidentDashboardVO> getDashboard() {
        if (!DataScope.isResident()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return Result.success(residentDashboardService.getCurrentDashboard());
    }

    @Operation(summary = "查询当前居民所属社区车棚的端口")
    @RequiresPermission("resident:dashboard:view")
    @GetMapping("/parking-areas/{parkingAreaId}/ports")
    public Result<List<ChargingPort>> listCurrentCommunityPorts(@PathVariable Long parkingAreaId) {
        if (!DataScope.isResident()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return Result.success(residentPortService.listCurrentCommunityPorts(parkingAreaId));
    }
}
