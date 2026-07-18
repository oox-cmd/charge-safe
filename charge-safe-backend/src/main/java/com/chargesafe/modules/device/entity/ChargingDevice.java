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

    private Long categoryId;

    private Integer portCount;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String parkingAreaName;

    @TableField(exist = false)
    private String categoryName;
}
