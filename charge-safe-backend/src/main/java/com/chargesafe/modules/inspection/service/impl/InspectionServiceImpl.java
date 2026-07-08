package com.chargesafe.modules.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.inspection.dto.*;
import com.chargesafe.modules.inspection.entity.InspectionRecord;
import com.chargesafe.modules.inspection.entity.InspectionTask;
import com.chargesafe.modules.inspection.entity.SafetyHazard;
import com.chargesafe.modules.inspection.enums.HazardStatus;
import com.chargesafe.modules.inspection.enums.TaskStatus;
import com.chargesafe.modules.inspection.mapper.InspectionRecordMapper;
import com.chargesafe.modules.inspection.mapper.InspectionTaskMapper;
import com.chargesafe.modules.inspection.mapper.SafetyHazardMapper;
import com.chargesafe.modules.inspection.service.InspectionService;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.mapper.CommunityMapper;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionServiceImpl implements InspectionService {

    private final InspectionTaskMapper taskMapper;
    private final InspectionRecordMapper recordMapper;
    private final SafetyHazardMapper hazardMapper;
    private final NotificationMapper notificationMapper;
    private final CommunityMapper communityMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public InspectionTask createTask(InspectionTaskDTO dto) {
        Long userId = UserContext.getUserId();

        InspectionTask task = new InspectionTask();
        task.setCommunityId(dto.getCommunityId());
        task.setTaskName(dto.getTaskName());
        task.setTaskDesc(dto.getTaskDesc());
        task.setInspectorId(dto.getInspectorId());
        task.setPlanStartTime(dto.getPlanStartTime());
        task.setPlanEndTime(dto.getPlanEndTime());
        task.setStatus(TaskStatus.PENDING.getCode());
        task.setCreatedBy(userId);
        taskMapper.insert(task);

        // 通知巡检人员
        createNotification(dto.getInspectorId(), "巡检任务通知",
                "您有新的巡检任务：" + task.getTaskName(),
                "INSPECTION", "INSPECTION_TASK", task.getId());

        return task;
    }

    @Override
    public PageResult<InspectionTask> pageTasks(Long communityId, Long inspectorId, String status, PageQuery pageQuery) {
        Page<InspectionTask> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<InspectionTask>()
                .eq(communityId != null, InspectionTask::getCommunityId, communityId)
                .eq(inspectorId != null, InspectionTask::getInspectorId, inspectorId)
                .eq(StringUtils.hasText(status), InspectionTask::getStatus, status)
                .orderByDesc(InspectionTask::getCreatedTime);

        Page<InspectionTask> result = taskMapper.selectPage(page, wrapper);

        // Fill communityName and inspectorName
        Set<Long> communityIds = result.getRecords().stream().map(InspectionTask::getCommunityId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> inspectorIds = result.getRecords().stream().map(InspectionTask::getInspectorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> communityNameMap = communityIds.isEmpty() ? Map.of() : communityMapper.selectBatchIds(communityIds).stream().collect(Collectors.toMap(Community::getId, Community::getName));
        Map<Long, String> inspectorNameMap = inspectorIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(inspectorIds).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));
        result.getRecords().forEach(t -> {
            t.setCommunityName(communityNameMap.get(t.getCommunityId()));
            t.setInspectorName(inspectorNameMap.get(t.getInspectorId()));
        });

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public InspectionTask getTaskById(Long id) {
        InspectionTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return task;
    }

    @Override
    public void startTask(Long taskId) {
        Long userId = UserContext.getUserId();
        InspectionTask task = getTaskById(taskId);

        if (!task.getInspectorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!TaskStatus.PENDING.getCode().equals(task.getStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR);
        }

        task.setStatus(TaskStatus.IN_PROGRESS.getCode());
        task.setActualStartTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Override
    @Transactional
    public InspectionRecord submitInspection(Long taskId, InspectionSubmitDTO dto) {
        Long userId = UserContext.getUserId();
        InspectionTask task = getTaskById(taskId);

        if (!task.getInspectorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!TaskStatus.IN_PROGRESS.getCode().equals(task.getStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR);
        }

        InspectionRecord record = new InspectionRecord();
        record.setTaskId(taskId);
        record.setInspectorId(userId);
        record.setCheckItem(dto.getCheckItem());
        record.setCheckResult(dto.getCheckResult());
        record.setRemark(dto.getRemark());
        record.setImages(dto.getImages());
        recordMapper.insert(record);

        // 如果发现异常，更新任务状态
        if ("ABNORMAL".equals(dto.getCheckResult())) {
            task.setStatus(TaskStatus.ABNORMAL.getCode());
            taskMapper.updateById(task);
        }

        return record;
    }

    @Override
    public void completeTask(Long taskId) {
        Long userId = UserContext.getUserId();
        InspectionTask task = getTaskById(taskId);

        if (!task.getInspectorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!TaskStatus.IN_PROGRESS.getCode().equals(task.getStatus())
                && !TaskStatus.ABNORMAL.getCode().equals(task.getStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR);
        }

        task.setStatus(TaskStatus.COMPLETED.getCode());
        task.setActualEndTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Override
    @Transactional
    public SafetyHazard reportHazard(HazardReportDTO dto) {
        Long userId = UserContext.getUserId();

        SafetyHazard hazard = new SafetyHazard();
        hazard.setCommunityId(dto.getCommunityId());
        hazard.setTaskId(dto.getTaskId());
        hazard.setReporterId(userId);
        hazard.setHazardType(dto.getHazardType());
        hazard.setSeverity(dto.getSeverity());
        hazard.setDescription(dto.getDescription());
        hazard.setImages(dto.getImages());
        hazard.setStatus(HazardStatus.PENDING.getCode());
        hazardMapper.insert(hazard);

        // 通知物业管理员
        createNotification(2L, "安全隐患通知",
                "有新的安全隐患上报，请及时处理。隐患类型：" + dto.getHazardType(),
                "HAZARD", "SAFETY_HAZARD", hazard.getId());

        return hazard;
    }

    @Override
    public PageResult<SafetyHazard> pageHazards(Long communityId, String status, PageQuery pageQuery) {
        Page<SafetyHazard> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<SafetyHazard> wrapper = new LambdaQueryWrapper<SafetyHazard>()
                .eq(communityId != null, SafetyHazard::getCommunityId, communityId)
                .eq(StringUtils.hasText(status), SafetyHazard::getStatus, status)
                .orderByDesc(SafetyHazard::getCreatedTime);

        Page<SafetyHazard> result = hazardMapper.selectPage(page, wrapper);

        // Fill reporterName
        Set<Long> reporterIds = result.getRecords().stream().map(SafetyHazard::getReporterId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> reporterNameMap = reporterIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(reporterIds).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));
        result.getRecords().forEach(h -> h.setReporterName(reporterNameMap.get(h.getReporterId())));

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public SafetyHazard getHazardById(Long id) {
        SafetyHazard hazard = hazardMapper.selectById(id);
        if (hazard == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return hazard;
    }

    @Override
    public void rectifyHazard(Long hazardId, HazardRectifyDTO dto) {
        Long userId = UserContext.getUserId();
        SafetyHazard hazard = getHazardById(hazardId);

        if (!HazardStatus.PENDING.getCode().equals(hazard.getStatus())
                && !HazardStatus.REVIEW_FAILED.getCode().equals(hazard.getStatus())) {
            throw new BusinessException(ResultCode.HAZARD_STATUS_ERROR);
        }

        hazard.setStatus(HazardStatus.WAITING_REVIEW.getCode());
        hazard.setRectifierId(userId);
        hazard.setRectifyDesc(dto.getRectifyDesc());
        hazard.setRectifyImages(dto.getRectifyImages());
        hazard.setRectifyTime(LocalDateTime.now());
        hazardMapper.updateById(hazard);

        // 通知巡检人员复查
        createNotification(hazard.getReporterId(), "隐患复查通知",
                "隐患整改已完成，请进行复查。",
                "HAZARD", "SAFETY_HAZARD", hazardId);
    }

    @Override
    public void reviewHazard(Long hazardId, HazardReviewDTO dto) {
        Long userId = UserContext.getUserId();
        SafetyHazard hazard = getHazardById(hazardId);

        if (!HazardStatus.WAITING_REVIEW.getCode().equals(hazard.getStatus())) {
            throw new BusinessException(ResultCode.HAZARD_STATUS_ERROR);
        }

        hazard.setReviewerId(userId);
        hazard.setReviewResult(dto.getReviewResult());
        hazard.setReviewRemark(dto.getReviewRemark());
        hazard.setReviewTime(LocalDateTime.now());

        if ("PASS".equals(dto.getReviewResult())) {
            hazard.setStatus(HazardStatus.REVIEW_PASSED.getCode());
        } else {
            hazard.setStatus(HazardStatus.REVIEW_FAILED.getCode());
            // 通知物业重新整改
            createNotification(hazard.getRectifierId(), "隐患复查不通过",
                    "隐患复查不通过，请重新整改。",
                    "HAZARD", "SAFETY_HAZARD", hazardId);
        }

        hazardMapper.updateById(hazard);
    }

    @Override
    public void closeHazard(Long hazardId) {
        SafetyHazard hazard = getHazardById(hazardId);

        if (!HazardStatus.REVIEW_PASSED.getCode().equals(hazard.getStatus())) {
            throw new BusinessException(ResultCode.HAZARD_STATUS_ERROR);
        }

        hazard.setStatus(HazardStatus.CLOSED.getCode());
        hazardMapper.updateById(hazard);
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
}
