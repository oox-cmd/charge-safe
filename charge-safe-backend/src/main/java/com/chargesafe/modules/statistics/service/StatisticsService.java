package com.chargesafe.modules.statistics.service;

import com.chargesafe.modules.statistics.vo.DashboardVO;

public interface StatisticsService {

    /**
     * 获取仪表盘统计数据
     */
    DashboardVO getDashboard(Long communityId);
}
