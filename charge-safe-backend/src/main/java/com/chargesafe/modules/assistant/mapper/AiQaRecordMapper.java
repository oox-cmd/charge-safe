package com.chargesafe.modules.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.assistant.entity.AiQaRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * AI 问答记录 Mapper
 */
@Mapper
public interface AiQaRecordMapper extends BaseMapper<AiQaRecord> {

    /**
     * 统计今日问答数
     */
    @Select("SELECT COUNT(*) FROM ai_qa_record WHERE DATE(created_time) = CURDATE()")
    Long countToday();

    /**
     * 统计本周问答数
     */
    @Select("SELECT COUNT(*) FROM ai_qa_record WHERE YEARWEEK(created_time, 1) = YEARWEEK(CURDATE(), 1)")
    Long countWeek();

    /**
     * 统计本月问答数
     */
    @Select("SELECT COUNT(*) FROM ai_qa_record WHERE YEAR(created_time) = YEAR(NOW()) AND MONTH(created_time) = MONTH(NOW())")
    Long countMonth();

    /**
     * 统计来源分布
     */
    @Select("SELECT source_type, COUNT(*) as count FROM ai_qa_record GROUP BY source_type")
    List<Map<String, Object>> countBySourceType();

    /**
     * 统计高频问题 TOP10
     */
    @Select("SELECT question, COUNT(*) as count FROM ai_qa_record GROUP BY question ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> getHotQuestions();

    /**
     * 统计近7天每日问答数
     */
    @Select("SELECT DATE(created_time) as date, COUNT(*) as count FROM ai_qa_record WHERE created_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(created_time) ORDER BY date")
    List<Map<String, Object>> getDailyTrend();
}
