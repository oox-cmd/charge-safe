package com.chargesafe.modules.resident.service;

import com.chargesafe.modules.device.entity.ChargingPort;

import java.util.List;

/** 居民端车棚端口查询；所属社区始终取自登录上下文。 */
public interface ResidentPortService {

    List<ChargingPort> listCurrentCommunityPorts(Long parkingAreaId);
}
