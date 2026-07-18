package com.chargesafe.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("alarm_record")
public class AlarmRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private Long portId;
    private Long communityId;
    private String alarmType;
    private String alarmContent;
    private String alarmLevel;
    private String status;
    private Long handledBy;
    private LocalDateTime handleTime;
    private LocalDateTime createdTime;
}
