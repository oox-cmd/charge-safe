package com.chargesafe.modules.repair.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修记录实体
 */
@Data
@TableName("repair_report")
public class RepairReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reporterId;

    private Long communityId;

    private Long deviceId;

    private Long portId;

    private String faultType;

    @JsonProperty("faultDesc")
    private String faultDesc;

    /**
     * 故障照片(JSON数组)
     */
    private String faultImages;

    /**
     * 状态: PENDING-待处理 CONFIRMED-已确认 DISPATCHED-已派单 REJECTED-已驳回
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String reporterName;

    @TableField(exist = false)
    private String deviceName;
}
