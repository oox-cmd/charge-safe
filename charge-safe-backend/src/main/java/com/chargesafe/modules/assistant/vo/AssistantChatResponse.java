package com.chargesafe.modules.assistant.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 助手聊天响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatResponse {

    /**
     * 回答内容
     */
    private String answer;

    /**
     * 推荐的快捷操作
     */
    private List<String> suggestions;

    /**
     * 回答来源: LLM / FAQ / FALLBACK
     */
    private String sourceType;
}
