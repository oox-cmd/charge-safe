package com.chargesafe.modules.auth.controller;

import com.chargesafe.common.OperationLog;
import com.chargesafe.common.Result;
import com.chargesafe.modules.auth.dto.LoginDTO;
import com.chargesafe.modules.auth.service.AuthService;
import com.chargesafe.modules.auth.vo.LoginVO;
import com.chargesafe.modules.user.service.MenuService;
import com.chargesafe.modules.user.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MenuService menuService;

    @Operation(summary = "用户登录")
    @OperationLog(module = "认证管理", description = "用户登录", type = "LOGIN")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<LoginVO> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }

    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/menus")
    public Result<java.util.List<MenuVO>> getCurrentUserMenus() {
        return Result.success(menuService.listCurrentUserMenus());
    }
}
