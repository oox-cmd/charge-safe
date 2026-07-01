package com.chargesafe.modules.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 充电设备实体
 */
@Data
@TableName("charging_device")
public class ChargingDevice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parkingAreaId;

    private String deviceCode;

    private String deviceName;

    private String deviceType;

    /**
     * 端口数量
     */
    private Integer portCount;

    /**
     * 状态: 0-停用 1-启用 2-故障
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
