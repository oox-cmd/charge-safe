package com.chargesafe.modules.resident.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.CommunityMapper;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.resident.service.ResidentDashboardService;
import com.chargesafe.modules.resident.vo.ResidentDashboardVO;
import com.chargesafe.modules.resident.vo.ResidentParkingAreaVO;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidentDashboardServiceImpl implements ResidentDashboardService {

    private final CommunityMapper communityMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ChargingPortMapper portMapper;

    @Override
    public ResidentDashboardVO getCurrentDashboard() {
        Long communityId = UserContext.getCommunityId();
        if (communityId == null) {
            return new ResidentDashboardVO();
        }

        Community community = communityMapper.selectById(communityId);
        if (community == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        ResidentDashboardVO dashboard = new ResidentDashboardVO();
        dashboard.setCommunityId(community.getId());
        dashboard.setCommunityName(community.getName());

        List<ParkingArea> areas = parkingAreaMapper.selectList(
                new LambdaQueryWrapper<ParkingArea>()
                        .eq(ParkingArea::getCommunityId, communityId)
                        .eq(ParkingArea::getStatus, 1)
                        .orderByAsc(ParkingArea::getId));
        dashboard.setTotalParkingAreas(areas.size());
        if (areas.isEmpty()) {
            return dashboard;
        }

        List<Long> areaIds = areas.stream().map(ParkingArea::getId).toList();
        List<ChargingDevice> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<ChargingDevice>()
                        .in(ChargingDevice::getParkingAreaId, areaIds));
        Map<Long, List<ChargingDevice>> devicesByArea = devices.stream()
                .collect(Collectors.groupingBy(ChargingDevice::getParkingAreaId));

        List<Long> deviceIds = devices.stream().map(ChargingDevice::getId).toList();
        List<ChargingPort> ports = deviceIds.isEmpty()
                ? List.of()
                : portMapper.selectList(new LambdaQueryWrapper<ChargingPort>()
                        .in(ChargingPort::getDeviceId, deviceIds));
        Map<Long, List<ChargingPort>> portsByDevice = ports.stream()
                .collect(Collectors.groupingBy(ChargingPort::getDeviceId));

        List<ResidentParkingAreaVO> areaViews = new ArrayList<>();
        for (ParkingArea area : areas) {
            List<ChargingPort> areaPorts = devicesByArea.getOrDefault(area.getId(), List.of()).stream()
                    .map(ChargingDevice::getId)
                    .flatMap(deviceId -> portsByDevice.getOrDefault(deviceId, List.of()).stream())
                    .toList();
            ResidentParkingAreaVO areaView = toAreaView(area, areaPorts);
            areaViews.add(areaView);
            accumulate(dashboard, areaView);
        }
        dashboard.setParkingAreas(areaViews);
        dashboard.setAvailableForReservation(dashboard.getIdleCount() > 0);
        return dashboard;
    }

    private ResidentParkingAreaVO toAreaView(ParkingArea area, List<ChargingPort> ports) {
        Map<String, Long> counts = ports.stream()
                .map(ChargingPort::getStatus)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        ResidentParkingAreaVO view = new ResidentParkingAreaVO();
        view.setParkingAreaId(area.getId());
        view.setParkingAreaName(area.getName());
        view.setLocationDescription(area.getLocation());
        view.setTotalPorts(ports.size());
        view.setIdleCount(counts.getOrDefault(PortStatus.IDLE.getCode(), 0L));
        view.setReservedCount(counts.getOrDefault(PortStatus.RESERVED.getCode(), 0L));
        view.setChargingCount(counts.getOrDefault(PortStatus.CHARGING.getCode(), 0L));
        view.setFaultCount(counts.getOrDefault(PortStatus.FAULT.getCode(), 0L));
        view.setMaintenanceCount(counts.getOrDefault(PortStatus.MAINTENANCE.getCode(), 0L));
        view.setUnavailableCount(countUnavailable(counts));
        view.setAvailableForReservation(view.getIdleCount() > 0);
        return view;
    }

    private long countUnavailable(Map<String, Long> counts) {
        return counts.getOrDefault(PortStatus.COMPLETED_WAITING_PICKUP.getCode(), 0L)
                + counts.getOrDefault(PortStatus.OVERTIME_OCCUPIED.getCode(), 0L)
                + counts.getOrDefault(PortStatus.DISABLED.getCode(), 0L)
                + counts.getOrDefault(PortStatus.OFFLINE.getCode(), 0L);
    }

    private void accumulate(ResidentDashboardVO dashboard, ResidentParkingAreaVO area) {
        dashboard.setTotalPorts(dashboard.getTotalPorts() + area.getTotalPorts());
        dashboard.setIdleCount(dashboard.getIdleCount() + area.getIdleCount());
        dashboard.setReservedCount(dashboard.getReservedCount() + area.getReservedCount());
        dashboard.setChargingCount(dashboard.getChargingCount() + area.getChargingCount());
        dashboard.setFaultCount(dashboard.getFaultCount() + area.getFaultCount());
        dashboard.setMaintenanceCount(dashboard.getMaintenanceCount() + area.getMaintenanceCount());
        dashboard.setUnavailableCount(dashboard.getUnavailableCount() + area.getUnavailableCount());
    }
}
