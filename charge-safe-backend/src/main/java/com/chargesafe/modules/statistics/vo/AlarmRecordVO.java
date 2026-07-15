package com.chargesafe.modules.statistics.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 大屏统一告警记录视图对象
 */
@Data
public class AlarmRecordVO {

    /**
     * 告警唯一标识 (格式: 类型-原ID)
     */
    private String id;

    /**
     * 所属设备ID (如果有)
     */
    private Long deviceId;

    /**
     * 所属社区ID
     */
    private Long communityId;

    /**
     * 告警类型
     * FAULT: 设备端口故障
     * HAZARD: 安全隐患
     */
    private String alarmType;

    /**
     * 告警内容描述
     */
    private String alarmContent;

    /**
     * 告警级别: INFO-提示 WARNING-警告 ERROR-严重
     */
    private String alarmLevel;

    /**
     * 发生时间
     */
    private LocalDateTime createdTime;
}
