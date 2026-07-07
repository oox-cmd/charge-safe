package com.chargesafe.modules.repair.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工单派发请求参数
 */
@Data
public class TicketDispatchDTO {

    @NotNull(message = "报修ID不能为空")
    private Long reportId;

    @NotNull(message = "维修人员ID不能为空")
    private Long assigneeId;

    private String repairRequirement;
}
