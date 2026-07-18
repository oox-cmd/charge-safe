package com.chargesafe.modules.device.controller;

import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.device.dto.DeviceDTO;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.service.DeviceService;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "充电设备管理")
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "分页查询设备列表")
    @RequiresPermission("device:list:view")
    @GetMapping
    public Result<PageResult<ChargingDevice>> pageList(
            @RequestParam(required = false) Long parkingAreaId,
            PageQuery pageQuery) {
        return Result.success(deviceService.pageList(parkingAreaId, pageQuery));
    }

    @Operation(summary = "查询车棚下所有设备")
    @RequiresPermission("device:list:view")
    @GetMapping("/list")
    public Result<List<ChargingDevice>> listByParkingAreaId(
            @RequestParam(required = false) Long parkingAreaId) {
        return Result.success(deviceService.listByParkingAreaId(parkingAreaId));
    }

    @Operation(summary = "根据ID查询设备")
    @RequiresPermission("device:list:view")
    @GetMapping("/{id}")
    public Result<ChargingDevice> getById(@PathVariable Long id) {
        return Result.success(deviceService.getById(id));
    }

    @Operation(summary = "创建设备")
    @RequiresPermission("device:list:view")
    @PostMapping
    public Result<ChargingDevice> create(@Valid @RequestBody DeviceDTO dto) {
        return Result.success(deviceService.create(dto));
    }

    @Operation(summary = "更新设备")
    @RequiresPermission("device:list:view")
    @PutMapping("/{id}")
    public Result<ChargingDevice> update(@PathVariable Long id, @Valid @RequestBody DeviceDTO dto) {
        dto.setId(id);
        return Result.success(deviceService.update(dto));
    }

    @Operation(summary = "启用/停用设备")
    @RequiresPermission("device:list:view")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        deviceService.updateStatus(id, status);
        return Result.success();
    }
}
