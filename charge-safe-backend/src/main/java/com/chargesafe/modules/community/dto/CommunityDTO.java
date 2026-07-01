package com.chargesafe.modules.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 社区请求参数
 */
@Data
public class CommunityDTO {

    private Long id;

    @NotBlank(message = "社区名称不能为空")
    private String name;

    private String address;

    private String contactName;

    private String contactPhone;
}
