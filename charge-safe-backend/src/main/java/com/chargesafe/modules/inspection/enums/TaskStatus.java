package com.chargesafe.modules.inspection.enums;

import lombok.Getter;

/**
 * 巡检任务状态枚举
 */
@Getter
public enum TaskStatus {

    PENDING("PENDING", "待执行"),
    IN_PROGRESS("IN_PROGRESS", "执行中"),
    COMPLETED("COMPLETED", "已完成"),
    ABNORMAL("ABNORMAL", "发现异常"),
    OVERDUE("OVERDUE", "已逾期"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    TaskStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
