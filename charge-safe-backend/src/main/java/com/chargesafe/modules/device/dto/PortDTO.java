package com.chargesafe.modules.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充电端口请求参数
 */
@Data
public class PortDTO {

    private Long id;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "端口号不能为空")
    private Integer portNumber;

    private String portName;

    private BigDecimal powerRating;
}
