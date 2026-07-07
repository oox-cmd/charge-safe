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

    // -- modules2 兼容字段 --

    /** 端口总数 */
    private Long total;
    /** 空闲 */
    private Long idle;
    /** 充电中 */
    private Long charging;
    /** 已预约 */
    private Long reserved;
    /** 故障 */
    private Long fault;
    /** 维修中 */
    private Long maintenance;
    /** 离线 */
    private Long offline;

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
