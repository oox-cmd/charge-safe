package com.chargesafe.modules.device.service;

import com.chargesafe.common.PageResult;
import com.chargesafe.common.PageQuery;
import com.chargesafe.modules.device.dto.DeviceDTO;
import com.chargesafe.modules.device.entity.ChargingDevice;

import java.util.List;

public interface DeviceService {

    /**
     * 分页查询设备列表
     */
    PageResult<ChargingDevice> pageList(Long parkingAreaId, PageQuery pageQuery);

    /**
     * 查询车棚下所有设备
     */
    List<ChargingDevice> listByParkingAreaId(Long parkingAreaId);

    /**
     * 根据ID查询设备
     */
    ChargingDevice getById(Long id);

    /**
     * 创建设备
     */
    ChargingDevice create(DeviceDTO dto);

    /**
     * 更新设备
     */
    ChargingDevice update(DeviceDTO dto);

    /**
     * 启用/停用设备
     */
    void updateStatus(Long id, Integer status);
}
