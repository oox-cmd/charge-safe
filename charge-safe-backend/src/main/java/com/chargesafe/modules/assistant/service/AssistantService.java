package com.chargesafe.modules.assistant.service;

import com.chargesafe.modules.assistant.dto.AssistantChatRequest;
import com.chargesafe.modules.assistant.vo.AssistantChatResponse;
import com.chargesafe.modules.assistant.vo.AssistantStatsVO;

/**
 * AI 助手服务接口
 */
public interface AssistantService {

    /**
     * 聊天问答
     *
     * @param userId   用户ID
     * @param request  请求参数
     * @return 回答结果
     */
    AssistantChatResponse chat(Long userId, AssistantChatRequest request);

    /**
     * 获取统计数据
     *
     * @return 统计信息
     */
    AssistantStatsVO getStats();
}
