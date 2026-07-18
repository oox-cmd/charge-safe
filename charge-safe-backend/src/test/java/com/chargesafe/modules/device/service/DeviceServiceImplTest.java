package com.chargesafe.modules.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.PageQuery;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.DeviceCategoryMapper;
import com.chargesafe.modules.device.service.impl.DeviceServiceImpl;
import com.chargesafe.security.LoginUser;
import com.chargesafe.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceServiceImplTest {

    private final ChargingDeviceMapper deviceMapper = mock(ChargingDeviceMapper.class);
    private final ParkingAreaMapper parkingAreaMapper = mock(ParkingAreaMapper.class);
    private final DeviceCategoryMapper categoryMapper = mock(DeviceCategoryMapper.class);
    private final DeviceServiceImpl service = new DeviceServiceImpl(deviceMapper, parkingAreaMapper, categoryMapper);

    @AfterEach
    void clearUserContext() {
        UserContext.remove();
    }

    @Test
    void pageListAllowsDevicesWithoutCategory() {
        UserContext.setUser(new LoginUser(1L, "admin", "PLATFORM_ADMIN"));
        ChargingDevice device = new ChargingDevice();
        device.setId(10L);

        when(deviceMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<ChargingDevice> page = invocation.getArgument(0);
            page.setRecords(List.of(device));
            page.setTotal(1);
            return page;
        });
        PageQuery query = new PageQuery();
        query.setPage(1);
        query.setSize(10);

        assertDoesNotThrow(() -> service.pageList(null, query));
        assertNull(device.getCategoryName());
        verify(categoryMapper, never()).selectBatchIds(any());
    }
}
