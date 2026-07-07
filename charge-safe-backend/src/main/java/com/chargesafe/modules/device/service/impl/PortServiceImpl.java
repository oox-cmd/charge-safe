package com.chargesafe.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.device.dto.PortDTO;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.device.service.PortService;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.security.DataScope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortServiceImpl implements PortService {

    private final ChargingPortMapper portMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ParkingAreaMapper parkingAreaMapper;

    @Override
    public PageResult<ChargingPort> pageList(Long deviceId, String status, PageQuery pageQuery) {
        List<Long> scopedDeviceIds = resolveDeviceIds(deviceId);
        Page<ChargingPort> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ChargingPort> wrapper = new LambdaQueryWrapper<ChargingPort>()
                .in(scopedDeviceIds != null, ChargingPort::getDeviceId, scopedDeviceIds)
                .eq(StringUtils.hasText(status), ChargingPort::getStatus, status)
                .orderByAsc(ChargingPort::getPortNumber);

        Page<ChargingPort> result = portMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public List<ChargingPort> listByDeviceId(Long deviceId) {
        List<Long> scopedDeviceIds = resolveDeviceIds(deviceId);
        if (scopedDeviceIds != null && scopedDeviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return portMapper.selectList(
                new LambdaQueryWrapper<ChargingPort>()
                        .in(scopedDeviceIds != null, ChargingPort::getDeviceId, scopedDeviceIds)
                        .orderByAsc(ChargingPort::getPortNumber)
        );
    }

    @Override
    public List<ChargingPort> listByParkingAreaId(Long parkingAreaId) {
        if (parkingAreaId == null) {
            return Collections.emptyList();
        }
        // 先查询车棚下所有设备
        List<ChargingDevice> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<ChargingDevice>()
                        .eq(ChargingDevice::getParkingAreaId, parkingAreaId)
        );
        if (devices.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> deviceIds = devices.stream()
                .map(ChargingDevice::getId)
                .collect(Collectors.toList());

        List<Long> scopedDeviceIds = resolveDeviceIds(null);
        if (scopedDeviceIds != null) {
            deviceIds = deviceIds.stream().filter(scopedDeviceIds::contains).collect(Collectors.toList());
            if (deviceIds.isEmpty()) {
                return Collections.emptyList();
            }
        }

        return portMapper.selectList(
                new LambdaQueryWrapper<ChargingPort>()
                        .in(ChargingPort::getDeviceId, deviceIds)
                        .orderByAsc(ChargingPort::getDeviceId, ChargingPort::getPortNumber)
        );
    }

    @Override
    public List<ChargingPort> listIdlePorts(Long parkingAreaId) {
        List<ChargingPort> allPorts = listByParkingAreaId(parkingAreaId);
        return allPorts.stream()
                .filter(port -> PortStatus.IDLE.getCode().equals(port.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public ChargingPort getById(Long id) {
        ChargingPort port = portMapper.selectById(id);
        if (port == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return port;
    }

    @Override
    @Transactional
    public ChargingPort create(PortDTO dto) {
        ChargingPort port = new ChargingPort();
        BeanUtils.copyProperties(dto, port);
        port.setStatus(PortStatus.IDLE.getCode());
        if (port.getPowerRating() == null) {
            port.setPowerRating(new BigDecimal("500"));
        }
        portMapper.insert(port);

        // 更新设备端口数量
        updateDevicePortCount(dto.getDeviceId());

        return port;
    }

    @Override
    @Transactional
    public List<ChargingPort> batchCreate(Long deviceId, int count) {
        ChargingDevice device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        List<ChargingPort> ports = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ChargingPort port = new ChargingPort();
            port.setDeviceId(deviceId);
            port.setPortNumber(i);
            port.setPortName(device.getDeviceName() + "-" + i + "号口");
            port.setStatus(PortStatus.IDLE.getCode());
            port.setPowerRating(new BigDecimal("500"));
            portMapper.insert(port);
            ports.add(port);
        }

        // 更新设备端口数量
        updateDevicePortCount(deviceId);

        return ports;
    }

    @Override
    public void updateStatus(Long id, String status) {
        ChargingPort port = getById(id);
        port.setStatus(status);
        portMapper.updateById(port);
    }

    @Override
    public Map<String, Long> countByStatus(Long communityId) {
        // 查询条件：如果有社区ID，需要先查社区下的设备
        LambdaQueryWrapper<ChargingPort> wrapper = new LambdaQueryWrapper<>();

        if (DataScope.isPlatformAdmin()) {
            if (communityId != null) {
                List<ChargingDevice> devices = deviceMapper.selectList(
                        new LambdaQueryWrapper<ChargingDevice>()
                                .eq(ChargingDevice::getParkingAreaId, communityId)
                );
                if (devices.isEmpty()) {
                    return new HashMap<>();
                }
                List<Long> deviceIds = devices.stream()
                        .map(ChargingDevice::getId)
                        .collect(Collectors.toList());
                wrapper.in(ChargingPort::getDeviceId, deviceIds);
            }
        } else {
            List<Long> deviceIds = resolveDeviceIds(null);
            if (deviceIds == null || deviceIds.isEmpty()) {
                return new HashMap<>();
            }
            wrapper.in(ChargingPort::getDeviceId, deviceIds);
        }

        List<ChargingPort> ports = portMapper.selectList(wrapper);

        return ports.stream()
                .collect(Collectors.groupingBy(ChargingPort::getStatus, Collectors.counting()));
    }

    /**
     * 解析设备ID集合：非平台管理员时，仅允许查询所属社区下的设备。
     * 返回 null 表示不限（仅平台管理员未指定设备时）；返回空集合表示无权查看任何数据。
     */
    private List<Long> resolveDeviceIds(Long deviceId) {
        if (DataScope.isPlatformAdmin()) {
            return deviceId != null ? List.of(deviceId) : null;
        }
        Long communityId = DataScope.currentCommunityId();
        if (communityId == null) {
            return null;
        }
        List<Long> communityParkingAreaIds = parkingAreaMapper.selectList(
                        new LambdaQueryWrapper<ParkingArea>().eq(ParkingArea::getCommunityId, communityId))
                .stream().map(ParkingArea::getId).toList();
        if (communityParkingAreaIds.isEmpty()) {
            return List.of(-1L);
        }
        List<Long> communityDeviceIds = deviceMapper.selectList(
                        new LambdaQueryWrapper<ChargingDevice>().in(ChargingDevice::getParkingAreaId, communityParkingAreaIds))
                .stream().map(ChargingDevice::getId).toList();
        if (communityDeviceIds.isEmpty()) {
            return List.of(-1L);
        }
        if (deviceId != null) {
            return communityDeviceIds.contains(deviceId) ? List.of(deviceId) : List.of(-1L);
        }
        return communityDeviceIds;
    }

    private void updateDevicePortCount(Long deviceId) {
        Long count = portMapper.selectCount(
                new LambdaQueryWrapper<ChargingPort>()
                        .eq(ChargingPort::getDeviceId, deviceId)
        );
        ChargingDevice device = deviceMapper.selectById(deviceId);
        if (device != null) {
            device.setPortCount(count.intValue());
            deviceMapper.updateById(device);
        }
    }
}
