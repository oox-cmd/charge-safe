package com.chargesafe.modules.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检任务实体
 */
@Data
@TableName("inspection_task")
public class InspectionTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long communityId;

    private String taskName;

    private String taskDesc;

    private Long inspectorId;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private String status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    @JsonProperty("taskNo")
    public String getTaskNo() {
        return "INS" + (id != null ? String.format("%04d", id) : "");
    }

    @TableField(exist = false)
    private String communityName;

    @TableField(exist = false)
    private String inspectorName;
}
