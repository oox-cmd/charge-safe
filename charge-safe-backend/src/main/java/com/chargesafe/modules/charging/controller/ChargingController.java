package com.chargesafe.modules.charging.controller;

import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.charging.dto.ReservationDTO;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import com.chargesafe.modules.charging.entity.ChargingReservation;
import com.chargesafe.modules.charging.service.ChargingService;
import com.chargesafe.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "预约充电管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChargingController {

    private final ChargingService chargingService;

    // ==================== 预约相关 ====================

    @Operation(summary = "预约充电端口")
    @PostMapping("/reservations")
    public Result<ChargingReservation> createReservation(@Valid @RequestBody ReservationDTO dto) {
        return Result.success(chargingService.createReservation(dto));
    }

    @Operation(summary = "取消预约")
    @PostMapping("/reservations/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id) {
        chargingService.cancelReservation(id);
        return Result.success();
    }

    @Operation(summary = "获取当前用户的有效预约")
    @GetMapping("/reservations/my")
    public Result<ChargingReservation> getMyActiveReservation() {
        return Result.success(chargingService.getMyActiveReservation());
    }

    @Operation(summary = "分页查询预约记录")
    @GetMapping("/reservations")
    public Result<PageResult<ChargingReservation>> pageReservations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        return Result.success(chargingService.pageReservations(userId, status, pageQuery));
    }

    // ==================== 充电相关 ====================

    @Operation(summary = "开始充电（扫码）")
    @PostMapping("/charging/start")
    public Result<ChargingOrder> startCharging(@RequestParam Long reservationId) {
        return Result.success(chargingService.startCharging(reservationId));
    }

    @Operation(summary = "结束充电")
    @PostMapping("/charging/{orderId}/finish")
    public Result<ChargingOrder> finishCharging(@PathVariable Long orderId) {
        return Result.success(chargingService.finishCharging(orderId));
    }

    // ==================== 订单相关 ====================

    @Operation(summary = "分页查询充电订单")
    @GetMapping("/orders")
    public Result<PageResult<ChargingOrder>> pageOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        return Result.success(chargingService.pageOrders(userId, status, pageQuery));
    }

    @Operation(summary = "查询当前用户的充电订单")
    @GetMapping("/orders/my")
    public Result<PageResult<ChargingOrder>> pageMyOrders(
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        Long userId = UserContext.getUserId();
        return Result.success(chargingService.pageOrders(userId, status, pageQuery));
    }

    @Operation(summary = "根据ID查询订单")
    @GetMapping("/orders/{id}")
    public Result<ChargingOrder> getOrderById(@PathVariable Long id) {
        return Result.success(chargingService.getOrderById(id));
    }
}
