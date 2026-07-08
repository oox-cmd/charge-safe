package com.chargesafe.modules.user.dto;

import lombok.Data;

@Data
public class RoleQueryDTO {

    private String roleName;

    private String roleCode;

    private Integer status;
}
