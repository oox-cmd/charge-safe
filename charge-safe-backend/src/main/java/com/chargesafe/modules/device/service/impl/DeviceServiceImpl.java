package com.chargesafe.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.device.dto.DeviceDTO;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.DeviceCategory;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.DeviceCategoryMapper;
import com.chargesafe.modules.device.service.DeviceService;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.security.DataScope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final ChargingDeviceMapper deviceMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final DeviceCategoryMapper deviceCategoryMapper;

    @Override
    public PageResult<ChargingDevice> pageList(Long parkingAreaId, PageQuery pageQuery) {
        Page<ChargingDevice> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ChargingDevice> wrapper = new LambdaQueryWrapper<ChargingDevice>()
                .eq(parkingAreaId != null, ChargingDevice::getParkingAreaId, parkingAreaId)
                .orderByDesc(ChargingDevice::getCreatedTime);
        if (!DataScope.isPlatformAdmin()) {
            List<Long> areaIds = scopedParkingAreaIds();
            if (areaIds.isEmpty()) {
                return new PageResult<>(0, pageQuery.getPage(), pageQuery.getSize(), List.of());
            }
            wrapper.in(ChargingDevice::getParkingAreaId, areaIds);
        }

        Page<ChargingDevice> result = deviceMapper.selectPage(page, wrapper);

        // Fill parkingAreaName
        Set<Long> areaIds = result.getRecords().stream().map(ChargingDevice::getParkingAreaId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> areaNameMap = areaIds.isEmpty() ? Map.of() : parkingAreaMapper.selectBatchIds(areaIds).stream().collect(Collectors.toMap(ParkingArea::getId, ParkingArea::getName));
        result.getRecords().forEach(d -> {
            if (d.getParkingAreaId() != null) {
                d.setParkingAreaName(areaNameMap.get(d.getParkingAreaId()));
            }
        });

        Set<Long> categoryIds = result.getRecords().stream().map(ChargingDevice::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = categoryIds.isEmpty() ? Map.of() : deviceCategoryMapper.selectBatchIds(categoryIds)
                .stream().collect(Collectors.toMap(DeviceCategory::getId, DeviceCategory::getName));
        result.getRecords().forEach(d -> {
            if (d.getCategoryId() != null) {
                d.setCategoryName(categoryNameMap.get(d.getCategoryId()));
            }
        });

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public List<ChargingDevice> listByParkingAreaId(Long parkingAreaId) {
        if (parkingAreaId != null) {
            assertParkingAreaVisible(parkingAreaId);
        }
        LambdaQueryWrapper<ChargingDevice> wrapper = new LambdaQueryWrapper<ChargingDevice>()
                .eq(parkingAreaId != null, ChargingDevice::getParkingAreaId, parkingAreaId)
                .eq(ChargingDevice::getStatus, 1);
        if (!DataScope.isPlatformAdmin()) {
            List<Long> areaIds = scopedParkingAreaIds();
            if (areaIds.isEmpty()) {
                return List.of();
            }
            wrapper.in(ChargingDevice::getParkingAreaId, areaIds);
        }
        return deviceMapper.selectList(wrapper);
    }

    @Override
    public ChargingDevice getById(Long id) {
        ChargingDevice device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        assertParkingAreaVisible(device.getParkingAreaId());
        return device;
    }

    @Override
    public ChargingDevice create(DeviceDTO dto) {
        assertParkingAreaVisible(dto.getParkingAreaId());
        ensureCategoryAvailable(dto.getCategoryId());
        ChargingDevice device = new ChargingDevice();
        BeanUtils.copyProperties(dto, device);
        device.setStatus(1);
        device.setPortCount(0);
        deviceMapper.insert(device);
        return device;
    }

    @Override
    public ChargingDevice update(DeviceDTO dto) {
        ensureCategoryAvailable(dto.getCategoryId());
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

    private void ensureCategoryAvailable(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        DeviceCategory category = deviceCategoryMapper.selectById(categoryId);
        if (category == null || !Integer.valueOf(1).equals(category.getStatus())) {
            throw new BusinessException("设备分类不存在或已停用");
        }
    }

    private void assertParkingAreaVisible(Long parkingAreaId) {
        if (DataScope.isPlatformAdmin()) {
            return;
        }
        ParkingArea parkingArea = parkingAreaMapper.selectById(parkingAreaId);
        if (parkingArea == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(parkingArea.getCommunityId(), DataScope.currentCommunityId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private List<Long> scopedParkingAreaIds() {
        Long communityId = DataScope.currentCommunityId();
        if (communityId == null) {
            return List.of();
        }
        return parkingAreaMapper.selectList(new LambdaQueryWrapper<ParkingArea>()
                        .eq(ParkingArea::getCommunityId, communityId))
                .stream().map(ParkingArea::getId).toList();
    }
}
