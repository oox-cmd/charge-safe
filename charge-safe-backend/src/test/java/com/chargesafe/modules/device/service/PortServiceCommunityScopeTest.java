package com.chargesafe.modules.device.service;

import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.device.service.impl.PortServiceImpl;
import com.chargesafe.security.LoginUser;
import com.chargesafe.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PortServiceCommunityScopeTest {

    @AfterEach
    void clearUserContext() {
        UserContext.remove();
    }

    @Test
    void statisticsScopeResolvesCommunityThroughParkingAreas() {
        ChargingPortMapper portMapper = mock(ChargingPortMapper.class);
        ChargingDeviceMapper deviceMapper = mock(ChargingDeviceMapper.class);
        ParkingAreaMapper parkingAreaMapper = mock(ParkingAreaMapper.class);
        PortServiceImpl service = new PortServiceImpl(portMapper, deviceMapper, parkingAreaMapper);

        UserContext.setUser(new LoginUser(1L, "admin", "PLATFORM_ADMIN"));
        ParkingArea area = new ParkingArea();
        area.setId(501L);
        area.setCommunityId(99L);
        ChargingDevice device = new ChargingDevice();
        device.setId(601L);
        device.setParkingAreaId(501L);
        ChargingPort port = new ChargingPort();
        port.setStatus("IDLE");
        when(parkingAreaMapper.selectList(any())).thenReturn(List.of(area));
        when(deviceMapper.selectList(any())).thenReturn(List.of(device));
        when(portMapper.selectList(any())).thenReturn(List.of(port));

        assertEquals(1L, service.countByStatus(99L).get("IDLE"));

        // The service must resolve a community through its parking areas before
        // querying devices; the previous implementation skipped this mapper.
        verify(parkingAreaMapper).selectList(any());
    }
}
