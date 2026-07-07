package com.chargesafe.security;

import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.service.MenuService;
import com.chargesafe.modules.user.vo.MenuVO;
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
import java.util.HashSet;
import java.util.List;
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

                // 先设置上下文，以便 MenuService 能读取当前用户ID加载权限码
                UserContext.setUser(loginUser);

                // 平台管理员拥有全部权限（permissions 为 null）；其余角色按菜单权限码加载
                if (!DataScope.ROLE_PLATFORM_ADMIN.equals(roleCode)) {
                    try {
                        Set<String> permissions = new HashSet<>();
                        collectPermissions(menuService.listCurrentUserMenus(), permissions);
                        loginUser.setPermissions(permissions);
                    } catch (Exception e) {
                        log.error("加载用户权限码失败", e);
                    }
                }
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

    private void collectPermissions(List<MenuVO> menus, Set<String> permissions) {
        if (menus == null) {
            return;
        }
        for (MenuVO menu : menus) {
            if (menu.getPermissionCode() != null && !menu.getPermissionCode().isEmpty()) {
                permissions.add(menu.getPermissionCode());
            }
            collectPermissions(menu.getChildren(), permissions);
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
