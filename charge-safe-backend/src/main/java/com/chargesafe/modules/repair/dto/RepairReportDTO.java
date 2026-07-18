package com.chargesafe.modules.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报修请求参数
 */
@Data
public class RepairReportDTO {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    private Long portId;

    @NotBlank(message = "故障类型不能为空")
    private String faultType;

    @NotBlank(message = "故障描述不能为空")
    private String faultDesc;

    private String faultImages;
}
