package com.chargesafe.modules.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 充电预约实体
 */
@Data
@TableName("charging_reservation")
public class ChargingReservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long portId;

    private Long communityId;

    private LocalDateTime reserveTime;

    private LocalDateTime expireTime;

    private String status;

    private String cancelReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String portName;
}
