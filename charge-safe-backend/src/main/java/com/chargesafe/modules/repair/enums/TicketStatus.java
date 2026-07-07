package com.chargesafe.modules.repair.enums;

import lombok.Getter;

/**
 * 维修工单状态枚举
 */
@Getter
public enum TicketStatus {

    PENDING("PENDING", "待受理"),
    DISPATCHED("DISPATCHED", "已派单"),
    PROCESSING("PROCESSING", "处理中"),
    WAITING_ACCEPTANCE("WAITING_ACCEPTANCE", "待验收"),
    FINISHED("FINISHED", "已完成"),
    CLOSED("CLOSED", "已关闭"),
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String desc;

    TicketStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
