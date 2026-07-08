package com.chargesafe.modules.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检记录实体
 */
@Data
@TableName("inspection_record")
public class InspectionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long inspectorId;

    private String checkItem;

    /**
     * 检查结果: NORMAL-正常 ABNORMAL-异常
     */
    private String checkResult;

    private String remark;

    /**
     * 照片(JSON数组)
     */
    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
