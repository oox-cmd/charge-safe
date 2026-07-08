package com.chargesafe.modules.inspection.service;

import com.chargesafe.common.PageResult;
import com.chargesafe.common.PageQuery;
import com.chargesafe.modules.inspection.dto.*;
import com.chargesafe.modules.inspection.entity.InspectionRecord;
import com.chargesafe.modules.inspection.entity.InspectionTask;
import com.chargesafe.modules.inspection.entity.SafetyHazard;

public interface InspectionService {

    // ==================== 巡检任务 ====================

    /**
     * 创建巡检任务
     */
    InspectionTask createTask(InspectionTaskDTO dto);

    /**
     * 分页查询巡检任务
     */
    PageResult<InspectionTask> pageTasks(Long communityId, Long inspectorId, String status, PageQuery pageQuery);

    /**
     * 根据ID查询巡检任务
     */
    InspectionTask getTaskById(Long id);

    /**
     * 开始巡检
     */
    void startTask(Long taskId);

    /**
     * 提交巡检结果
     */
    InspectionRecord submitInspection(Long taskId, InspectionSubmitDTO dto);

    /**
     * 完成巡检任务
     */
    void completeTask(Long taskId);

    // ==================== 安全隐患 ====================

    /**
     * 上报安全隐患
     */
    SafetyHazard reportHazard(HazardReportDTO dto);

    /**
     * 分页查询安全隐患
     */
    PageResult<SafetyHazard> pageHazards(Long communityId, String status, PageQuery pageQuery);

    /**
     * 根据ID查询安全隐患
     */
    SafetyHazard getHazardById(Long id);

    /**
     * 隐患整改
     */
    void rectifyHazard(Long hazardId, HazardRectifyDTO dto);

    /**
     * 隐患复查
     */
    void reviewHazard(Long hazardId, HazardReviewDTO dto);

    /**
     * 关闭隐患
     */
    void closeHazard(Long hazardId);
}
