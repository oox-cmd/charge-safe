package com.chargesafe.modules.repair.controller;

import com.chargesafe.common.OperationLog;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.repair.dto.RepairReportDTO;
import com.chargesafe.modules.repair.dto.TicketDispatchDTO;
import com.chargesafe.modules.repair.dto.TicketFinishDTO;
import com.chargesafe.modules.repair.entity.RepairReport;
import com.chargesafe.modules.repair.entity.RepairTicket;
import com.chargesafe.modules.repair.service.RepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "报修工单管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

    // ==================== 报修相关 ====================

    @Operation(summary = "提交报修")
    @OperationLog(module = "报修管理", description = "提交报修", type = "CREATE")
    @PostMapping("/repair-reports")
    public Result<RepairReport> createReport(@Valid @RequestBody RepairReportDTO dto) {
        return Result.success(repairService.createReport(dto));
    }

    @Operation(summary = "分页查询报修记录")
    @GetMapping("/repair-reports")
    public Result<PageResult<RepairReport>> pageReports(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        return Result.success(repairService.pageReports(communityId, status, pageQuery));
    }

    @Operation(summary = "根据ID查询报修记录")
    @GetMapping("/repair-reports/{id}")
    public Result<RepairReport> getReportById(@PathVariable Long id) {
        return Result.success(repairService.getReportById(id));
    }

    // ==================== 工单相关 ====================

    @Operation(summary = "派发工单")
    @OperationLog(module = "报修管理", description = "派发工单", type = "UPDATE")
    @PostMapping("/repair-tickets/dispatch")
    public Result<RepairTicket> dispatchTicket(@Valid @RequestBody TicketDispatchDTO dto) {
        return Result.success(repairService.dispatchTicket(dto));
    }

    @Operation(summary = "运维人员接单")
    @OperationLog(module = "报修管理", description = "运维人员接单", type = "UPDATE")
    @PostMapping("/repair-tickets/{id}/accept")
    public Result<Void> acceptTicket(@PathVariable Long id) {
        repairService.acceptTicket(id);
        return Result.success();
    }

    @Operation(summary = "运维人员开始处理")
    @PostMapping("/repair-tickets/{id}/start")
    public Result<Void> startTicket(@PathVariable Long id) {
        repairService.startTicket(id);
        return Result.success();
    }

    @Operation(summary = "维修完成")
    @OperationLog(module = "报修管理", description = "维修完成", type = "UPDATE")
    @PostMapping("/repair-tickets/{id}/finish")
    public Result<Void> finishTicket(@PathVariable Long id, @Valid @RequestBody TicketFinishDTO dto) {
        repairService.finishTicket(id, dto);
        return Result.success();
    }

    @Operation(summary = "验收工单")
    @OperationLog(module = "报修管理", description = "验收工单", type = "UPDATE")
    @PostMapping("/repair-tickets/{id}/accept-repair")
    public Result<Void> acceptTicketByProperty(@PathVariable Long id) {
        repairService.acceptTicketByProperty(id);
        return Result.success();
    }

    @Operation(summary = "驳回工单")
    @PostMapping("/repair-tickets/{id}/reject")
    public Result<Void> rejectTicket(@PathVariable Long id, @RequestParam String reason) {
        repairService.rejectTicket(id, reason);
        return Result.success();
    }

    @Operation(summary = "关闭工单")
    @PostMapping("/repair-tickets/{id}/close")
    public Result<Void> closeTicket(@PathVariable Long id) {
        repairService.closeTicket(id);
        return Result.success();
    }

    @Operation(summary = "分页查询工单")
    @GetMapping("/repair-tickets")
    public Result<PageResult<RepairTicket>> pageTickets(
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        return Result.success(repairService.pageTickets(assigneeId, status, pageQuery));
    }

    @Operation(summary = "根据ID查询工单")
    @GetMapping("/repair-tickets/{id}")
    public Result<RepairTicket> getTicketById(@PathVariable Long id) {
        return Result.success(repairService.getTicketById(id));
    }
}
