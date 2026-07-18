package com.chargesafe.modules.assistant.controller;

import com.chargesafe.common.Result;
import com.chargesafe.modules.assistant.dto.AssistantChatRequest;
import com.chargesafe.modules.assistant.service.AssistantService;
import com.chargesafe.modules.assistant.service.impl.AssistantServiceImpl;
import com.chargesafe.modules.assistant.vo.AssistantChatResponse;
import com.chargesafe.modules.assistant.vo.AssistantStatsVO;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 助手控制器
 */
@Tag(name = "AI 智能助手")
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;
    private final AssistantServiceImpl assistantServiceImpl;

    // ==================== 小程序端接口 ====================

    @Operation(summary = "AI 聊天问答")
    @PostMapping("/chat")
    public Result<AssistantChatResponse> chat(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody AssistantChatRequest request) {
        return Result.success(assistantService.chat(userId, request));
    }

    @Operation(summary = "AI 聊天问答（流式输出）")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody AssistantChatRequest request) {
        return assistantServiceImpl.chatStream(userId, request);
    }

    // ==================== 管理端接口 ====================

    @Operation(summary = "获取 AI 助手统计数据")
    @RequiresPermission("assistant:stats:view")
    @GetMapping("/stats")
    public Result<AssistantStatsVO> getStats() {
        return Result.success(assistantService.getStats());
    }
}
