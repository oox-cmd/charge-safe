package com.chargesafe.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.DeviceCategory;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.DeviceCategoryMapper;
import com.chargesafe.modules.device.service.DeviceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceCategoryServiceImpl extends ServiceImpl<DeviceCategoryMapper, DeviceCategory> implements DeviceCategoryService {

    private final DeviceCategoryMapper categoryMapper;
    private final ChargingDeviceMapper deviceMapper;

    @Override
    @Transactional
    public DeviceCategory create(DeviceCategory input) {
        normalize(input);
        ensureCodeUnique(input.getCode(), null);
        input.setId(null);
        input.setDeleted(null);
        input.setCreateTime(null);
        input.setUpdateTime(null);
        if (input.getStatus() == null) {
            input.setStatus(1);
        }
        ensureStatusValue(input.getStatus());
        categoryMapper.insert(input);
        return input;
    }

    @Override
    @Transactional
    public DeviceCategory update(Long id, DeviceCategory input) {
        DeviceCategory existing = getRequired(id);
        normalize(input);

        existing.setName(input.getName());
        // 分类编码是稳定的业务标识；前端也不提供编辑入口，避免绕过该约束。
        existing.setPower(input.getPower());
        existing.setDescription(input.getDescription());
        if (input.getStatus() != null) {
            ensureStatusValue(input.getStatus());
            existing.setStatus(input.getStatus());
        }
        categoryMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getRequired(id);
        if (deviceMapper.selectCount(new LambdaQueryWrapper<ChargingDevice>()
                .eq(ChargingDevice::getCategoryId, id)) > 0) {
            throw new BusinessException("设备分类仍被充电设备使用，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    private DeviceCategory getRequired(Long id) {
        DeviceCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return category;
    }

    private void ensureCodeUnique(String code, Long ignoredId) {
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<DeviceCategory>()
                .eq(DeviceCategory::getCode, code)
                .ne(ignoredId != null, DeviceCategory::getId, ignoredId));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST);
        }
    }

    private void ensureStatusValue(Integer status) {
        if (!Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void normalize(DeviceCategory category) {
        category.setName(trim(category.getName()));
        category.setCode(trim(category.getCode()));
        category.setPower(trim(category.getPower()));
        category.setDescription(trim(category.getDescription()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
