package com.chargesafe.modules.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车棚/停放区实体
 */
@Data
@TableName("parking_area")
public class ParkingArea {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long communityId;

    private String name;

    private String location;

    /**
     * 端口总数
     */
    private Integer portCount;

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
