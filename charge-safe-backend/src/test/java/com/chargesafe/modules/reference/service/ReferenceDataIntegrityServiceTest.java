package com.chargesafe.modules.reference.service;

import com.chargesafe.common.BusinessException;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.DeviceCategory;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.DeviceCategoryMapper;
import com.chargesafe.modules.device.service.impl.DeviceCategoryServiceImpl;
import com.chargesafe.modules.user.entity.SysDept;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysDeptMapper;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.service.impl.SysDeptServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReferenceDataIntegrityServiceTest {

    @Test
    void deletingReferencedDeviceCategoryIsRejected() {
        DeviceCategoryMapper categoryMapper = mock(DeviceCategoryMapper.class);
        ChargingDeviceMapper deviceMapper = mock(ChargingDeviceMapper.class);
        DeviceCategoryServiceImpl service = new DeviceCategoryServiceImpl(categoryMapper, deviceMapper);

        DeviceCategory category = new DeviceCategory();
        category.setId(8L);
        when(categoryMapper.selectById(8L)).thenReturn(category);
        when(deviceMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> service.delete(8L));
        verify(categoryMapper, never()).deleteById(8L);
    }

    @Test
    void deletingDepartmentWithAssignedUsersIsRejected() {
        SysDeptMapper deptMapper = mock(SysDeptMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        SysDeptServiceImpl service = new SysDeptServiceImpl(deptMapper, userMapper);

        SysDept dept = new SysDept();
        dept.setId(9L);
        when(deptMapper.selectById(9L)).thenReturn(dept);
        when(deptMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> service.delete(9L));
        verify(deptMapper, never()).deleteById(9L);
    }

    @Test
    void movingDepartmentUnderItselfIsRejected() {
        SysDeptMapper deptMapper = mock(SysDeptMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        SysDeptServiceImpl service = new SysDeptServiceImpl(deptMapper, userMapper);

        SysDept existing = new SysDept();
        existing.setId(10L);
        existing.setName("运维部");
        existing.setCode("OPS");
        existing.setStatus(1);
        when(deptMapper.selectById(10L)).thenReturn(existing);

        SysDept update = new SysDept();
        update.setParentId(10L);
        update.setName("运维部");
        update.setCode("OPS");
        update.setStatus(1);

        assertThrows(BusinessException.class, () -> service.update(10L, update));
        verify(deptMapper, never()).updateById(any(SysDept.class));
    }
}
