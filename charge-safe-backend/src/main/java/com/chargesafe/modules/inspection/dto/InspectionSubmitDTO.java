package com.chargesafe.modules.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 巡检提交请求参数
 */
@Data
public class InspectionSubmitDTO {

    @NotBlank(message = "检查项目不能为空")
    private String checkItem;

    @NotBlank(message = "检查结果不能为空")
    private String checkResult;

    private String remark;

    private String images;
}
