package com.chargesafe.modules.device.enums;

import lombok.Getter;

/**
 * 充电端口状态枚举
 */
@Getter
public enum PortStatus {

    IDLE("IDLE", "空闲"),
    RESERVED("RESERVED", "已预约"),
    CHARGING("CHARGING", "充电中"),
    COMPLETED_WAITING_PICKUP("COMPLETED_WAITING_PICKUP", "已完成待取车"),
    OVERTIME_OCCUPIED("OVERTIME_OCCUPIED", "超时占用"),
    FAULT("FAULT", "故障"),
    MAINTENANCE("MAINTENANCE", "维修中"),
    DISABLED("DISABLED", "停用"),
    OFFLINE("OFFLINE", "离线");

    private final String code;
    private final String desc;

    PortStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 判断端口是否可预约
     */
    public boolean isReservable() {
        return this == IDLE;
    }

    /**
     * 判断端口是否可用（开始充电）
     */
    public boolean isAvailable() {
        return this == IDLE || this == RESERVED;
    }
}
