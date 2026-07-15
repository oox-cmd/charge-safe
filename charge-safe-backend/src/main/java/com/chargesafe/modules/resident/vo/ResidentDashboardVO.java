package com.chargesafe.modules.resident.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResidentDashboardVO {

    private Long communityId;
    private String communityName;
    private int totalParkingAreas;
    private long totalPorts;
    private long idleCount;
    private long reservedCount;
    private long chargingCount;
    private long faultCount;
    private long maintenanceCount;
    private long unavailableCount;
    private Boolean availableForReservation = false;
    private List<ResidentParkingAreaVO> parkingAreas = new ArrayList<>();
}
