package com.chargesafe.modules.auth.vo;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String roleCode;
    private String roleName;
    private Long communityId;
}
