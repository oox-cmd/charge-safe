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

    /**
     * 状态: PENDING-待生效 CONVERTED-已转订单 CANCELLED-用户取消 EXPIRED-超时取消 FAULT_CANCELLED-设备故障取消
     */
    private String status;

    private String cancelReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
