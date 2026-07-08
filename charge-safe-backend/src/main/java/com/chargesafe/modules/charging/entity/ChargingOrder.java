package com.chargesafe.modules.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private Integer durationMinutes;

    private BigDecimal powerConsumed;

    private BigDecimal amount;

    private String status;

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

    @TableField(exist = false)
    private String portCode;

    @JsonProperty("chargingPrice")
    public BigDecimal getChargingPrice() {
        return null;
    }
}
