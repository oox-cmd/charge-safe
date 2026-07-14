package com.chargesafe.security;

import com.chargesafe.common.Result;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.service.MenuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final SysUserMapper userMapper;
    private final MenuService menuService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        UserContext.remove();
        try {
            String authorization = request.getHeader("Authorization");
            if (!StringUtils.hasText(authorization) && isPublicRequest(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = getTokenFromRequest(request);
            if (!StringUtils.hasText(token)) {
                rejectUnauthorized(request, response, "missing_or_malformed_authorization");
                return;
            }

            if (!authenticate(request, response, token)) {
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.remove();
        }
    }

    private boolean authenticate(HttpServletRequest request, HttpServletResponse response, String token)
            throws IOException {
        try {
            if (!jwtUtils.validateToken(token)) {
                rejectUnauthorized(request, response, "invalid_token");
                return false;
            }

            Long userId = jwtUtils.getUserId(token);
            String username = jwtUtils.getUsername(token);
            String roleCode = jwtUtils.getRoleCode(token);

            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                rejectUnauthorized(request, response, "user_not_found");
                return false;
            }
            if (Integer.valueOf(0).equals(user.getStatus())) {
                rejectUnauthorized(request, response, "account_disabled");
                return false;
            }

            LoginUser loginUser = new LoginUser(userId, username, roleCode);
            loginUser.setNickname(user.getNickname());
            loginUser.setCommunityId(user.getCommunityId());

            try {
                Set<String> permissions = menuService.listPermissionCodesByUserId(userId);
                loginUser.setPermissions(permissions);
            } catch (Exception e) {
                log.warn("加载用户权限码失败: type={}, path={}",
                        e.getClass().getSimpleName(), request.getRequestURI());
            }

            UserContext.setUser(loginUser);
            return true;
        } catch (Exception e) {
            log.warn("JWT认证失败: type={}, path={}",
                    e.getClass().getSimpleName(), request.getRequestURI());
            rejectUnauthorized(request, response, "authentication_exception");
            return false;
        }
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/api/auth/login".equals(requestPath(request));
    }

    private String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return StringUtils.hasText(contextPath) ? uri.substring(contextPath.length()) : uri;
    }

    private void rejectUnauthorized(HttpServletRequest request, HttpServletResponse response, String reason)
            throws IOException {
        UserContext.remove();
        log.warn("JWT认证拒绝: reason={}, path={}", reason, request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.error(ResultCode.UNAUTHORIZED));
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
