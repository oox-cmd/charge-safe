package com.chargesafe.modules.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
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

    /**
     * 状态: PENDING-待执行 IN_PROGRESS-执行中 COMPLETED-已完成 ABNORMAL-发现异常 OVERDUE-已逾期 CANCELLED-已取消
     */
    private String status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
