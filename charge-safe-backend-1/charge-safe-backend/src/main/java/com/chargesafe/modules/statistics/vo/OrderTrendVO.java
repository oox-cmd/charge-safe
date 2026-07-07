package com.chargesafe.modules.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单趋势统计（最近7天）
 */
@Data
public class OrderTrendVO {

    /**
     * 日期列表（如 "07-01", "07-02", ...）
     */
    private List<String> dates;

    /**
     * 每日订单数量
     */
    private List<Long> orderCounts;

    /**
     * 每日完成订单数量
     */
    private List<Long> completedCounts;

    /**
     * 每日订单金额
     */
    private List<BigDecimal> amounts;

    // -- modules2 兼容字段 --

    /** 每日订单数量 */
    private List<Long> counts;
    /** 每日订单金额 */
    private List<BigDecimal> revenues;
}
