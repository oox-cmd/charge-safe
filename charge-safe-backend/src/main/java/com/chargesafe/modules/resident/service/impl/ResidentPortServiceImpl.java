package com.chargesafe.modules.resident.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.resident.service.ResidentPortService;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResidentPortServiceImpl implements ResidentPortService {

    private final ParkingAreaMapper parkingAreaMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ChargingPortMapper portMapper;

    @Override
    public List<ChargingPort> listCurrentCommunityPorts(Long parkingAreaId) {
        Long communityId = UserContext.getCommunityId();
        if (communityId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        ParkingArea area = parkingAreaMapper.selectById(parkingAreaId);
        if (area == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!communityId.equals(area.getCommunityId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        List<ChargingDevice> devices = deviceMapper.selectList(new LambdaQueryWrapper<ChargingDevice>()
                .eq(ChargingDevice::getParkingAreaId, parkingAreaId)
                .eq(ChargingDevice::getStatus, 1));
        if (devices.isEmpty()) {
            return List.of();
        }
        return portMapper.selectList(new LambdaQueryWrapper<ChargingPort>()
                .in(ChargingPort::getDeviceId, devices.stream().map(ChargingDevice::getId).toList())
                .orderByAsc(ChargingPort::getDeviceId, ChargingPort::getPortNumber));
    }
}
