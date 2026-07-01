package com.chargesafe.modules.inspection.controller;

import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.inspection.dto.*;
import com.chargesafe.modules.inspection.entity.InspectionRecord;
import com.chargesafe.modules.inspection.entity.InspectionTask;
import com.chargesafe.modules.inspection.entity.SafetyHazard;
import com.chargesafe.modules.inspection.service.InspectionService;
import com.chargesafe.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "巡检隐患管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    // ==================== 巡检任务 ====================

    @Operation(summary = "创建巡检任务")
    @PostMapping("/inspection-tasks")
    public Result<InspectionTask> createTask(@Valid @RequestBody InspectionTaskDTO dto) {
        return Result.success(inspectionService.createTask(dto));
    }

    @Operation(summary = "分页查询巡检任务")
    @GetMapping("/inspection-tasks")
    public Result<PageResult<InspectionTask>> pageTasks(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) Long inspectorId,
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        return Result.success(inspectionService.pageTasks(communityId, inspectorId, status, pageQuery));
    }

    @Operation(summary = "查询我的巡检任务")
    @GetMapping("/inspection-tasks/my")
    public Result<PageResult<InspectionTask>> pageMyTasks(
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        Long userId = UserContext.getUserId();
        return Result.success(inspectionService.pageTasks(null, userId, status, pageQuery));
    }

    @Operation(summary = "根据ID查询巡检任务")
    @GetMapping("/inspection-tasks/{id}")
    public Result<InspectionTask> getTaskById(@PathVariable Long id) {
        return Result.success(inspectionService.getTaskById(id));
    }

    @Operation(summary = "开始巡检")
    @PostMapping("/inspection-tasks/{id}/start")
    public Result<Void> startTask(@PathVariable Long id) {
        inspectionService.startTask(id);
        return Result.success();
    }

    @Operation(summary = "提交巡检结果")
    @PostMapping("/inspection-tasks/{id}/submit")
    public Result<InspectionRecord> submitInspection(@PathVariable Long id, @Valid @RequestBody InspectionSubmitDTO dto) {
        return Result.success(inspectionService.submitInspection(id, dto));
    }

    @Operation(summary = "完成巡检任务")
    @PostMapping("/inspection-tasks/{id}/complete")
    public Result<Void> completeTask(@PathVariable Long id) {
        inspectionService.completeTask(id);
        return Result.success();
    }

    // ==================== 安全隐患 ====================

    @Operation(summary = "上报安全隐患")
    @PostMapping("/hazards")
    public Result<SafetyHazard> reportHazard(@Valid @RequestBody HazardReportDTO dto) {
        return Result.success(inspectionService.reportHazard(dto));
    }

    @Operation(summary = "分页查询安全隐患")
    @GetMapping("/hazards")
    public Result<PageResult<SafetyHazard>> pageHazards(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) String status,
            PageQuery pageQuery) {
        return Result.success(inspectionService.pageHazards(communityId, status, pageQuery));
    }

    @Operation(summary = "根据ID查询安全隐患")
    @GetMapping("/hazards/{id}")
    public Result<SafetyHazard> getHazardById(@PathVariable Long id) {
        return Result.success(inspectionService.getHazardById(id));
    }

    @Operation(summary = "隐患整改")
    @PostMapping("/hazards/{id}/rectify")
    public Result<Void> rectifyHazard(@PathVariable Long id, @Valid @RequestBody HazardRectifyDTO dto) {
        inspectionService.rectifyHazard(id, dto);
        return Result.success();
    }

    @Operation(summary = "隐患复查")
    @PostMapping("/hazards/{id}/review")
    public Result<Void> reviewHazard(@PathVariable Long id, @Valid @RequestBody HazardReviewDTO dto) {
        inspectionService.reviewHazard(id, dto);
        return Result.success();
    }

    @Operation(summary = "关闭隐患")
    @PostMapping("/hazards/{id}/close")
    public Result<Void> closeHazard(@PathVariable Long id) {
        inspectionService.closeHazard(id);
        return Result.success();
    }
}
