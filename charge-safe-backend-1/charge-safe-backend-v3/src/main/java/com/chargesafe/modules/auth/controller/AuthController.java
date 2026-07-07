package com.chargesafe.modules.auth.controller;

import com.chargesafe.common.Result;
import com.chargesafe.modules.auth.dto.*;
import com.chargesafe.modules.auth.service.AuthService;
import com.chargesafe.modules.auth.vo.LoginVO;
import com.chargesafe.modules.auth.vo.PermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private Map<String, Object> flatLogin(LoginVO vo) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("token", vo.getToken());
        if (vo.getUser() != null) {
            map.put("userId", vo.getUser().getId());
            map.put("nickname", vo.getUser().getNickname());
            map.put("phone", vo.getUser().getPhone());
            map.put("avatar", vo.getUser().getAvatar() != null ? vo.getUser().getAvatar() : "");
        }
        return map;
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(flatLogin(authService.login(dto)));
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/login/sms")
    public Result<Map<String, Object>> smsLogin(@Valid @RequestBody SmsLoginDTO dto) {
        return Result.success(flatLogin(authService.smsLogin(dto)));
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsSendDTO dto) {
        authService.sendSmsCode(dto.getPhone());
        return Result.success();
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.success(flatLogin(authService.me()));
    }

    @Operation(summary = "获取当前用户权限及菜单")
    @GetMapping("/permissions")
    public Result<PermissionVO> getPermissions() {
        return Result.success(authService.getPermissions());
    }
}
