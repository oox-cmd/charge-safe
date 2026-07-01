package com.chargesafe.common;

import com.baomidou.mybatisplus.annotation.*;
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

    /** 操作类型 */
    private String operation;

    /** 操作模块 */
    private String module;

    /** 请求方法 */
    private String method;

    /** 请求参数 */
    private String params;

    /** 请求URL */
    private String url;

    /** IP地址 */
    private String ip;

    /** 操作状态: 0-失败 1-成功 */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 耗时(毫秒) */
    private Long duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
