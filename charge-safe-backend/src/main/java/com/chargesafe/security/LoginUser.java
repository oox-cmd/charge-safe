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
    /**
     * 当前用户拥有的权限码集合（平台管理员为 null，表示拥有全部权限）
     */
    private Set<String> permissions;

    public LoginUser() {}

    public LoginUser(Long userId, String username, String roleCode) {
        this.userId = userId;
        this.username = username;
        this.roleCode = roleCode;
    }

    /**
     * 是否拥有指定权限码（permissions 为 null 表示平台管理员，拥有全部权限）
     */
    public boolean hasPermission(String permission) {
        return permissions == null || permissions.contains(permission);
    }
}
