package com.chargesafe.common;

import lombok.Getter;

/**
 * 结果状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),

    // 认证相关
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    TOKEN_EXPIRED(4001, "Token已过期"),
    TOKEN_INVALID(4002, "Token无效"),
    ACCOUNT_DISABLED(4003, "账号已被禁用"),
    USERNAME_OR_PASSWORD_ERROR(4004, "用户名或密码错误"),

    // 参数校验
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(4005, "参数缺失"),

    // 业务错误
    BUSINESS_ERROR(5000, "业务异常"),
    DATA_NOT_EXIST(5001, "数据不存在"),
    DATA_ALREADY_EXIST(5002, "数据已存在"),

    // 预约相关
    RESERVATION_ALREADY_EXIST(6001, "您已有有效预约，请先完成或取消"),
    PORT_NOT_IDLE(6002, "该端口当前不可预约"),
    RESERVATION_EXPIRED(6003, "预约已过期"),
    RESERVATION_NOT_FOUND(6004, "预约不存在"),

    // 充电相关
    CHARGING_NOT_STARTED(6010, "充电未开始"),
    CHARGING_ALREADY_FINISHED(6011, "充电已结束"),

    // 工单相关
    TICKET_STATUS_ERROR(6020, "工单状态不允许此操作"),
    TICKET_NOT_ASSIGNED(6021, "工单未分配维修人员"),

    // 巡检相关
    TASK_STATUS_ERROR(6030, "任务状态不允许此操作"),

    // 隐患相关
    HAZARD_STATUS_ERROR(6040, "隐患状态不允许此操作");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
