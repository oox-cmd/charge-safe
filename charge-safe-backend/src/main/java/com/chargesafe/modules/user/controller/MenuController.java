package com.chargesafe.modules.user.controller;

import com.chargesafe.common.OperationLog;
import com.chargesafe.common.Result;
import com.chargesafe.modules.user.dto.MenuCreateDTO;
import com.chargesafe.modules.user.dto.MenuUpdateDTO;
import com.chargesafe.modules.user.service.MenuService;
import com.chargesafe.modules.user.vo.MenuVO;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "查询菜单树")
    @RequiresPermission("system:menu:view")
    @GetMapping
    public Result<List<MenuVO>> listTree(@RequestParam(required = false, defaultValue = "false") Boolean onlyEnabled) {
        return Result.success(menuService.listTree(onlyEnabled));
    }

    @Operation(summary = "查询菜单树")
    @RequiresPermission("system:menu:view")
    @GetMapping("/tree")
    public Result<List<MenuVO>> tree(@RequestParam(required = false, defaultValue = "false") Boolean onlyEnabled) {
        return Result.success(menuService.listTree(onlyEnabled));
    }

    @Operation(summary = "创建菜单")
    @OperationLog(module = "菜单管理", description = "创建菜单", type = "CREATE")
    @RequiresPermission("system:menu:create")
    @PostMapping
    public Result<MenuVO> create(@Valid @RequestBody MenuCreateDTO dto) {
        return Result.success(menuService.create(dto));
    }

    @Operation(summary = "更新菜单")
    @OperationLog(module = "菜单管理", description = "更新菜单", type = "UPDATE")
    @RequiresPermission("system:menu:update")
    @PutMapping("/{id}")
    public Result<MenuVO> update(@PathVariable Long id, @Valid @RequestBody MenuUpdateDTO dto) {
        return Result.success(menuService.update(id, dto));
    }

    @Operation(summary = "删除菜单")
    @OperationLog(module = "菜单管理", description = "删除菜单", type = "DELETE")
    @RequiresPermission("system:menu:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
