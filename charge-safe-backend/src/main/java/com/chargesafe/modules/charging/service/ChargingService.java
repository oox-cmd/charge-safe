package com.chargesafe.modules.charging.service;

import com.chargesafe.common.PageResult;
import com.chargesafe.common.PageQuery;
import com.chargesafe.modules.charging.dto.ReservationDTO;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import com.chargesafe.modules.charging.entity.ChargingReservation;

public interface ChargingService {

    /**
     * 预约充电端口
     */
    ChargingReservation createReservation(ReservationDTO dto);

    /**
     * 取消预约
     */
    void cancelReservation(Long reservationId);

    /**
     * 查询当前用户的有效预约
     */
    ChargingReservation getMyActiveReservation();

    /**
     * 分页查询预约记录
     */
    PageResult<ChargingReservation> pageReservations(Long userId, String status, PageQuery pageQuery);

    /**
     * 开始充电（扫码）
     */
    ChargingOrder startCharging(Long reservationId);

    /**
     * 结束充电
     */
    ChargingOrder finishCharging(Long orderId);

    /**
     * 分页查询充电订单
     */
    PageResult<ChargingOrder> pageOrders(Long userId, String status, PageQuery pageQuery);

    /**
     * 根据ID查询订单
     */
    ChargingOrder getOrderById(Long orderId);
}
