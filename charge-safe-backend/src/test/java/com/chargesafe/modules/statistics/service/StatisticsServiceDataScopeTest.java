package com.chargesafe.modules.statistics.service;

import com.chargesafe.modules.charging.mapper.ChargingOrderMapper;
import com.chargesafe.modules.charging.mapper.ChargingReservationMapper;
import com.chargesafe.modules.community.mapper.CommunityMapper;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.inspection.mapper.SafetyHazardMapper;
import com.chargesafe.modules.notification.mapper.AlarmRecordMapper;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.repair.mapper.RepairTicketMapper;
import com.chargesafe.modules.statistics.service.impl.StatisticsServiceImpl;
import com.chargesafe.modules.statistics.vo.PortStatusStatisticsVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StatisticsServiceDataScopeTest {

    @Test
    void communityWithoutDevicesMustNotFallBackToAllPorts() {
        ChargingPortMapper portMapper = mock(ChargingPortMapper.class);
        ChargingDeviceMapper deviceMapper = mock(ChargingDeviceMapper.class);
        ParkingAreaMapper parkingAreaMapper = mock(ParkingAreaMapper.class);
        when(parkingAreaMapper.selectList(any())).thenReturn(List.of());

        StatisticsServiceImpl service = new StatisticsServiceImpl(
                portMapper,
                deviceMapper,
                parkingAreaMapper,
                mock(CommunityMapper.class),
                mock(ChargingReservationMapper.class),
                mock(ChargingOrderMapper.class),
                mock(RepairTicketMapper.class),
                mock(SafetyHazardMapper.class),
                mock(NotificationMapper.class),
                mock(AlarmRecordMapper.class));

        PortStatusStatisticsVO result = service.getPortStatusStatistics(99L);

        assertEquals(0L, result.getPortCounts().values().stream().mapToLong(Long::longValue).sum());
        verifyNoInteractions(portMapper);
    }
}
