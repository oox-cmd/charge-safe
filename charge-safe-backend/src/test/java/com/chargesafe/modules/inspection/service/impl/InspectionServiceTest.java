package com.chargesafe.modules.inspection.service.impl;

import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.mapper.CommunityMapper;
import com.chargesafe.modules.inspection.dto.*;
import com.chargesafe.modules.inspection.entity.InspectionRecord;
import com.chargesafe.modules.inspection.entity.InspectionTask;
import com.chargesafe.modules.inspection.entity.SafetyHazard;
import com.chargesafe.modules.inspection.enums.HazardStatus;
import com.chargesafe.modules.inspection.enums.TaskStatus;
import com.chargesafe.modules.inspection.mapper.InspectionRecordMapper;
import com.chargesafe.modules.inspection.mapper.InspectionTaskMapper;
import com.chargesafe.modules.inspection.mapper.SafetyHazardMapper;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.security.LoginUser;
import com.chargesafe.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InspectionServiceTest {

    private static final Long INSPECTOR_ID = 10L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long COMMUNITY_ID = 1L;
    private static final Long TASK_ID = 100L;
    private static final Long HAZARD_ID = 200L;

    private InspectionTaskMapper taskMapper;
    private InspectionRecordMapper recordMapper;
    private SafetyHazardMapper hazardMapper;
    private NotificationMapper notificationMapper;
    private CommunityMapper communityMapper;
    private SysUserMapper userMapper;
    private InspectionServiceImpl inspectionService;

    @BeforeEach
    void setUp() {
        taskMapper = mock(InspectionTaskMapper.class);
        recordMapper = mock(InspectionRecordMapper.class);
        hazardMapper = mock(SafetyHazardMapper.class);
        notificationMapper = mock(NotificationMapper.class);
        communityMapper = mock(CommunityMapper.class);
        userMapper = mock(SysUserMapper.class);

        inspectionService = new InspectionServiceImpl(
                taskMapper, recordMapper, hazardMapper,
                notificationMapper, communityMapper, userMapper);

        when(taskMapper.insert(any())).thenReturn(1);
        when(recordMapper.insert(any())).thenReturn(1);
        when(hazardMapper.insert(any())).thenReturn(1);
        when(notificationMapper.insert(any())).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    // ==================== 巡检任务 ====================

    @Test
    void createTask_shouldSucceed() {
        UserContext.setUser(new LoginUser(1L, "admin", "PLATFORM_ADMIN"));

        InspectionTaskDTO dto = new InspectionTaskDTO();
        dto.setCommunityId(COMMUNITY_ID);
        dto.setTaskName("月度巡检");
        dto.setInspectorId(INSPECTOR_ID);

        InspectionTask result = inspectionService.createTask(dto);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING.getCode());
        assertThat(result.getTaskName()).isEqualTo("月度巡检");
        verify(taskMapper).insert(any(InspectionTask.class));
        verify(notificationMapper).insert(any());
    }

    @Test
    void startTask_shouldSucceed() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        when(taskMapper.selectById(TASK_ID)).thenReturn(createTask(TaskStatus.PENDING));
        when(taskMapper.updateById(any())).thenReturn(1);

        inspectionService.startTask(TASK_ID);

        verify(taskMapper).selectById(TASK_ID);
        verify(taskMapper).updateById(any());
    }

    @Test
    void startTask_shouldReject_whenWrongUser() {
        UserContext.setUser(new LoginUser(OTHER_USER_ID, "other", "INSPECTOR"));
        when(taskMapper.selectById(TASK_ID)).thenReturn(createTask(TaskStatus.PENDING));

        assertThatThrownBy(() -> inspectionService.startTask(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(403));
        verify(taskMapper, never()).updateById(any());
    }

    @Test
    void startTask_shouldReject_whenStatusNotPending() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        when(taskMapper.selectById(TASK_ID)).thenReturn(createTask(TaskStatus.COMPLETED));

        assertThatThrownBy(() -> inspectionService.startTask(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6030));
        verify(taskMapper, never()).updateById(any());
    }

    @Test
    void submitInspection_normal_shouldNotChangeTaskStatus() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        when(taskMapper.selectById(TASK_ID)).thenReturn(createTask(TaskStatus.IN_PROGRESS));

        InspectionSubmitDTO dto = new InspectionSubmitDTO();
        dto.setCheckItem("端口检查");
        dto.setCheckResult("NORMAL");

        InspectionRecord record = inspectionService.submitInspection(TASK_ID, dto);

        assertThat(record.getCheckResult()).isEqualTo("NORMAL");
        verify(recordMapper).insert(any(InspectionRecord.class));
        verify(taskMapper, never()).updateById(any());
    }

    @Test
    void submitInspection_abnormal_shouldUpdateTaskStatus() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        InspectionTask task = createTask(TaskStatus.IN_PROGRESS);
        when(taskMapper.selectById(TASK_ID)).thenReturn(task);

        InspectionSubmitDTO dto = new InspectionSubmitDTO();
        dto.setCheckItem("消防检查");
        dto.setCheckResult("ABNORMAL");

        InspectionRecord record = inspectionService.submitInspection(TASK_ID, dto);

        assertThat(record.getCheckResult()).isEqualTo("ABNORMAL");
        verify(recordMapper).insert(any(InspectionRecord.class));
        verify(taskMapper).updateById(task);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.ABNORMAL.getCode());
    }

    @Test
    void submitInspection_shouldReject_whenWrongUser() {
        UserContext.setUser(new LoginUser(OTHER_USER_ID, "other", "INSPECTOR"));
        when(taskMapper.selectById(TASK_ID)).thenReturn(createTask(TaskStatus.IN_PROGRESS));

        InspectionSubmitDTO dto = new InspectionSubmitDTO();
        dto.setCheckItem("端口检查");
        dto.setCheckResult("NORMAL");

        assertThatThrownBy(() -> inspectionService.submitInspection(TASK_ID, dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(403));
        verify(recordMapper, never()).insert(any());
    }

    @Test
    void completeTask_shouldSucceed() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        InspectionTask task = createTask(TaskStatus.IN_PROGRESS);
        when(taskMapper.selectById(TASK_ID)).thenReturn(task);

        inspectionService.completeTask(TASK_ID);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED.getCode());
        verify(taskMapper).updateById(task);
    }

    @Test
    void completeTask_shouldReject_whenWrongUser() {
        UserContext.setUser(new LoginUser(OTHER_USER_ID, "other", "INSPECTOR"));
        when(taskMapper.selectById(TASK_ID)).thenReturn(createTask(TaskStatus.IN_PROGRESS));

        assertThatThrownBy(() -> inspectionService.completeTask(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(403));
        verify(taskMapper, never()).updateById(any());
    }

    @Test
    void completeTask_shouldReject_whenTaskIsPending() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        when(taskMapper.selectById(TASK_ID)).thenReturn(createTask(TaskStatus.PENDING));

        assertThatThrownBy(() -> inspectionService.completeTask(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6030));
        verify(taskMapper, never()).updateById(any());
    }

    // ==================== 安全隐患 ====================

    @Test
    void reportHazard_shouldSucceed() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));

        HazardReportDTO dto = new HazardReportDTO();
        dto.setCommunityId(COMMUNITY_ID);
        dto.setHazardType("消防");
        dto.setSeverity("HIGH");
        dto.setDescription("灭火器过期");

        SafetyHazard result = inspectionService.reportHazard(dto);

        assertThat(result.getStatus()).isEqualTo(HazardStatus.PENDING.getCode());
        verify(hazardMapper).insert(any(SafetyHazard.class));
        verify(notificationMapper).insert(any());
    }

    @Test
    void rectifyHazard_shouldSucceed_fromPending() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        SafetyHazard hazard = createHazard(HazardStatus.PENDING);
        when(hazardMapper.selectById(HAZARD_ID)).thenReturn(hazard);

        HazardRectifyDTO dto = new HazardRectifyDTO();
        dto.setRectifyDesc("已更换灭火器");

        inspectionService.rectifyHazard(HAZARD_ID, dto);

        assertThat(hazard.getStatus()).isEqualTo(HazardStatus.WAITING_REVIEW.getCode());
        assertThat(hazard.getRectifierId()).isEqualTo(INSPECTOR_ID);
        verify(hazardMapper).updateById(hazard);
    }

    @Test
    void rectifyHazard_shouldSucceed_fromReviewFailed() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        SafetyHazard hazard = createHazard(HazardStatus.REVIEW_FAILED);
        when(hazardMapper.selectById(HAZARD_ID)).thenReturn(hazard);

        HazardRectifyDTO dto = new HazardRectifyDTO();
        dto.setRectifyDesc("已重新整改");

        inspectionService.rectifyHazard(HAZARD_ID, dto);

        assertThat(hazard.getStatus()).isEqualTo(HazardStatus.WAITING_REVIEW.getCode());
        verify(hazardMapper).updateById(hazard);
    }

    @Test
    void rectifyHazard_shouldReject_whenStatusClosed() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        when(hazardMapper.selectById(HAZARD_ID)).thenReturn(createHazard(HazardStatus.CLOSED));

        HazardRectifyDTO dto = new HazardRectifyDTO();
        dto.setRectifyDesc("不应允许关闭后更改");

        assertThatThrownBy(() -> inspectionService.rectifyHazard(HAZARD_ID, dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6040));
        verify(hazardMapper, never()).updateById(any());
    }

    @Test
    void reviewHazard_pass_shouldChangeToReviewPassed() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        SafetyHazard hazard = createHazard(HazardStatus.WAITING_REVIEW);
        when(hazardMapper.selectById(HAZARD_ID)).thenReturn(hazard);

        HazardReviewDTO dto = new HazardReviewDTO();
        dto.setReviewResult("PASS");
        dto.setReviewRemark("整改合格");

        inspectionService.reviewHazard(HAZARD_ID, dto);

        assertThat(hazard.getStatus()).isEqualTo(HazardStatus.REVIEW_PASSED.getCode());
        assertThat(hazard.getReviewerId()).isEqualTo(INSPECTOR_ID);
        verify(hazardMapper).updateById(hazard);
        verify(notificationMapper, never()).insert(any());
    }

    @Test
    void reviewHazard_fail_shouldRevertToWaitingRectify() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        SafetyHazard hazard = createHazard(HazardStatus.WAITING_REVIEW);
        hazard.setRectifierId(99L);
        when(hazardMapper.selectById(HAZARD_ID)).thenReturn(hazard);

        HazardReviewDTO dto = new HazardReviewDTO();
        dto.setReviewResult("FAIL");
        dto.setReviewRemark("整改不彻底，请继续");

        inspectionService.reviewHazard(HAZARD_ID, dto);

        assertThat(hazard.getStatus()).isEqualTo(HazardStatus.REVIEW_FAILED.getCode());
        verify(hazardMapper).updateById(hazard);
        verify(notificationMapper).insert(any());
    }

    @Test
    void reviewHazard_shouldReject_whenNotWaitingReview() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        when(hazardMapper.selectById(HAZARD_ID)).thenReturn(createHazard(HazardStatus.PENDING));

        HazardReviewDTO dto = new HazardReviewDTO();
        dto.setReviewResult("PASS");

        assertThatThrownBy(() -> inspectionService.reviewHazard(HAZARD_ID, dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6040));
        verify(hazardMapper, never()).updateById(any());
    }

    @Test
    void closeHazard_shouldSucceed_whenReviewPassed() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        SafetyHazard hazard = createHazard(HazardStatus.REVIEW_PASSED);
        when(hazardMapper.selectById(HAZARD_ID)).thenReturn(hazard);

        inspectionService.closeHazard(HAZARD_ID);

        assertThat(hazard.getStatus()).isEqualTo(HazardStatus.CLOSED.getCode());
        verify(hazardMapper).updateById(hazard);
    }

    @Test
    void closeHazard_shouldReject_whenNotReviewPassed() {
        UserContext.setUser(new LoginUser(INSPECTOR_ID, "inspector", "INSPECTOR"));
        when(hazardMapper.selectById(HAZARD_ID)).thenReturn(createHazard(HazardStatus.PENDING));

        assertThatThrownBy(() -> inspectionService.closeHazard(HAZARD_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6040));
        verify(hazardMapper, never()).updateById(any());
    }

    // ==================== helpers ====================

    private InspectionTask createTask(TaskStatus status) {
        InspectionTask task = new InspectionTask();
        task.setId(TASK_ID);
        task.setInspectorId(INSPECTOR_ID);
        task.setCommunityId(COMMUNITY_ID);
        task.setTaskName("月度巡检");
        task.setStatus(status.getCode());
        return task;
    }

    private SafetyHazard createHazard(HazardStatus status) {
        SafetyHazard hazard = new SafetyHazard();
        hazard.setId(HAZARD_ID);
        hazard.setCommunityId(COMMUNITY_ID);
        hazard.setReporterId(INSPECTOR_ID);
        hazard.setHazardType("消防");
        hazard.setSeverity("HIGH");
        hazard.setDescription("灭火器过期");
        hazard.setStatus(status.getCode());
        return hazard;
    }
}
