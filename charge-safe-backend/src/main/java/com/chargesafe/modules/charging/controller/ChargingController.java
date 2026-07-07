package com.chargesafe.modules.charging.controller;

import com.chargesafe.common.OperationLog;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.charging.dto.ReservationDTO;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import com.chargesafe.modules.charging.entity.ChargingReservation;
import com.chargesafe.modules.charging.service.ChargingService;
import com.chargesafe.security.RequiresPermission;
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
    @OperationLog(module = "预约充电", description = "预约充电端口", type = "CREATE")
    @PostMapping("/reservations")
    public Result<ChargingReservation> createReservation(@Valid @RequestBody ReservationDTO dto) {
        return Result.success(chargingService.createReservation(dto));
    }

    @Operation(summary = "取消预约")
    @OperationLog(module = "预约充电", description = "取消预约", type = "UPDATE")
    @PostMapping("/reservations/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id) {
        chargingService.cancelReservation(id);
        return Result.success();
    }

    @Operation(summary = "过期取消预约", description = "内部/定时任务接口：预约超过有效期后自动取消并恢复端口为空闲。也可手动触发。")
    @RequiresPermission("reservation:view")
    @PostMapping("/reservations/{id}/expire")
    public Result<Void> expireReservation(@PathVariable Long id) {
        chargingService.expireReservation(id);
        return Result.success();
    }

    @Operation(summary = "设备故障取消预约", description = "内部/模拟接口：因设备端口故障取消预约，并将端口状态置为 FAULT。")
    @RequiresPermission("reservation:view")
    @PostMapping("/reservations/{id}/fault-cancel")
    public Result<Void> faultCancelReservation(@PathVariable Long id,
                                               @RequestParam(required = false) String reason) {
        chargingService.faultCancelReservation(id, reason);
        return Result.success();
    }

    @Operation(summary = "获取当前用户的有效预约")
    @GetMapping("/reservations/my")
    public Result<ChargingReservation> getMyActiveReservation() {
        return Result.success(chargingService.getMyActiveReservation());
    }

    @Operation(summary = "分页查询预约记录")
    @RequiresPermission("reservation:view")
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

    @Operation(summary = "无预约直接开始充电")
    @PostMapping("/charging/start-direct")
    public Result<ChargingOrder> startChargingDirectly(@RequestParam Long portId) {
        return Result.success(chargingService.startChargingDirectly(portId));
    }

    @Operation(summary = "结束充电")
    @PostMapping("/charging/{orderId}/finish")
    public Result<ChargingOrder> finishCharging(@PathVariable Long orderId) {
        return Result.success(chargingService.finishCharging(orderId));
    }

    // ==================== 订单相关 ====================

    @Operation(summary = "分页查询充电订单")
    @RequiresPermission("order:view")
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
    @RequiresPermission("order:view")
    @GetMapping("/orders/{id}")
    public Result<ChargingOrder> getOrderById(@PathVariable Long id) {
        return Result.success(chargingService.getOrderById(id));
    }
}
