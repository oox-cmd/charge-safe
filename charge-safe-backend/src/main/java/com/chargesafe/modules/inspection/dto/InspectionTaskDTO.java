package com.chargesafe.modules.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检任务请求参数
 */
@Data
public class InspectionTaskDTO {

    @NotNull(message = "社区ID不能为空")
    private Long communityId;

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    private String taskDesc;

    @NotNull(message = "巡检人员ID不能为空")
    private Long inspectorId;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;
}
