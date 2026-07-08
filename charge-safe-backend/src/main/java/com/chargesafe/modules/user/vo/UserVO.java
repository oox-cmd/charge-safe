package com.chargesafe.modules.user.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String phone;

    private String avatar;

    private Integer status;

    private Long communityId;

    private LocalDateTime createdTime;

    private List<Long> roleIds;

    private List<String> roleNames;

    private List<String> roleCodes;

    @JsonProperty("roleName")
    public String getRoleName() {
        return roleNames != null && !roleNames.isEmpty() ? String.join(",", roleNames) : null;
    }

    private String communityName;
}
