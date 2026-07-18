package com.chargesafe.modules.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDTO {

    private String nickname;

    private String phone;

    private String avatar;

    private Integer status;

    private Long communityId;

    private Long deptId;

    private List<Long> roleIds;
}
