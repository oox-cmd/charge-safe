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
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

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

    private static final AtomicLong TICKET_SEQ = new AtomicLong(1);

    @Override
    @Transactional
    public RepairReport createReport(RepairReportDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 获取设备所属社区ID
        Long communityId = null;
        ChargingDevice device = deviceMapper.selectById(dto.getDeviceId());
        if (device != null) {
            ParkingArea parkingArea = parkingAreaMapper.selectById(device.getParkingAreaId());
            if (parkingArea != null) {
                communityId = parkingArea.getCommunityId();
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
        LambdaQueryWrapper<RepairReport> wrapper = new LambdaQueryWrapper<RepairReport>()
                .eq(communityId != null, RepairReport::getCommunityId, communityId)
                .eq(StringUtils.hasText(status), RepairReport::getStatus, status)
                .orderByDesc(RepairReport::getCreatedTime);

        Page<RepairReport> result = reportMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public RepairReport getReportById(Long id) {
        RepairReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return report;
    }

    @Override
    @Transactional
    public RepairTicket dispatchTicket(TicketDispatchDTO dto) {
        Long userId = UserContext.getUserId();
        RepairReport report = getReportById(dto.getReportId());

        // 更新报修状态
        report.setStatus(ReportStatus.DISPATCHED.getCode());
        reportMapper.updateById(report);

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

        saveTicketLog(ticketId, userId, "FINISH", "维修完成，等待验收", TicketStatus.PROCESSING.getCode(), TicketStatus.WAITING_ACCEPTANCE.getCode());

        // 通知物业验收
        createNotificationForProperty(ticket.getCommunityId(), "工单验收通知",
                "维修工单" + ticket.getTicketNo() + "已完成维修，请进行验收。",
                "REPAIR", "REPAIR_TICKET", ticketId);
    }

    @Override
    @Transactional
    public void acceptTicketByProperty(Long ticketId) {
        Long userId = UserContext.getUserId();
        RepairTicket ticket = getTicketById(ticketId);

        if (!TicketStatus.WAITING_ACCEPTANCE.getCode().equals(ticket.getStatus())) {
            throw new BusinessException(ResultCode.TICKET_STATUS_ERROR);
        }

        ticket.setStatus(TicketStatus.FINISHED.getCode());
        ticketMapper.updateById(ticket);

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
    }

    @Override
    public void rejectTicket(Long ticketId, String reason) {
        Long userId = UserContext.getUserId();
        RepairTicket ticket = getTicketById(ticketId);

        if (!TicketStatus.WAITING_ACCEPTANCE.getCode().equals(ticket.getStatus())) {
            throw new BusinessException(ResultCode.TICKET_STATUS_ERROR);
        }

        ticket.setStatus(TicketStatus.REJECTED.getCode());
        ticketMapper.updateById(ticket);

        saveTicketLog(ticketId, userId, "REJECT", "验收不通过：" + reason, TicketStatus.WAITING_ACCEPTANCE.getCode(), TicketStatus.REJECTED.getCode());

        // 通知运维人员
        createNotification(ticket.getAssigneeId(), "工单驳回通知",
                "您的维修工单" + ticket.getTicketNo() + "验收不通过，请重新处理。",
                "REPAIR", "REPAIR_TICKET", ticketId);
    }

    @Override
    public void closeTicket(Long ticketId) {
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
                .eq(assigneeId != null, RepairTicket::getAssigneeId, assigneeId)
                .eq(StringUtils.hasText(status), RepairTicket::getStatus, status)
                .orderByDesc(RepairTicket::getCreatedTime);

        Page<RepairTicket> result = ticketMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public RepairTicket getTicketById(Long id) {
        RepairTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return ticket;
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
        // 简化处理：发送给社区的第一个物业管理员
        // 实际应该查询社区下所有物业管理员
        Notification notification = new Notification();
        notification.setUserId(2L); // 默认物业管理员ID
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }
}
