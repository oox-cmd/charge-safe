package com.chargesafe.modules.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安全隐患实体
 */
@Data
@TableName("safety_hazard")
public class SafetyHazard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long communityId;

    private Long taskId;

    private Long reporterId;

    private String hazardType;

    private String severity;

    private String description;

    private String images;

    private String status;

    private Long rectifierId;

    private String rectifyDesc;

    private String rectifyImages;

    private LocalDateTime rectifyTime;

    private Long reviewerId;

    private String reviewResult;

    private String reviewRemark;

    private LocalDateTime reviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    @JsonProperty("hazardNo")
    public String getHazardNo() {
        return "HZ" + (id != null ? String.format("%04d", id) : "");
    }

    @JsonProperty("title")
    public String getTitle() {
        return description;
    }

    @TableField(exist = false)
    private String reporterName;
}
