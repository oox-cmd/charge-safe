package com.chargesafe.modules.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单趋势统计（最近7天）
 */
@Data
public class OrderTrendVO {

    private List<String> dates;

    private List<Long> orderCounts;

    private List<Long> completedCounts;

    private List<BigDecimal> amounts;

    private List<DayTrend> trends;

    @Data
    public static class DayTrend {
        private String date;
        private Long count;
        private BigDecimal amount;
    }
}
