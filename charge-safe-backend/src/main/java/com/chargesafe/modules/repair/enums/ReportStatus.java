package com.chargesafe.modules.repair.enums;

import lombok.Getter;

/**
 * 报修记录状态枚举
 */
@Getter
public enum ReportStatus {

    PENDING("PENDING", "待处理"),
    CONFIRMED("CONFIRMED", "已确认"),
    DISPATCHED("DISPATCHED", "已派单"),
    PROCESSING("PROCESSING", "处理中"),
    WAITING_ACCEPTANCE("WAITING_ACCEPTANCE", "待验收"),
    FINISHED("FINISHED", "已完成"),
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String desc;

    ReportStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
