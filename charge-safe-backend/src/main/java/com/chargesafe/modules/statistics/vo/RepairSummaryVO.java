package com.chargesafe.modules.statistics.vo;

import lombok.Data;

import java.util.List;

/**
 * 维修工单状态汇总
 */
@Data
public class RepairSummaryVO {

    /**
     * 各状态工单数量列表
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
