package com.chargesafe.modules.simulator.controller;

import com.chargesafe.common.Result;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.simulator.service.SimulatorService;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "设备状态模拟")
@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final SimulatorService simulatorService;

    @Operation(summary = "模拟修改端口状态")
    @RequiresPermission("simulator:manage")
    @PostMapping("/ports/{id}/status")
    public Result<ChargingPort> simulatePortStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(simulatorService.simulatePortStatus(id, status));
    }

    @Operation(summary = "模拟端口故障")
    @RequiresPermission("simulator:manage")
    @PostMapping("/ports/{id}/fault")
    public Result<ChargingPort> simulatePortFault(@PathVariable Long id) {
        return Result.success(simulatorService.simulatePortFault(id));
    }

    @Operation(summary = "模拟端口恢复正常")
    @RequiresPermission("simulator:manage")
    @PostMapping("/ports/{id}/recover")
    public Result<ChargingPort> simulatePortRecover(@PathVariable Long id) {
        return Result.success(simulatorService.simulatePortRecover(id));
    }

    @Operation(summary = "模拟充电完成")
    @RequiresPermission("simulator:manage")
    @PostMapping("/charging/{orderId}/complete")
    public Result<Void> simulateChargingComplete(@PathVariable Long orderId) {
        simulatorService.simulateChargingComplete(orderId);
        return Result.success();
    }
}
