package com.chargesafe.modules.statistics.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 端口状态统计
 * 同时输出 statusList 数组和扁平 key-value（如 "IDLE": 10），兼容两种前端格式
 */
@Data
public class PortStatusStatisticsVO {

    /**
     * 各状态端口数量列表（统计页使用）
     */
    private List<StatusCount> statusList;

    /**
     * 扁平状态统计（运维 dashboard 使用），通过 @JsonAnyGetter 展开到顶层
     */
    @JsonAnyGetter
    private Map<String, Long> portCounts = new LinkedHashMap<>();

    /**
     * 状态计数项
     */
    @Data
    public static class StatusCount {
        private String status;
        private String statusName;
        private Long count;
    }
}
