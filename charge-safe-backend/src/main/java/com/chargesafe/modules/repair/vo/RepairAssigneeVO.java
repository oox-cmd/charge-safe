package com.chargesafe.modules.repair.vo;

import lombok.Data;

/**
 * 可派发维修人员的最小公开信息。
 */
@Data
public class RepairAssigneeVO {

    private Long id;

    private String username;

    private String nickname;
}
