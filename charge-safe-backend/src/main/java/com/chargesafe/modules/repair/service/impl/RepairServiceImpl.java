package com.chargesafe.modules.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.repair.dto.RepairReportDTO;
import com.chargesafe.modules.repair.dto.TicketDispatchDTO;
import com.chargesafe.modules.repair.dto.TicketFinishDTO;
import com.chargesafe.modules.repair.entity.RepairReport;
import com.chargesafe.modules.repair.entity.RepairTicket;
import com.chargesafe.modules.repair.entity.RepairTicketLog;
import com.chargesafe.modules.repair.enums.ReportStatus;
import com.chargesafe.modules.repair.enums.TicketStatus;
import com.chargesafe.modules.repair.mapper.RepairReportMapper;
import com.chargesafe.modules.repair.mapper.RepairTicketLogMapper;
import com.chargesafe.modules.repair.mapper.RepairTicketMapper;
import com.chargesafe.modules.repair.service.RepairService;
import com.chargesafe.modules.repair.vo.RepairAssigneeVO;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.entity.SysUserRole;
import com.chargesafe.modules.user.entity.SysRole;
import com.chargesafe.modules.user.mapper.SysRoleMapper;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.mapper.SysUserRoleMapper;
import com.chargesafe.security.DataScope;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairServiceImpl implements RepairService {

    private final RepairReportMapper reportMapper;
    private final RepairTicketMapper ticketMapper;
    private final RepairTicketLogMapper ticketLogMapper;
    private final ChargingDeviceMapper deviceMapper;
    private final ChargingPortMapper portMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final NotificationMapper notificationMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    private static final AtomicLong TICKET_SEQ = new AtomicLong(1);

    @Override
    @Transactional
    public RepairReport createReport(RepairReportDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (!DataScope.isResident()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        ChargingDevice device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        ParkingArea parkingArea = parkingAreaMapper.selectById(device.getParkingAreaId());
        if (parkingArea == null || parkingArea.getCommunityId() == null) {
            throw new BusinessException("设备未关联有效社区");
        }
        Long communityId = parkingArea.getCommunityId();
        if (!communityId.equals(DataScope.currentCommunityId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (dto.getPortId() != null) {
            ChargingPort port = portMapper.selectById(dto.getPortId());
            if (port == null || !device.getId().equals(port.getDeviceId())) {
                throw new BusinessException("充电端口与设备不匹配");
            }
        }

        RepairReport report = new RepairReport();
        report.setReporterId(userId);
        report.setCommunityId(communityId);
        report.setDeviceId(dto.getDeviceId());
        report.setPortId(dto.getPortId());
        report.setFaultType(dto.getFaultType());
        report.setFaultDesc(dto.getFaultDesc());
        report.setFaultImages(dto.getFaultImages());
        report.setStatus(ReportStatus.PENDING.getCode());
        reportMapper.insert(report);

        // 通知物业管理员
        if (communityId != null) {
            createNotificationForProperty(communityId, "新报修通知",
                    "有新的设备报修待处理，请及时查看。", "REPAIR", "REPAIR_REPORT", report.getId());
        }

        return report;
    }

    @Override
    public PageResult<RepairReport> pageReports(Long communityId, String status, PageQuery pageQuery) {
        Page<RepairReport> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        Long scopedCommunityId = DataScope.isPlatformAdmin() ? communityId : DataScope.currentCommunityId();
        if (!DataScope.isPlatformAdmin() && scopedCommunityId == null) {
            return new PageResult<>(0, pageQuery.getPage(), pageQuery.getSize(), List.of());
        }
        LambdaQueryWrapper<RepairReport> wrapper = new LambdaQueryWrapper<RepairReport>()
                .eq(scopedCommunityId != null, RepairReport::getCommunityId, scopedCommunityId)
                .eq(StringUtils.hasText(status), RepairReport::getStatus, status)
                .orderByDesc(RepairReport::getCreatedTime);

        Page<RepairReport> result = reportMapper.selectPage(page, wrapper);
        enrichReports(result.getRecords());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public PageResult<RepairReport> pageMyReports(String status, PageQuery pageQuery) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (!DataScope.isResident()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        Page<RepairReport> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        Page<RepairReport> result = reportMapper.selectPage(page, new LambdaQueryWrapper<RepairReport>()
                .eq(RepairReport::getReporterId, userId)
                .eq(StringUtils.hasText(status), RepairReport::getStatus, status)
                .orderByDesc(RepairReport::getCreatedTime));
        enrichReports(result.getRecords());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    private void enrichReports(List<RepairReport> reports) {
        List<Long> reporterIds = reports.stream().map(RepairReport::getReporterId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> reporterMap = reporterIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(reporterIds).stream().collect(Collectors.toMap(SysUser::getId, u -> u.getNickname() != null ? u.getNickname() : u.getUsername()));

        List<Long> deviceIds = reports.stream().map(RepairReport::getDeviceId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> deviceMap = deviceIds.isEmpty() ? Map.of() :
                deviceMapper.selectBatchIds(deviceIds).stream().collect(Collectors.toMap(ChargingDevice::getId, d -> d.getDeviceName() != null ? d.getDeviceName() : d.getDeviceCode()));

        for (RepairReport report : reports) {
            if (report.getReporterId() != null) {
                report.setReporterName(reporterMap.get(report.getReporterId()));
            }
            if (report.getDeviceId() != null) {
                report.setDeviceName(deviceMap.get(report.getDeviceId()));
            }
        }

    }

    @Override
    public RepairReport getReportById(Long id) {
        RepairReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        assertReportVisibleToManager(report);
        return report;
    }

    @Override
    public RepairReport getMyReportById(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (!DataScope.isResident()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        RepairReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!userId.equals(report.getReporterId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        enrichReports(List.of(report));
        return report;
    }

    @Override
    @Transactional
    public RepairTicket dispatchTicket(TicketDispatchDTO dto) {
        assertPropertyManager();
        Long userId = UserContext.getUserId();
        RepairReport report = getReportById(dto.getReportId());

        if (!Set.of(ReportStatus.PENDING.getCode(), ReportStatus.REJECTED.getCode()).contains(report.getStatus())) {
            throw new BusinessException(ResultCode.TICKET_STATUS_ERROR);
        }
        assertDispatchableMaintainer(report, dto.getAssigneeId());

        // 只允许待处理或已驳回的报修单派单，条件更新避免并发重复创建工单。
        int transitioned = reportMapper.transitionToDispatched(report.getId(), ReportStatus.DISPATCHED.getCode());
        if (transitioned != 1) {
            throw new BusinessException(ResultCode.TICKET_STATUS_ERROR);
        }

        report.setStatus(ReportStatus.DISPATCHED.getCode());

        // 创建维修工单
        RepairTicket ticket = new RepairTicket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setReportId(dto.getReportId());
        ticket.setCommunityId(report.getCommunityId());
        ticket.setDeviceId(report.getDeviceId());
        ticket.setPortId(report.getPortId());
        ticket.setDispatcherId(userId);
        ticket.setAssigneeId(dto.getAssigneeId());
        ticket.setFaultDesc(report.getFaultDesc());
        ticket.setRepairRequirement(dto.getRepairRequirement());
        ticket.setStatus(TicketStatus.DISPATCHED.getCode());
        ticket.setDispatchTime(LocalDateTime.now());
        ticketMapper.insert(ticket);

        // 记录工单日志
        saveTicketLog(ticket.getId(), userId, "DISPATCH", "派发工单", null, TicketStatus.DISPATCHED.getCode());

        // 通知运维人员
        createNotification(dto.getAssigneeId(), "工单派发通知",
                "您有新的维修工单待处理，工单号：" + ticket.getTicketNo(),
                "REPAIR", "REPAIR_TICKET", ticket.getId());
        createNotification(report.getReporterId(), "报修已派单",
                "您提交的报修已派发维修人员处理。", "REPAIR", "REPAIR_REPORT", report.getId());

        return ticket;
    }

    @Override
    public void acceptTicket(Long ticketId) {
        Long userId = UserContext.getUserId();
        RepairTicket ticket = getTicketById(ticketId);

        if (!ticket.getAssigneeId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!TicketStatus.DISPATCHED.getCode().equals(ticket.getStatus())) {
            throw new BusinessException(ResultCode.TICKET_STATUS_ERROR);
        }

        ticket.setStatus(TicketStatus.PROCESSING.getCode());
        ticket.setAcceptTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        updateReportStatus(ticket.getReportId(), ReportStatus.PROCESSING);

        saveTicketLog(ticketId, userId, "ACCEPT", "接单处理", TicketStatus.DISPATCHED.getCode(), TicketStatus.PROCESSING.getCode());
    }

    @Override
    public void startTicket(Long ticketId) {
        // startTicket 和 acceptTicket 合并，接单即开始处理
        acceptTicket(ticketId);
    }

    @Override
    @Transactional
    public void finishTicket(Long ticketId, TicketFinishDTO dto) {
        Long userId = UserContext.getUserId();
        RepairTicket ticket = getTicketById(ticketId);

        if (!ticket.getAssigneeId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!TicketStatus.PROCESSING.getCode().equals(ticket.getStatus())) {
            throw new BusinessException(ResultCode.TICKET_STATUS_ERROR);
        }

        ticket.setStatus(TicketStatus.WAITING_ACCEPTANCE.getCode());
        ticket.setRepairResult(dto.getRepairResult());
        ticket.setRepairImages(dto.getRepairImages());
        ticket.setFinishTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        updateReportStatus(ticket.getReportId(), ReportStatus.WAITING_ACCEPTANCE);

        saveTicketLog(ticketId, userId, "FINISH", "维修完成，等待验收", TicketStatus.PROCESSING.getCode(), TicketStatus.WAITING_ACCEPTANCE.getCode());

        // 通知物业验收
        createNotificationForProperty(ticket.getCommunityId(), "工单验收通知",
                "维修工单" + ticket.getTicketNo() + "已完成维修，请进行验收。",
                "REPAIR", "REPAIR_TICKET", ticketId);
    }

    @Override
    @Transactional
    public void acceptTicketByProperty(Long ticketId) {
        assertPropertyManager();
        Long userId = UserContext.getUserId();
        RepairTicket ticket = getTicketById(ticketId);

        if (!TicketStatus.WAITING_ACCEPTANCE.getCode().equals(ticket.getStatus())) {
            throw new BusinessException(ResultCode.TICKET_STATUS_ERROR);
        }

        ticket.setStatus(TicketStatus.FINISHED.getCode());
        ticketMapper.updateById(ticket);
        updateReportStatus(ticket.getReportId(), ReportStatus.FINISHED);

        saveTicketLog(ticketId, userId, "ACCEPT_REPAIR", "验收通过", TicketStatus.WAITING_ACCEPTANCE.getCode(), TicketStatus.FINISHED.getCode());

        // 如果有端口，恢复端口状态
        if (ticket.getPortId() != null) {
            ChargingPort port = portMapper.selectById(ticket.getPortId());
            if (port != null && PortStatus.MAINTENANCE.getCode().equals(port.getStatus())) {
                port.setStatus(PortStatus.IDLE.getCode());
                portMapper.updateById(port);
            }
        }

        // 通知运维人员
        createNotification(ticket.getAssigneeId(), "工单验收通知",
                "您的维修工单" + ticket.getTicketNo() + "已通过验收。",
                "REPAIR", "REPAIR_TICKET", ticketId);
        notifyReporter(ticket, "报修处理完成", "您提交的报修已处理完成。");
    }

    @Override
    public void rejectTicket(Long ticketId, String reason) {
        assertPropertyManager();
        Long userId = UserContext.getUserId();
        RepairTicket ticket = getTicketById(ticketId);

        if (!TicketStatus.WAITING_ACCEPTANCE.getCode().equals(ticket.getStatus())) {
            throw new BusinessException(ResultCode.TICKET_STATUS_ERROR);
        }

        ticket.setStatus(TicketStatus.REJECTED.getCode());
        ticketMapper.updateById(ticket);
        updateReportStatus(ticket.getReportId(), ReportStatus.REJECTED);

        saveTicketLog(ticketId, userId, "REJECT", "验收不通过：" + reason, TicketStatus.WAITING_ACCEPTANCE.getCode(), TicketStatus.REJECTED.getCode());

        // 通知运维人员
        createNotification(ticket.getAssigneeId(), "工单驳回通知",
                "您的维修工单" + ticket.getTicketNo() + "验收不通过，请重新处理。",
                "REPAIR", "REPAIR_TICKET", ticketId);
    }

    @Override
    public void closeTicket(Long ticketId) {
        assertPropertyManager();
        Long userId = UserContext.getUserId();
        RepairTicket ticket = getTicketById(ticketId);

        if (!TicketStatus.FINISHED.getCode().equals(ticket.getStatus())) {
            throw new BusinessException(ResultCode.TICKET_STATUS_ERROR);
        }

        ticket.setStatus(TicketStatus.CLOSED.getCode());
        ticket.setCloseTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);

        saveTicketLog(ticketId, userId, "CLOSE", "关闭工单", TicketStatus.FINISHED.getCode(), TicketStatus.CLOSED.getCode());
    }

    @Override
    public PageResult<RepairTicket> pageTickets(Long assigneeId, String status, PageQuery pageQuery) {
        Page<RepairTicket> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<RepairTicket> wrapper = new LambdaQueryWrapper<RepairTicket>()
                .eq(StringUtils.hasText(status), RepairTicket::getStatus, status)
                .orderByDesc(RepairTicket::getCreatedTime);

        if (DataScope.isMaintainer()) {
            Long userId = DataScope.currentUserId();
            if (userId == null) {
                return new PageResult<>(0, pageQuery.getPage(), pageQuery.getSize(), List.of());
            }
            wrapper.eq(RepairTicket::getAssigneeId, userId);
        } else if (DataScope.isPlatformAdmin()) {
            wrapper.eq(assigneeId != null, RepairTicket::getAssigneeId, assigneeId);
        } else {
            Long communityId = DataScope.currentCommunityId();
            if (communityId == null) {
                return new PageResult<>(0, pageQuery.getPage(), pageQuery.getSize(), List.of());
            }
            wrapper.eq(RepairTicket::getCommunityId, communityId);
        }

        Page<RepairTicket> result = ticketMapper.selectPage(page, wrapper);

        // Fill portCode and assigneeName
        Set<Long> portIds = result.getRecords().stream().map(RepairTicket::getPortId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = result.getRecords().stream().map(RepairTicket::getAssigneeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> portCodeMap = portIds.isEmpty() ? Map.of() : portMapper.selectBatchIds(portIds).stream().collect(Collectors.toMap(ChargingPort::getId, ChargingPort::getPortName));
        Map<Long, String> userNameMap = userIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));
        result.getRecords().forEach(t -> {
            t.setPortCode(portCodeMap.get(t.getPortId()));
            t.setAssigneeName(userNameMap.get(t.getAssigneeId()));
        });

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public RepairTicket getTicketById(Long id) {
        RepairTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        assertTicketVisible(ticket);
        return ticket;
    }

    @Override
    public List<RepairAssigneeVO> listDispatchableMaintainers(Long reportId) {
        assertPropertyManager();
        RepairReport report = getReportById(reportId);
        return findActiveMaintainers(report.getCommunityId());
    }

    private String generateTicketNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = TICKET_SEQ.getAndIncrement() % 10000;
        return "TK" + timestamp + String.format("%04d", seq);
    }

    private void saveTicketLog(Long ticketId, Long operatorId, String action,
                               String content, String fromStatus, String toStatus) {
        RepairTicketLog log = new RepairTicketLog();
        log.setTicketId(ticketId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setContent(content);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        ticketLogMapper.insert(log);
    }

    private void createNotification(Long userId, String title, String content,
                                    String type, String businessType, Long businessId) {
        if (userId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    private void createNotificationForProperty(Long communityId, String title, String content,
                                               String type, String businessType, Long businessId) {
        if (communityId == null) {
            return;
        }
        SysRole propertyRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, DataScope.ROLE_PROPERTY_ADMIN)
                .eq(SysRole::getStatus, 1));
        if (propertyRole == null) {
            return;
        }
        List<Long> roleUserIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, propertyRole.getId()))
                .stream().map(SysUserRole::getUserId).distinct().toList();
        if (roleUserIds.isEmpty()) {
            return;
        }
        List<SysUser> propertyUsers = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, roleUserIds)
                .eq(SysUser::getCommunityId, communityId)
                .eq(SysUser::getStatus, 1));
        propertyUsers.forEach(user -> createNotification(user.getId(), title, content, type, businessType, businessId));
    }

    private void assertDispatchableMaintainer(RepairReport report, Long assigneeId) {
        boolean dispatchable = findActiveMaintainers(report.getCommunityId()).stream()
                .anyMatch(maintainer -> Objects.equals(maintainer.getId(), assigneeId));
        if (!dispatchable) {
            throw new BusinessException("维修人员不存在、未启用或不属于当前社区");
        }
    }

    private List<RepairAssigneeVO> findActiveMaintainers(Long communityId) {
        if (communityId == null) {
            return List.of();
        }
        SysRole maintainerRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, DataScope.ROLE_MAINTAINER)
                .eq(SysRole::getStatus, 1));
        if (maintainerRole == null) {
            return List.of();
        }
        List<Long> userIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, maintainerRole.getId()))
                .stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return List.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, userIds)
                        .eq(SysUser::getCommunityId, communityId)
                        .eq(SysUser::getStatus, 1))
                .stream()
                .map(user -> {
                    RepairAssigneeVO vo = new RepairAssigneeVO();
                    vo.setId(user.getId());
                    vo.setUsername(user.getUsername());
                    vo.setNickname(user.getNickname());
                    return vo;
                })
                .toList();
    }

    private void notifyReporter(RepairTicket ticket, String title, String content) {
        RepairReport report = reportMapper.selectById(ticket.getReportId());
        if (report != null) {
            createNotification(report.getReporterId(), title, content, "REPAIR", "REPAIR_REPORT", report.getId());
        }
    }

    private void updateReportStatus(Long reportId, ReportStatus status) {
        RepairReport report = reportMapper.selectById(reportId);
        if (report != null) {
            report.setStatus(status.getCode());
            reportMapper.updateById(report);
        }
    }

    private void assertPropertyManager() {
        if (!DataScope.isPlatformAdmin() && !DataScope.isPropertyAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private void assertReportVisibleToManager(RepairReport report) {
        if (DataScope.isPlatformAdmin()) {
            return;
        }
        if (DataScope.isPropertyAdmin() && Objects.equals(DataScope.currentCommunityId(), report.getCommunityId())) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN);
    }

    private void assertTicketVisible(RepairTicket ticket) {
        if (DataScope.isPlatformAdmin()) {
            return;
        }
        if (DataScope.isMaintainer() && Objects.equals(DataScope.currentUserId(), ticket.getAssigneeId())) {
            return;
        }
        if (DataScope.isPropertyAdmin() && Objects.equals(DataScope.currentCommunityId(), ticket.getCommunityId())) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN);
    }
}
