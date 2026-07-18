package com.chargesafe.modules.simulator.service;

import com.chargesafe.modules.charging.mapper.ChargingOrderMapper;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.notification.entity.AlarmRecord;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.mapper.AlarmRecordMapper;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.repair.entity.RepairReport;
import com.chargesafe.modules.repair.mapper.RepairReportMapper;
import com.chargesafe.modules.simulator.service.impl.SimulatorServiceImpl;
import com.chargesafe.modules.user.entity.SysRole;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.entity.SysUserRole;
import com.chargesafe.modules.user.mapper.SysRoleMapper;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.mapper.SysUserRoleMapper;
import com.chargesafe.security.LoginUser;
import com.chargesafe.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimulatorServiceCommunityNotificationTest {

    @AfterEach
    void clearUserContext() {
        UserContext.remove();
    }

    @Test
    void simulatedFaultNotifiesOnlyPropertyUsersOfThePortCommunityAndRecordsActor() {
        ChargingPortMapper portMapper = mock(ChargingPortMapper.class);
        ChargingDeviceMapper deviceMapper = mock(ChargingDeviceMapper.class);
        ParkingAreaMapper parkingAreaMapper = mock(ParkingAreaMapper.class);
        RepairReportMapper reportMapper = mock(RepairReportMapper.class);
        NotificationMapper notificationMapper = mock(NotificationMapper.class);
        ChargingOrderMapper orderMapper = mock(ChargingOrderMapper.class);
        AlarmRecordMapper alarmRecordMapper = mock(AlarmRecordMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
        SimulatorServiceImpl service = new SimulatorServiceImpl(portMapper, deviceMapper, parkingAreaMapper,
                reportMapper, notificationMapper, orderMapper, alarmRecordMapper,
                userMapper, roleMapper, userRoleMapper);

        ChargingPort port = new ChargingPort();
        port.setId(30L);
        port.setDeviceId(20L);
        port.setPortName("A-03");
        when(portMapper.selectById(30L)).thenReturn(port);
        ChargingDevice device = new ChargingDevice();
        device.setId(20L);
        device.setParkingAreaId(10L);
        when(deviceMapper.selectById(20L)).thenReturn(device);
        ParkingArea area = new ParkingArea();
        area.setId(10L);
        area.setCommunityId(9L);
        when(parkingAreaMapper.selectById(10L)).thenReturn(area);
        when(reportMapper.insert(any(RepairReport.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, RepairReport.class).setId(40L);
            return 1;
        });
        SysRole propertyRole = new SysRole();
        propertyRole.setId(2L);
        propertyRole.setRoleCode("PROPERTY_ADMIN");
        propertyRole.setStatus(1);
        when(roleMapper.selectOne(any())).thenReturn(propertyRole);
        SysUserRole propertyBinding = new SysUserRole();
        propertyBinding.setUserId(88L);
        propertyBinding.setRoleId(2L);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(propertyBinding));
        SysUser propertyUser = new SysUser();
        propertyUser.setId(88L);
        propertyUser.setCommunityId(9L);
        propertyUser.setStatus(1);
        when(userMapper.selectList(any())).thenReturn(List.of(propertyUser));
        UserContext.setUser(new LoginUser(77L, "platform-admin", "PLATFORM_ADMIN"));

        service.simulatePortFault(30L);

        ArgumentCaptor<RepairReport> reportCaptor = ArgumentCaptor.forClass(RepairReport.class);
        verify(reportMapper).insert(reportCaptor.capture());
        assertEquals(77L, reportCaptor.getValue().getReporterId());

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(notificationCaptor.capture());
        assertEquals(88L, notificationCaptor.getValue().getUserId());

        ArgumentCaptor<AlarmRecord> alarmCaptor = ArgumentCaptor.forClass(AlarmRecord.class);
        verify(alarmRecordMapper).insert(alarmCaptor.capture());
        assertTrue(alarmCaptor.getValue().getAlarmContent().contains("模拟"));
    }
}
