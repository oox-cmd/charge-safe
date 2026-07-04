package com.chargesafe.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.device.dto.DeviceDTO;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final ChargingDeviceMapper deviceMapper;

    @Override
    public PageResult<ChargingDevice> pageList(Long parkingAreaId, PageQuery pageQuery) {
        Page<ChargingDevice> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ChargingDevice> wrapper = new LambdaQueryWrapper<ChargingDevice>()
                .eq(parkingAreaId != null, ChargingDevice::getParkingAreaId, parkingAreaId)
                .orderByDesc(ChargingDevice::getCreatedTime);

        Page<ChargingDevice> result = deviceMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public List<ChargingDevice> listByParkingAreaId(Long parkingAreaId) {
        return deviceMapper.selectList(
                new LambdaQueryWrapper<ChargingDevice>()
                        .eq(parkingAreaId != null, ChargingDevice::getParkingAreaId, parkingAreaId)
                        .eq(ChargingDevice::getStatus, 1)
        );
    }

    @Override
    public ChargingDevice getById(Long id) {
        ChargingDevice device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return device;
    }

    @Override
    public ChargingDevice create(DeviceDTO dto) {
        ChargingDevice device = new ChargingDevice();
        BeanUtils.copyProperties(dto, device);
        device.setStatus(1);
        device.setPortCount(0);
        deviceMapper.insert(device);
        return device;
    }

    @Override
    public ChargingDevice update(DeviceDTO dto) {
        ChargingDevice device = getById(dto.getId());
        BeanUtils.copyProperties(dto, device, "id", "portCount", "status", "createdTime", "updatedTime", "deleted");
        deviceMapper.updateById(device);
        return device;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ChargingDevice device = getById(id);
        device.setStatus(status);
        deviceMapper.updateById(device);
    }
}
