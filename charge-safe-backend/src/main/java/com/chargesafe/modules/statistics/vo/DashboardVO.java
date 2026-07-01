package com.chargesafe.modules.statistics.vo;

import lombok.Data;

/**
 * 仪表盘统计数据
 */
@Data
public class DashboardVO {

    /**
     * 端口总数
     */
    private Long totalPorts;

    /**
     * 空闲端口数量
     */
    private Long idlePorts;

    /**
     * 充电中端口数量
     */
    private Long chargingPorts;

    /**
     * 故障端口数量
     */
    private Long faultPorts;

    /**
     * 今日预约数
     */
    private Long todayReservations;

    /**
     * 今日充电订单数
     */
    private Long todayOrders;

    /**
     * 待处理工单数
     */
    private Long pendingTickets;

    /**
     * 待整改隐患数
     */
    private Long pendingHazards;

    /**
     * 未读消息数
     */
    private Long unreadNotifications;
}
