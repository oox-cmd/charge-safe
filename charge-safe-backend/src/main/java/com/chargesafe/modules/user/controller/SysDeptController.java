package com.chargesafe.modules.user.controller;

import com.chargesafe.common.OperationLog;
import com.chargesafe.common.Result;
import com.chargesafe.modules.user.entity.SysDept;
import com.chargesafe.modules.user.service.SysDeptService;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/system/depts")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;

    @Operation(summary = "查询部门列表")
    @RequiresPermission("system:dept:view")
    @GetMapping
    public Result<List<SysDept>> list() {
        return Result.success(sysDeptService.list());
    }

    @Operation(summary = "创建部门")
    @OperationLog(module = "部门管理", description = "创建部门", type = "CREATE")
    @RequiresPermission("system:dept:create")
    @PostMapping
    public Result<SysDept> create(@Valid @RequestBody SysDept dept) {
        return Result.success(sysDeptService.create(dept));
    }

    @Operation(summary = "修改部门")
    @OperationLog(module = "部门管理", description = "修改部门", type = "UPDATE")
    @RequiresPermission("system:dept:edit")
    @PutMapping("/{id}")
    public Result<SysDept> update(@PathVariable Long id, @Valid @RequestBody SysDept dept) {
        return Result.success(sysDeptService.update(id, dept));
    }

    @Operation(summary = "删除部门")
    @OperationLog(module = "部门管理", description = "删除部门", type = "DELETE")
    @RequiresPermission("system:dept:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysDeptService.delete(id);
        return Result.success();
    }
}
