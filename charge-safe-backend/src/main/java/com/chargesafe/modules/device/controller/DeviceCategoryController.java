package com.chargesafe.modules.device.controller;

import com.chargesafe.common.OperationLog;
import com.chargesafe.common.Result;
import com.chargesafe.modules.device.entity.DeviceCategory;
import com.chargesafe.modules.device.service.DeviceCategoryService;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "设备分类管理")
@RestController
@RequestMapping("/api/devices/categories")
@RequiredArgsConstructor
public class DeviceCategoryController {

    private final DeviceCategoryService deviceCategoryService;

    @Operation(summary = "查询设备分类列表")
    @RequiresPermission("device:category:view")
    @GetMapping
    public Result<List<DeviceCategory>> list() {
        return Result.success(deviceCategoryService.list());
    }

    @Operation(summary = "创建设备分类")
    @OperationLog(module = "设备分类管理", description = "创建设备分类", type = "CREATE")
    @RequiresPermission("device:category:create")
    @PostMapping
    public Result<DeviceCategory> create(@Valid @RequestBody DeviceCategory category) {
        return Result.success(deviceCategoryService.create(category));
    }

    @Operation(summary = "修改设备分类")
    @OperationLog(module = "设备分类管理", description = "修改设备分类", type = "UPDATE")
    @RequiresPermission("device:category:edit")
    @PutMapping("/{id}")
    public Result<DeviceCategory> update(@PathVariable Long id, @Valid @RequestBody DeviceCategory category) {
        return Result.success(deviceCategoryService.update(id, category));
    }

    @Operation(summary = "删除设备分类")
    @OperationLog(module = "设备分类管理", description = "删除设备分类", type = "DELETE")
    @RequiresPermission("device:category:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceCategoryService.delete(id);
        return Result.success();
    }
}
