package com.chargesafe.modules.repair.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维修工单实体
 */
@Data
@TableName("repair_ticket")
public class RepairTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;

    private Long reportId;

    private Long communityId;

    private Long deviceId;

    private Long portId;

    private Long dispatcherId;

    private Long assigneeId;

    private String faultDesc;

    private String repairRequirement;

    private String repairResult;

    /**
     * 维修照片(JSON数组)
     */
    private String repairImages;

    /**
     * 状态: PENDING-待受理 DISPATCHED-已派单 PROCESSING-处理中 WAITING_ACCEPTANCE-待验收 FINISHED-已完成 CLOSED-已关闭 REJECTED-已驳回
     */
    private String status;

    private LocalDateTime dispatchTime;

    private LocalDateTime acceptTime;

    private LocalDateTime finishTime;

    private LocalDateTime closeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
