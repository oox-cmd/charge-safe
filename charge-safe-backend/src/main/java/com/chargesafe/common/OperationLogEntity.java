package com.chargesafe.common;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("operation_log")
public class OperationLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String operation;

    private String module;

    private String method;

    private String params;

    private String url;

    private String ip;

    private Integer status;

    private String errorMsg;

    private Long duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @JsonProperty("operator")
    public String getOperator() {
        return username;
    }

    @JsonProperty("createTime")
    public String getCreateTime() {
        return createdTime != null ? createdTime.toString().substring(0, 16) : null;
    }
}
