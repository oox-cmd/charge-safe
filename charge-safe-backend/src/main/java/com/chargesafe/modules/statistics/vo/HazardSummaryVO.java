package com.chargesafe.modules.statistics.vo;

import lombok.Data;

import java.util.List;

/**
 * 安全隐患状态汇总
 */
@Data
public class HazardSummaryVO {

    /**
     * 各状态隐患数量列表
     */
    private List<StatusCount> statusList;

    /**
     * 状态计数项
     */
    @Data
    public static class StatusCount {
        /**
         * 状态编码
         */
        private String status;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 数量
         */
        private Long count;
    }
}
