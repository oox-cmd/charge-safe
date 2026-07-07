package com.chargesafe.modules.repair.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_report")
public class RepairReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("reporter_id")
    private Long userId;
    private Long communityId;
    private Long deviceId;
    private Long portId;
    private String faultType;
    private String faultDesc;
    @TableField("fault_images")
    private String images;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    @TableLogic
    private Integer deleted;
}
