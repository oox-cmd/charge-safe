package com.chargesafe.modules.statistics.service;

import com.chargesafe.modules.statistics.vo.*;

import java.util.Map;

public interface StatisticsService {

    /**
     * 获取管理运维大屏所需的只读聚合数据
     */
    ManagementBigscreenVO getManagementBigscreen(Long communityId);

    /**
     * 获取仪表盘统计数据
     */
    DashboardVO getDashboard(Long communityId);

    /**
     * 获取端口状态统计
     */
    PortStatusStatisticsVO getPortStatusStatistics(Long communityId);

    /**
     * 获取订单趋势统计（最近7天）
     */
    OrderTrendVO getOrderTrend(Long communityId);

    /**
     * 获取维修工单状态汇总
     */
    RepairSummaryVO getRepairSummary(Long communityId);

    /**
     * 获取安全隐患状态汇总
     */
    HazardSummaryVO getHazardSummary(Long communityId);

    /**
     * 获取车棚使用率统计
     */
    ParkingAreaUsageVO getParkingAreaUsage(Long communityId);
}
