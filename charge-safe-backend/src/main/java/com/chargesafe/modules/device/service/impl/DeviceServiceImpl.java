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
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.security.DataScope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final ChargingDeviceMapper deviceMapper;
    private final ParkingAreaMapper parkingAreaMapper;

    @Override
    public PageResult<ChargingDevice> pageList(Long parkingAreaId, PageQuery pageQuery) {
        List<Long> scopedParkingAreaIds = resolveParkingAreaIds(parkingAreaId);
        Page<ChargingDevice> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ChargingDevice> wrapper = new LambdaQueryWrapper<ChargingDevice>()
                .in(scopedParkingAreaIds != null, ChargingDevice::getParkingAreaId, scopedParkingAreaIds)
                .orderByDesc(ChargingDevice::getCreatedTime);

        Page<ChargingDevice> result = deviceMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public List<ChargingDevice> listByParkingAreaId(Long parkingAreaId) {
        List<Long> scopedParkingAreaIds = resolveParkingAreaIds(parkingAreaId);
        return deviceMapper.selectList(
                new LambdaQueryWrapper<ChargingDevice>()
                        .in(scopedParkingAreaIds != null, ChargingDevice::getParkingAreaId, scopedParkingAreaIds)
                        .eq(ChargingDevice::getStatus, 1)
        );
    }

    /**
     * 解析车棚ID集合：非平台管理员时，仅允许查询所属社区下的车棚。
     * 返回 null 表示不限（仅平台管理员未指定车棚时）；返回空集合表示无权查看任何数据。
     */
    private List<Long> resolveParkingAreaIds(Long parkingAreaId) {
        if (DataScope.isPlatformAdmin()) {
            return parkingAreaId != null ? List.of(parkingAreaId) : null;
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
        if (parkingAreaId != null) {
            return communityParkingAreaIds.contains(parkingAreaId) ? List.of(parkingAreaId) : List.of(-1L);
        }
        return communityParkingAreaIds;
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
