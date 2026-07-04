package com.chargesafe.modules.user.controller;

import com.chargesafe.common.OperationLog;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.user.dto.*;
import com.chargesafe.modules.user.service.UserService;
import com.chargesafe.modules.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<PageResult<UserVO>> pageList(UserQueryDTO query, PageQuery pageQuery) {
        return Result.success(userService.pageList(query, pageQuery));
    }

    @Operation(summary = "创建用户")
    @OperationLog(module = "用户管理", description = "创建用户", type = "CREATE")
    @PostMapping
    public Result<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.create(dto));
    }

    @Operation(summary = "更新用户")
    @OperationLog(module = "用户管理", description = "更新用户", type = "UPDATE")
    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        return Result.success(userService.update(id, dto));
    }

    @Operation(summary = "删除用户")
    @OperationLog(module = "用户管理", description = "删除用户", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @Operation(summary = "启用/停用用户")
    @OperationLog(module = "用户管理", description = "启用/停用用户", type = "UPDATE")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "分配用户角色")
    @OperationLog(module = "用户管理", description = "分配用户角色", type = "UPDATE")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AssignRoleDTO dto) {
        userService.assignRoles(id, dto);
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    @OperationLog(module = "用户管理", description = "重置用户密码", type = "UPDATE")
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @RequestParam(defaultValue = "123456") String password) {
        userService.resetPassword(id, password);
        return Result.success();
    }
}
