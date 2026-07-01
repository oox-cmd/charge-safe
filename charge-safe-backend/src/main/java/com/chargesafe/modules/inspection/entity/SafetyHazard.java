package com.chargesafe.modules.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安全隐患实体
 */
@Data
@TableName("safety_hazard")
public class SafetyHazard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long communityId;

    private Long taskId;

    private Long reporterId;

    private String hazardType;

    /**
     * 严重程度: GENERAL-一般 SERIOUS-严重 URGENT-紧急
     */
    private String severity;

    private String description;

    /**
     * 照片(JSON数组)
     */
    private String images;

    /**
     * 状态: PENDING-待处理 RECTIFYING-整改中 WAITING_REVIEW-待复查 REVIEW_PASSED-复查通过 REVIEW_FAILED-复查不通过 CLOSED-已关闭
     */
    private String status;

    private Long rectifierId;

    private String rectifyDesc;

    private String rectifyImages;

    private LocalDateTime rectifyTime;

    private Long reviewerId;

    private String reviewResult;

    private String reviewRemark;

    private LocalDateTime reviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
