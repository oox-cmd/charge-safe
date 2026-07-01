package com.chargesafe.modules.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充电订单实体
 */
@Data
@TableName("charging_order")
public class ChargingOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long portId;

    private Long reservationId;

    private Long communityId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 充电时长(分钟)
     */
    private Integer durationMinutes;

    /**
     * 用电量(度)
     */
    private BigDecimal powerConsumed;

    /**
     * 费用(元)
     */
    private BigDecimal amount;

    /**
     * 状态: CHARGING-充电中 COMPLETED-已完成 SETTLED-已结算 CANCELLED-已取消 ABNORMAL-异常结束
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
