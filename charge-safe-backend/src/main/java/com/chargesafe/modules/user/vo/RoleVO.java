package com.chargesafe.modules.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleVO {

    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;

    private LocalDateTime createdTime;

    private List<Long> menuIds;
}
