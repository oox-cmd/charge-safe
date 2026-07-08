package com.chargesafe.modules.charging.enums;

import lombok.Getter;

/**
 * 预约状态枚举
 */
@Getter
public enum ReservationStatus {

    PENDING("PENDING", "待生效"),
    CONVERTED("CONVERTED", "已转订单"),
    CANCELLED("CANCELLED", "用户取消"),
    EXPIRED("EXPIRED", "超时取消"),
    FAULT_CANCELLED("FAULT_CANCELLED", "设备故障取消");

    private final String code;
    private final String desc;

    ReservationStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 判断预约是否有效
     */
    public boolean isActive() {
        return this == PENDING;
    }
}
