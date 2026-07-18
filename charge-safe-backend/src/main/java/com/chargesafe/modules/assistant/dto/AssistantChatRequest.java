package com.chargesafe.modules.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 助手聊天请求
 */
@Data
public class AssistantChatRequest {

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题不能超过500字")
    private String question;

    /**
     * 页面上下文，例如 parking-detail、reservation 等
     */
    private String pageContext;
}
