package com.chargesafe.modules.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 隐患上报请求参数
 */
@Data
public class HazardReportDTO {

    @NotNull(message = "社区ID不能为空")
    private Long communityId;

    private Long taskId;

    @NotBlank(message = "隐患类型不能为空")
    private String hazardType;

    @NotBlank(message = "严重程度不能为空")
    private String severity;

    @NotBlank(message = "隐患描述不能为空")
    private String description;

    private String images;
}
