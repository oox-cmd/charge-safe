package com.chargesafe.modules.repair.service;

import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.repair.dto.RepairReportDTO;
import com.chargesafe.modules.repair.dto.TicketDispatchDTO;
import com.chargesafe.modules.repair.entity.RepairReport;
import com.chargesafe.modules.repair.entity.RepairTicket;
import com.chargesafe.modules.repair.enums.ReportStatus;
import com.chargesafe.modules.repair.enums.TicketStatus;
import com.chargesafe.modules.repair.mapper.RepairReportMapper;
import com.chargesafe.modules.repair.mapper.RepairTicketLogMapper;
import com.chargesafe.modules.repair.mapper.RepairTicketMapper;
import com.chargesafe.modules.repair.service.impl.RepairServiceImpl;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepairServiceDataScopeTest {

    private final RepairReportMapper reportMapper = mock(RepairReportMapper.class);
    private final RepairTicketMapper ticketMapper = mock(RepairTicketMapper.class);
    private final ChargingDeviceMapper deviceMapper = mock(ChargingDeviceMapper.class);
    private final ChargingPortMapper portMapper = mock(ChargingPortMapper.class);
    private final ParkingAreaMapper parkingAreaMapper = mock(ParkingAreaMapper.class);
    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
    private final RepairServiceImpl service = new RepairServiceImpl(
            reportMapper,
            ticketMapper,
            mock(RepairTicketLogMapper.class),
            deviceMapper,
            portMapper,
            parkingAreaMapper,
            mock(NotificationMapper.class),
            userMapper,
            roleMapper,
            userRoleMapper);

    @AfterEach
    void clearUserContext() {
        UserContext.remove();
    }

    @Test
    void residentCannotCreateReportForDeviceOutsideOwnCommunity() {
        LoginUser resident = new LoginUser(101L, "resident", "RESIDENT");
        resident.setCommunityId(1L);
        UserContext.setUser(resident);

        ChargingDevice device = new ChargingDevice();
        device.setId(10L);
        device.setParkingAreaId(20L);
        ParkingArea area = new ParkingArea();
        area.setId(20L);
        area.setCommunityId(2L);
        when(deviceMapper.selectById(10L)).thenReturn(device);
        when(parkingAreaMapper.selectById(20L)).thenReturn(area);

        RepairReportDTO dto = new RepairReportDTO();
        dto.setDeviceId(10L);
        dto.setFaultType("无法充电");
        dto.setFaultDesc("端口无法正常开始充电，指示灯持续闪烁。");

        assertThrows(BusinessException.class, () -> service.createReport(dto));
        verify(reportMapper, never()).insert(any(RepairReport.class));
    }

    @Test
    void residentCannotReadAnotherResidentsReport() {
        LoginUser resident = new LoginUser(101L, "resident", "RESIDENT");
        resident.setCommunityId(1L);
        UserContext.setUser(resident);

        RepairReport report = new RepairReport();
        report.setId(30L);
        report.setReporterId(202L);
        when(reportMapper.selectById(30L)).thenReturn(report);

        assertThrows(BusinessException.class, () -> service.getMyReportById(30L));
    }

    @Test
    void platformAdminCanListReportsWithoutACommunityFilter() {
        UserContext.setUser(new LoginUser(1L, "admin", "PLATFORM_ADMIN"));
        PageQuery query = new PageQuery();
        query.setPage(1);
        query.setSize(10);
        when(reportMapper.selectPage(any(Page.class), any())).thenReturn(new Page<RepairReport>(1, 10));

        service.pageReports(null, null, query);

        verify(reportMapper).selectPage(any(Page.class), any());
    }

    @Test
    void propertyCannotDispatchAnAlreadyDispatchedReport() {
        LoginUser propertyAdmin = new LoginUser(101L, "property", "PROPERTY_ADMIN");
        propertyAdmin.setCommunityId(1L);
        UserContext.setUser(propertyAdmin);

        RepairReport report = new RepairReport();
        report.setId(30L);
        report.setCommunityId(1L);
        report.setStatus(ReportStatus.DISPATCHED.getCode());
        when(reportMapper.selectById(30L)).thenReturn(report);

        TicketDispatchDTO dto = new TicketDispatchDTO();
        dto.setReportId(30L);
        dto.setAssigneeId(202L);

        assertThrows(BusinessException.class, () -> service.dispatchTicket(dto));
        verify(ticketMapper, never()).insert(any(RepairTicket.class));
    }

    @Test
    void propertyCanRedispatchRejectedReportOnlyOnce() {
        LoginUser propertyAdmin = new LoginUser(101L, "property", "PROPERTY_ADMIN");
        propertyAdmin.setCommunityId(1L);
        UserContext.setUser(propertyAdmin);

        RepairReport report = new RepairReport();
        report.setId(30L);
        report.setCommunityId(1L);
        report.setReporterId(303L);
        report.setStatus(ReportStatus.REJECTED.getCode());
        when(reportMapper.selectById(30L)).thenReturn(report);
        when(reportMapper.transitionToDispatched(30L, ReportStatus.DISPATCHED.getCode())).thenReturn(1);
        stubActiveMaintainer(202L, 1L);

        TicketDispatchDTO dto = new TicketDispatchDTO();
        dto.setReportId(30L);
        dto.setAssigneeId(202L);

        service.dispatchTicket(dto);

        assertEquals(ReportStatus.DISPATCHED.getCode(), report.getStatus());
        verify(ticketMapper).insert(any(RepairTicket.class));
    }

    @Test
    void propertyCannotDispatchWhenAtomicStatusTransitionLosesRace() {
        LoginUser propertyAdmin = new LoginUser(101L, "property", "PROPERTY_ADMIN");
        propertyAdmin.setCommunityId(1L);
        UserContext.setUser(propertyAdmin);

        RepairReport report = new RepairReport();
        report.setId(30L);
        report.setCommunityId(1L);
        report.setStatus(ReportStatus.PENDING.getCode());
        when(reportMapper.selectById(30L)).thenReturn(report);
        when(reportMapper.transitionToDispatched(30L, ReportStatus.DISPATCHED.getCode())).thenReturn(0);
        stubActiveMaintainer(202L, 1L);

        TicketDispatchDTO dto = new TicketDispatchDTO();
        dto.setReportId(30L);
        dto.setAssigneeId(202L);

        assertThrows(BusinessException.class, () -> service.dispatchTicket(dto));
        verify(ticketMapper, never()).insert(any(RepairTicket.class));
    }

    @Test
    void propertyCannotDispatchToAUserOutsideTheReportCommunity() {
        LoginUser propertyAdmin = new LoginUser(101L, "property", "PROPERTY_ADMIN");
        propertyAdmin.setCommunityId(1L);
        UserContext.setUser(propertyAdmin);

        RepairReport report = new RepairReport();
        report.setId(30L);
        report.setCommunityId(1L);
        report.setStatus(ReportStatus.PENDING.getCode());
        when(reportMapper.selectById(30L)).thenReturn(report);
        stubActiveMaintainer(202L, 2L);

        TicketDispatchDTO dto = new TicketDispatchDTO();
        dto.setReportId(30L);
        dto.setAssigneeId(202L);

        assertThrows(BusinessException.class, () -> service.dispatchTicket(dto));
        verify(reportMapper, never()).transitionToDispatched(any(), any());
        verify(ticketMapper, never()).insert(any(RepairTicket.class));
    }

    @Test
    void propertyAcceptanceSynchronizesReportAndPortState() {
        LoginUser propertyAdmin = new LoginUser(101L, "property", "PROPERTY_ADMIN");
        propertyAdmin.setCommunityId(1L);
        UserContext.setUser(propertyAdmin);

        RepairTicket ticket = new RepairTicket();
        ticket.setId(20L);
        ticket.setReportId(30L);
        ticket.setCommunityId(1L);
        ticket.setPortId(40L);
        ticket.setAssigneeId(202L);
        ticket.setStatus(TicketStatus.WAITING_ACCEPTANCE.getCode());
        RepairReport report = new RepairReport();
        report.setId(30L);
        report.setReporterId(303L);
        report.setStatus(ReportStatus.DISPATCHED.getCode());
        ChargingPort port = new ChargingPort();
        port.setId(40L);
        port.setStatus(PortStatus.MAINTENANCE.getCode());

        when(ticketMapper.selectById(20L)).thenReturn(ticket);
        when(reportMapper.selectById(30L)).thenReturn(report);
        when(portMapper.selectById(40L)).thenReturn(port);

        service.acceptTicketByProperty(20L);

        assertEquals(TicketStatus.FINISHED.getCode(), ticket.getStatus());
        assertEquals(ReportStatus.FINISHED.getCode(), report.getStatus());
        assertEquals(PortStatus.IDLE.getCode(), port.getStatus());
        verify(ticketMapper).updateById(ticket);
        verify(reportMapper).updateById(report);
        verify(portMapper).updateById(port);
    }

    private void stubActiveMaintainer(Long userId, Long communityId) {
        SysRole maintainerRole = new SysRole();
        maintainerRole.setId(3L);
        maintainerRole.setRoleCode("MAINTAINER");
        maintainerRole.setStatus(1);
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(3L);
        SysUser maintainer = new SysUser();
        maintainer.setId(userId);
        maintainer.setUsername("maintainer");
        maintainer.setStatus(1);
        maintainer.setCommunityId(communityId);

        when(roleMapper.selectOne(any())).thenReturn(maintainerRole);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(userMapper.selectList(any())).thenReturn(communityId.equals(1L) ? List.of(maintainer) : List.of());
    }
}
