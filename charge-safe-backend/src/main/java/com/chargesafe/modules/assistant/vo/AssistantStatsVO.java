package com.chargesafe.modules.assistant.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI 助手统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantStatsVO {

    /**
     * 今日问答总数
     */
    private Long todayCount;

    /**
     * 本周问答总数
     */
    private Long weekCount;

    /**
     * 本月问答总数
     */
    private Long monthCount;

    /**
     * 总问答数
     */
    private Long totalCount;

    /**
     * 问答来源分布 (LLM/FAQ/FALLBACK)
     */
    private Map<String, Long> sourceDistribution;

    /**
     * 高频问题 TOP10
     */
    private List<HotQuestion> hotQuestions;

    /**
     * 近7天问答趋势
     */
    private List<DailyCount> dailyTrend;

    /**
     * 高频问题
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotQuestion {
        private String question;
        private Long count;
    }

    /**
     * 每日统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private Long count;
    }
}
