package com.chargesafe.modules.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignRoleDTO {

    private List<Long> roleIds;
}
