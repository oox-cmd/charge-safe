package com.chargesafe.modules.charging.enums;

import lombok.Getter;

/**
 * 充电订单状态枚举
 */
@Getter
public enum OrderStatus {

    CHARGING("CHARGING", "充电中"),
    COMPLETED("COMPLETED", "已完成"),
    SETTLED("SETTLED", "已结算"),
    CANCELLED("CANCELLED", "已取消"),
    ABNORMAL("ABNORMAL", "异常结束");

    private final String code;
    private final String desc;

    OrderStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
