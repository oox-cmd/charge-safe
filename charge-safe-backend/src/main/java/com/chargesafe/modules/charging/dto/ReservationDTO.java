package com.chargesafe.modules.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 预约请求参数
 */
@Data
public class ReservationDTO {

    @NotNull(message = "端口ID不能为空")
    private Long portId;
}
