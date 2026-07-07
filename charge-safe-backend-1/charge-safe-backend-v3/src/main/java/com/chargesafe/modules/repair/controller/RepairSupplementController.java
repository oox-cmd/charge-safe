package com.chargesafe.modules.repair.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.common.ResultCode;
import com.chargesafe.common.BusinessException;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.repair.entity.RepairReport;
import com.chargesafe.modules.repair.mapper.RepairReportMapper;
import com.chargesafe.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "报修补充接口（小程序）")
@RestController
@RequestMapping("/api/repair-reports")
@RequiredArgsConstructor
public class RepairSupplementController {

    private final RepairReportMapper repairReportMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ChargingPortMapper portMapper;

    @Operation(summary = "提交报修")
    @PostMapping
    @Transactional
    @PreAuthorize("@cs.hasAnyRole('RESIDENT', 'PROPERTY_STAFF')")
    public Result<Map<String, Object>> createReport(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);

        String faultType = (String) params.getOrDefault("faultType", "");
        String faultDesc = (String) params.getOrDefault("faultDesc", params.getOrDefault("description", ""));
        Object deviceIdObj = params.get("deviceId");
        Object portIdObj = params.get("portId");

        RepairReport report = new RepairReport();
        report.setUserId(userId);
        report.setFaultType(faultType);
        report.setFaultDesc(faultDesc);
        report.setStatus("PENDING");

        if (deviceIdObj != null) {
            Long deviceId = Long.valueOf(deviceIdObj.toString());
            report.setDeviceId(deviceId);
        }
        if (report.getCommunityId() == null) {
            report.setCommunityId(UserContext.getCommunityId());
        }
        if (portIdObj != null) {
            Long portId = Long.valueOf(portIdObj.toString());
            report.setPortId(portId);
        }

        repairReportMapper.insert(report);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", report.getId());
        result.put("status", "PENDING");
        return Result.success(result);
    }

    @Operation(summary = "我的报修记录")
    @GetMapping("/my")
    public Result<PageResult<RepairReport>> pageMyReports(
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        Page<RepairReport> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<RepairReport> wrapper = new LambdaQueryWrapper<RepairReport>()
                .eq(RepairReport::getUserId, userId)
                .eq(status != null && !status.isEmpty(), RepairReport::getStatus, status)
                .orderByDesc(RepairReport::getCreatedTime);
        Page<RepairReport> result = repairReportMapper.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "报修详情")
    @GetMapping("/{id}")
    public Result<RepairReport> getById(@PathVariable Long id) {
        RepairReport report = repairReportMapper.selectById(id);
        if (report == null) throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        return Result.success(report);
    }
}
