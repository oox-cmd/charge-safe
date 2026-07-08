package com.chargesafe.modules.user.controller;

import com.chargesafe.common.OperationLog;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.user.dto.*;
import com.chargesafe.modules.user.service.RoleService;
import com.chargesafe.modules.user.vo.RoleVO;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色")
    @GetMapping
    public Result<PageResult<RoleVO>> pageList(RoleQueryDTO query, PageQuery pageQuery) {
        return Result.success(roleService.pageList(query, pageQuery));
    }

    @Operation(summary = "查询启用角色")
    @GetMapping("/list")
    public Result<List<RoleVO>> listEnabled() {
        return Result.success(roleService.listEnabled());
    }

    @Operation(summary = "创建角色")
    @OperationLog(module = "角色管理", description = "创建角色", type = "CREATE")
    @RequiresPermission("system:role:create")
    @PostMapping
    public Result<RoleVO> create(@Valid @RequestBody RoleCreateDTO dto) {
        return Result.success(roleService.create(dto));
    }

    @Operation(summary = "更新角色")
    @OperationLog(module = "角色管理", description = "更新角色", type = "UPDATE")
    @RequiresPermission("system:role:update")
    @PutMapping("/{id}")
    public Result<RoleVO> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        return Result.success(roleService.update(id, dto));
    }

    @Operation(summary = "删除角色")
    @OperationLog(module = "角色管理", description = "删除角色", type = "DELETE")
    @RequiresPermission("system:role:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "分配角色菜单")
    @OperationLog(module = "角色管理", description = "分配角色菜单", type = "UPDATE")
    @RequiresPermission("system:role:assign-menu")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody AssignMenuDTO dto) {
        roleService.assignMenus(id, dto);
        return Result.success();
    }
}
