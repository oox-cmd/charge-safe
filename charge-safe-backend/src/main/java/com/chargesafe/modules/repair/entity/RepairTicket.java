package com.chargesafe.modules.repair.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private String repairImages;

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

    @TableField(exist = false)
    private String portCode;

    @TableField(exist = false)
    private String assigneeName;

    @JsonProperty("description")
    public String getDescription() {
        return faultDesc;
    }
}
