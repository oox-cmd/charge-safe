package com.chargesafe.modules.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充电端口实体
 */
@Data
@TableName("charging_port")
public class ChargingPort {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private Integer portNumber;

    private String portName;

    /**
     * 状态: IDLE-空闲 RESERVED-已预约 CHARGING-充电中
     * COMPLETED_WAITING_PICKUP-已完成待取车 OVERTIME_OCCUPIED-超时占用
     * FAULT-故障 MAINTENANCE-维修中 DISABLED-停用 OFFLINE-离线
     */
    private String status;

    /**
     * 额定功率(W)
     */
    private BigDecimal powerRating;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    /**
     * 设备名称（非数据库字段，由 Service 填充）
     */
    @TableField(exist = false)
    private String deviceName;

    @JsonProperty("portNo")
    public Integer getPortNo() {
        return portNumber;
    }

    @JsonProperty("portCode")
    public String getPortCode() {
        return portName;
    }
}
