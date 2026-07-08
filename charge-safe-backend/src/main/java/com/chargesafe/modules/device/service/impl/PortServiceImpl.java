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

    @Override
    public PageResult<ChargingPort> pageList(Long deviceId, String status, PageQuery pageQuery) {
        Page<ChargingPort> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ChargingPort> wrapper = new LambdaQueryWrapper<ChargingPort>()
                .eq(deviceId != null, ChargingPort::getDeviceId, deviceId)
                .eq(StringUtils.hasText(status), ChargingPort::getStatus, status)
                .orderByAsc(ChargingPort::getPortNumber);

        Page<ChargingPort> result = portMapper.selectPage(page, wrapper);

        // 填充设备名称
        Set<Long> deviceIds = result.getRecords().stream()
                .map(ChargingPort::getDeviceId)
                .collect(Collectors.toSet());
        if (!deviceIds.isEmpty()) {
            Map<Long, String> deviceNameMap = deviceMapper.selectBatchIds(deviceIds).stream()
                    .collect(Collectors.toMap(ChargingDevice::getId, ChargingDevice::getDeviceName));
            result.getRecords().forEach(p -> p.setDeviceName(deviceNameMap.get(p.getDeviceId())));
        }

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public List<ChargingPort> listByDeviceId(Long deviceId) {
        return portMapper.selectList(
                new LambdaQueryWrapper<ChargingPort>()
                        .eq(ChargingPort::getDeviceId, deviceId)
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

        List<ChargingPort> ports = portMapper.selectList(wrapper);

        return ports.stream()
                .collect(Collectors.groupingBy(ChargingPort::getStatus, Collectors.counting()));
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
