package com.chargesafe.modules.assistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 问答记录实体
 */
@Data
@TableName("ai_qa_record")
public class AiQaRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 问题
     */
    private String question;

    /**
     * 回答
     */
    private String answer;

    /**
     * 页面上下文
     */
    private String pageContext;

    /**
     * 回答来源: LLM / FAQ / FALLBACK
     */
    private String sourceType;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
