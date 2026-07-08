package com.chargesafe.security;

import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.service.MenuService;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserId(token);
                String username = jwtUtils.getUsername(token);
                String roleCode = jwtUtils.getRoleCode(token);

                LoginUser loginUser = new LoginUser(userId, username, roleCode);

                // 查询用户完整信息（包含社区ID）
                SysUser user = userMapper.selectById(userId);
                if (user != null) {
                    loginUser.setNickname(user.getNickname());
                    loginUser.setCommunityId(user.getCommunityId());
                }

                // 加载用户权限码集合
                try {
                    Set<String> permissions = menuService.listPermissionCodesByUserId(userId);
                    loginUser.setPermissions(permissions);
                } catch (Exception e) {
                    log.warn("加载用户权限码失败: {}", e.getMessage());
                }

                UserContext.setUser(loginUser);
            }
        } catch (Exception e) {
            log.error("JWT认证失败", e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.remove();
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
