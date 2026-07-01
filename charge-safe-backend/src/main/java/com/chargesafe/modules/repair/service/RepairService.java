package com.chargesafe.modules.repair.service;

import com.chargesafe.common.PageResult;
import com.chargesafe.common.PageQuery;
import com.chargesafe.modules.repair.dto.RepairReportDTO;
import com.chargesafe.modules.repair.dto.TicketDispatchDTO;
import com.chargesafe.modules.repair.dto.TicketFinishDTO;
import com.chargesafe.modules.repair.entity.RepairReport;
import com.chargesafe.modules.repair.entity.RepairTicket;

public interface RepairService {

    /**
     * 提交报修
     */
    RepairReport createReport(RepairReportDTO dto);

    /**
     * 分页查询报修记录
     */
    PageResult<RepairReport> pageReports(Long communityId, String status, PageQuery pageQuery);

    /**
     * 根据ID查询报修记录
     */
    RepairReport getReportById(Long id);

    /**
     * 派发工单
     */
    RepairTicket dispatchTicket(TicketDispatchDTO dto);

    /**
     * 运维人员接单
     */
    void acceptTicket(Long ticketId);

    /**
     * 运维人员开始处理
     */
    void startTicket(Long ticketId);

    /**
     * 维修完成
     */
    void finishTicket(Long ticketId, TicketFinishDTO dto);

    /**
     * 验收工单
     */
    void acceptTicketByProperty(Long ticketId);

    /**
     * 驳回工单
     */
    void rejectTicket(Long ticketId, String reason);

    /**
     * 关闭工单
     */
    void closeTicket(Long ticketId);

    /**
     * 分页查询工单
     */
    PageResult<RepairTicket> pageTickets(Long assigneeId, String status, PageQuery pageQuery);

    /**
     * 根据ID查询工单
     */
    RepairTicket getTicketById(Long id);
}
