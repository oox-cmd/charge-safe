package com.chargesafe.modules.user.dto;

import lombok.Data;

@Data
public class UserQueryDTO {

    private String username;

    private String phone;

    private Integer status;

    private Long roleId;

    private Long communityId;
}
