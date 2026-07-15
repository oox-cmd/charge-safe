package com.chargesafe.modules.resident.vo;

import lombok.Data;

@Data
public class ResidentParkingAreaVO {

    private Long parkingAreaId;
    private String parkingAreaName;
    private String locationDescription;
    private long totalPorts;
    private long idleCount;
    private long reservedCount;
    private long chargingCount;
    private long faultCount;
    private long maintenanceCount;
    private long unavailableCount;
    private Boolean availableForReservation;
}
