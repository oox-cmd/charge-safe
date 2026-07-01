package com.chargesafe.security;

import lombok.Data;

import java.io.Serializable;

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

    public LoginUser() {}

    public LoginUser(Long userId, String username, String roleCode) {
        this.userId = userId;
        this.username = username;
        this.roleCode = roleCode;
    }
}
