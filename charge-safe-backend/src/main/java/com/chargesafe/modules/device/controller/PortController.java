package com.chargesafe.modules.device.controller;

import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.device.dto.PortDTO;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.service.PortService;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "充电端口管理")
@RestController
@RequestMapping("/api/ports")
@RequiredArgsConstructor
public class PortController {

    private final PortService portService;

    @Operation(summary = "分页查询端口列表")
    @GetMapping
    public Result<PageResult<ChargingPort>> pageList(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        return Result.success(portService.pageList(deviceId, status, pageQuery));
    }

    @Operation(summary = "查询设备下所有端口")
    @GetMapping("/list")
    public Result<List<ChargingPort>> listByDeviceId(@RequestParam Long deviceId) {
        return Result.success(portService.listByDeviceId(deviceId));
    }

    @Operation(summary = "查询车棚下所有端口")
    @GetMapping("/parking-area/{parkingAreaId}")
    public Result<List<ChargingPort>> listByParkingAreaId(@PathVariable Long parkingAreaId) {
        return Result.success(portService.listByParkingAreaId(parkingAreaId));
    }

    @Operation(summary = "查询空闲端口")
    @GetMapping("/idle/{parkingAreaId}")
    public Result<List<ChargingPort>> listIdlePorts(@PathVariable Long parkingAreaId) {
        return Result.success(portService.listIdlePorts(parkingAreaId));
    }

    @Operation(summary = "根据ID查询端口")
    @GetMapping("/{id}")
    public Result<ChargingPort> getById(@PathVariable Long id) {
        return Result.success(portService.getById(id));
    }

    @Operation(summary = "创建端口")
    @RequiresPermission("port:view")
    @PostMapping
    public Result<ChargingPort> create(@Valid @RequestBody PortDTO dto) {
        return Result.success(portService.create(dto));
    }

    @Operation(summary = "批量创建端口")
    @RequiresPermission("port:view")
    @PostMapping("/batch")
    public Result<List<ChargingPort>> batchCreate(
            @RequestParam Long deviceId,
            @RequestParam int count) {
        return Result.success(portService.batchCreate(deviceId, count));
    }

    @Operation(summary = "更新端口状态")
    @RequiresPermission("port:view")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        portService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "统计各状态端口数量")
    @GetMapping("/statistics")
    public Result<Map<String, Long>> countByStatus(
            @RequestParam(required = false) Long communityId) {
        return Result.success(portService.countByStatus(communityId));
    }
}
