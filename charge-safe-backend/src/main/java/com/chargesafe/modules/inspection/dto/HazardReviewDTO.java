package com.chargesafe.modules.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 隐患复查请求参数
 */
@Data
public class HazardReviewDTO {

    @NotBlank(message = "复查结果不能为空")
    private String reviewResult;

    private String reviewRemark;
}
