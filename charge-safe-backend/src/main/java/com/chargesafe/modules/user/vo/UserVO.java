package com.chargesafe.modules.user.vo;

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
}
