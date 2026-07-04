package com.chargesafe.modules.statistics.vo;

import lombok.Data;

import java.util.List;

/**
 * 端口状态统计
 */
@Data
public class PortStatusStatisticsVO {

    /**
     * 各状态端口数量列表
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
