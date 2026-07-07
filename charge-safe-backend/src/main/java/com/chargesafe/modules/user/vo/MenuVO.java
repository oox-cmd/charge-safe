package com.chargesafe.modules.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class MenuVO {

    private Long id;

    private Long parentId;

    private String menuName;

    private Integer menuType;

    private String path;

    private String component;

    private String permissionCode;

    private String icon;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createdTime;

    private List<MenuVO> children = new ArrayList<>();
}
