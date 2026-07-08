package com.chargesafe.modules.inspection.enums;

import lombok.Getter;

/**
 * 安全隐患状态枚举
 */
@Getter
public enum HazardStatus {

    PENDING("PENDING", "待处理"),
    RECTIFYING("RECTIFYING", "整改中"),
    WAITING_REVIEW("WAITING_REVIEW", "待复查"),
    REVIEW_PASSED("REVIEW_PASSED", "复查通过"),
    REVIEW_FAILED("REVIEW_FAILED", "复查不通过"),
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String desc;

    HazardStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
