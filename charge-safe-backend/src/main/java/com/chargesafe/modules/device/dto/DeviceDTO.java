package com.chargesafe.modules.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 充电设备请求参数
 */
@Data
public class DeviceDTO {

    private Long id;

    @NotNull(message = "车棚ID不能为空")
    private Long parkingAreaId;

    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    private String deviceType;
}
