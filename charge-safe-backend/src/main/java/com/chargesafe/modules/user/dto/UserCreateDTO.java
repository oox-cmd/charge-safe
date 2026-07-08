package com.chargesafe.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickname;

    private String phone;

    private String avatar;

    private Integer status = 1;

    private Long communityId;

    private List<Long> roleIds;
}
