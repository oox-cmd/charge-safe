package com.chargesafe.modules.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区实体
 */
@Data
@TableName("community")
public class Community {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private String contactName;

    private String contactPhone;

    private Double lat;

    private Double lng;

    /**
     * 状态: 0-停用 1-启用
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
