package com.chargesafe.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String avatar;

    private Integer status;

    private Long communityId;

    private Long deptId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String roleName;

    @TableField(exist = false)
    private String communityName;

    @TableField(exist = false)
    private String deptName;
}
