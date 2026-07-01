package com.chargesafe.modules.repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 维修完成请求参数
 */
@Data
public class TicketFinishDTO {

    @NotBlank(message = "维修结果不能为空")
    private String repairResult;

    private String repairImages;
}
