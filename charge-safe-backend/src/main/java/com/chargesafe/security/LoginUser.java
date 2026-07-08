package com.chargesafe.security;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 当前登录用户信息
 */
@Data
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String nickname;
    private String roleCode;
    private Long communityId;
    private Set<String> permissions;

    public LoginUser() {}

    public LoginUser(Long userId, String username, String roleCode) {
        this.userId = userId;
        this.username = username;
        this.roleCode = roleCode;
    }

    /**
     * 判断当前用户是否拥有指定权限码。
     * <p>空值安全：permission 或 permissions 为 null/空时返回 false。
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.contains(permission);
    }
}
