package com.chargesafe.modules.resident.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.CommunityMapper;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.resident.service.impl.ResidentDashboardServiceImpl;
import com.chargesafe.modules.resident.vo.ResidentDashboardVO;
import com.chargesafe.security.LoginUser;
import com.chargesafe.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResidentDashboardServiceTest {

    private static final long COMMUNITY_ID = 10L;
    private static final long PARKING_AREA_ID = 20L;
    private static final long DEVICE_ID = 30L;

    @Mock
    private CommunityMapper communityMapper;
    @Mock
    private ParkingAreaMapper parkingAreaMapper;
    @Mock
    private ChargingDeviceMapper deviceMapper;
    @Mock
    private ChargingPortMapper portMapper;

    private ResidentDashboardService service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "resident-dashboard-test"),
                ParkingArea.class);
        service = new ResidentDashboardServiceImpl(
                communityMapper, parkingAreaMapper, deviceMapper, portMapper);
        UserContext.remove();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void returnsOnlyCurrentUsersCommunityAndMapsAllNinePortStatuses() {
        LoginUser resident = new LoginUser(5L, "resident01", "RESIDENT");
        resident.setCommunityId(COMMUNITY_ID);
        UserContext.setUser(resident);

        Community community = new Community();
        community.setId(COMMUNITY_ID);
        community.setName("阳光花园小区");
        when(communityMapper.selectById(COMMUNITY_ID)).thenReturn(community);

        ParkingArea area = new ParkingArea();
        area.setId(PARKING_AREA_ID);
        area.setCommunityId(COMMUNITY_ID);
        area.setName("A栋充电车棚");
        area.setLocation("小区东门入口处");
        when(parkingAreaMapper.selectList(any())).thenReturn(List.of(area));

        ChargingDevice device = new ChargingDevice();
        device.setId(DEVICE_ID);
        device.setParkingAreaId(PARKING_AREA_ID);
        when(deviceMapper.selectList(any())).thenReturn(List.of(device));
        when(portMapper.selectList(any())).thenReturn(List.of(
                port(1L, PortStatus.IDLE),
                port(2L, PortStatus.RESERVED),
                port(3L, PortStatus.CHARGING),
                port(4L, PortStatus.COMPLETED_WAITING_PICKUP),
                port(5L, PortStatus.OVERTIME_OCCUPIED),
                port(6L, PortStatus.FAULT),
                port(7L, PortStatus.MAINTENANCE),
                port(8L, PortStatus.DISABLED),
                port(9L, PortStatus.OFFLINE)
        ));

        ResidentDashboardVO result = service.getCurrentDashboard();

        assertThat(result.getCommunityId()).isEqualTo(COMMUNITY_ID);
        assertThat(result.getCommunityName()).isEqualTo("阳光花园小区");
        assertThat(result.getTotalParkingAreas()).isEqualTo(1);
        assertThat(result.getTotalPorts()).isEqualTo(9);
        assertThat(result.getIdleCount()).isEqualTo(1);
        assertThat(result.getReservedCount()).isEqualTo(1);
        assertThat(result.getChargingCount()).isEqualTo(1);
        assertThat(result.getFaultCount()).isEqualTo(1);
        assertThat(result.getMaintenanceCount()).isEqualTo(1);
        assertThat(result.getUnavailableCount()).isEqualTo(4);
        assertThat(result.getAvailableForReservation()).isTrue();
        assertThat(result.getParkingAreas()).singleElement().satisfies(item -> {
            assertThat(item.getParkingAreaId()).isEqualTo(PARKING_AREA_ID);
            assertThat(item.getParkingAreaName()).isEqualTo("A栋充电车棚");
            assertThat(item.getLocationDescription()).isEqualTo("小区东门入口处");
            assertThat(item.getTotalPorts()).isEqualTo(9);
            assertThat(item.getUnavailableCount()).isEqualTo(4);
        });

        ArgumentCaptor<LambdaQueryWrapper<ParkingArea>> areaQuery = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(parkingAreaMapper).selectList(areaQuery.capture());
        assertThat(areaQuery.getValue().getSqlSegment()).contains("community_id");
        assertThat(areaQuery.getValue().getParamNameValuePairs().values()).contains(COMMUNITY_ID);
    }

    @Test
    void returnsExplicitEmptyResultWithoutQueryingAnyCommunityWhenUserHasNoCommunity() {
        UserContext.setUser(new LoginUser(5L, "resident01", "RESIDENT"));

        ResidentDashboardVO result = service.getCurrentDashboard();

        assertThat(result.getCommunityId()).isNull();
        assertThat(result.getCommunityName()).isNull();
        assertThat(result.getTotalParkingAreas()).isZero();
        assertThat(result.getTotalPorts()).isZero();
        assertThat(result.getAvailableForReservation()).isFalse();
        assertThat(result.getParkingAreas()).isEmpty();
        verifyNoInteractions(communityMapper, parkingAreaMapper, deviceMapper, portMapper);
    }

    @Test
    void neverUsesARequestSuppliedCommunityBecauseTheServiceAcceptsNoCommunityParameter() {
        assertThat(ResidentDashboardService.class.getDeclaredMethods())
                .allSatisfy(method -> assertThat(method.getParameterCount()).isZero());
        verify(parkingAreaMapper, never()).selectList(any());
    }

    private ChargingPort port(Long id, PortStatus status) {
        ChargingPort port = new ChargingPort();
        port.setId(id);
        port.setDeviceId(DEVICE_ID);
        port.setStatus(status.getCode());
        return port;
    }
}
