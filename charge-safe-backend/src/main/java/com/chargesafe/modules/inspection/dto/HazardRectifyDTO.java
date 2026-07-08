package com.chargesafe.modules.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 隐患整改请求参数
 */
@Data
public class HazardRectifyDTO {

    @NotBlank(message = "整改情况描述不能为空")
    private String rectifyDesc;

    private String rectifyImages;
}
