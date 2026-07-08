package com.chargesafe.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    /**
     * 消息类型: RESERVATION-预约相关 CHARGING-充电相关 REPAIR-报修工单 INSPECTION-巡检相关 HAZARD-隐患相关 SYSTEM-系统通知
     */
    private String type;

    private String businessType;

    private Long businessId;

    /**
     * 是否已读: 0-未读 1-已读
     */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
