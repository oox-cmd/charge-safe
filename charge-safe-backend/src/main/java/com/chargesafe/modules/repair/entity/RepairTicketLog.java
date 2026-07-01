package com.chargesafe.modules.repair.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单日志实体
 */
@Data
@TableName("repair_ticket_log")
public class RepairTicketLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ticketId;

    private Long operatorId;

    private String action;

    private String content;

    private String fromStatus;

    private String toStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
