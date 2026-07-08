package com.chargesafe.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RoleUpdateDTO {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;

    private Integer status;

    private List<Long> menuIds;
}
