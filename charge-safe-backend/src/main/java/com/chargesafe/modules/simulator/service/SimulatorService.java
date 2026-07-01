package com.chargesafe.modules.simulator.service;

import com.chargesafe.modules.device.entity.ChargingPort;

public interface SimulatorService {

    /**
     * 模拟修改端口状态
     */
    ChargingPort simulatePortStatus(Long portId, String status);

    /**
     * 模拟端口故障
     */
    ChargingPort simulatePortFault(Long portId);

    /**
     * 模拟端口恢复正常
     */
    ChargingPort simulatePortRecover(Long portId);

    /**
     * 模拟充电完成
     */
    void simulateChargingComplete(Long orderId);
}
