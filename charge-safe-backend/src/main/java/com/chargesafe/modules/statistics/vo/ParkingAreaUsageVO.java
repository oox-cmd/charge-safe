package com.chargesafe.modules.statistics.vo;

import com.chargesafe.modules.device.entity.ChargingPort;
import lombok.Data;

import java.util.List;

/**
 * 车棚使用率统计
 */
@Data
public class ParkingAreaUsageVO {

    /**
     * 各车棚使用情况列表
     */
    private List<ParkingAreaUsage> areas;

    /**
     * 单个车棚使用情况
     */
    @Data
    public static class ParkingAreaUsage {

        /**
         * 所属社区业务 ID，用于三级大屏从社区准确下钻至车棚。
         */
        private Long communityId;

        /**
         * 车棚ID
         */
        private Long id;

        /**
         * 车棚名称
         */
        private String name;

        /**
         * 该车棚下真实的端口明细，按端口号升序排列，供大屏端口矩阵展示，不再使用汇总数伪造。
         */
        private List<ChargingPort> ports;

        /**
         * 端口总数
         */
        private Integer totalPorts;

        /**
         * 空闲端口数
         */
        private Integer idlePorts;

        /**
         * 充电中端口数
         */
        private Integer chargingPorts;

        /**
         * 故障端口数
         */
        private Integer faultPorts;

        /**
         * 使用率（百分比）
         */
        private Double usageRate;
    }
}
