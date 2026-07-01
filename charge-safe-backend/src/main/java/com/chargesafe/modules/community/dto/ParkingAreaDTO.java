package com.chargesafe.modules.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 车棚请求参数
 */
@Data
public class ParkingAreaDTO {

    private Long id;

    @NotNull(message = "社区ID不能为空")
    private Long communityId;

    @NotBlank(message = "车棚名称不能为空")
    private String name;

    private String location;
}
