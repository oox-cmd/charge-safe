package com.chargesafe.modules.statistics.vo;

import lombok.Data;

import java.util.List;

@Data
public class ManagementBigscreenVO {

    private DashboardVO dashboard;
    private PortStatusStatisticsVO portStatus;
    private List<OrderTrendVO.DayTrend> orderTrend;
    private ParkingAreaUsageVO parkingAreaUsage;
    private List<AlarmRecordVO> alarms;
    private List<CommunitySceneVO> communities;
    private String dataSource;
    private String dataDisclaimer;
}
