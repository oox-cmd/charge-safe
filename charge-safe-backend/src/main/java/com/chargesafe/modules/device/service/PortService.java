package com.chargesafe.modules.device.service;

import com.chargesafe.common.PageResult;
import com.chargesafe.common.PageQuery;
import com.chargesafe.modules.device.dto.PortDTO;
import com.chargesafe.modules.device.entity.ChargingPort;

import java.util.List;
import java.util.Map;

public interface PortService {

    /**
     * 分页查询端口列表
     */
    PageResult<ChargingPort> pageList(Long deviceId, String status, PageQuery pageQuery);

    /**
     * 查询设备下所有端口
     */
    List<ChargingPort> listByDeviceId(Long deviceId);

    /**
     * 查询车棚下所有端口
     */
    List<ChargingPort> listByParkingAreaId(Long parkingAreaId);

    /**
     * 查询空闲端口
     */
    List<ChargingPort> listIdlePorts(Long parkingAreaId);

    /**
     * 根据ID查询端口
     */
    ChargingPort getById(Long id);

    /**
     * 创建端口
     */
    ChargingPort create(PortDTO dto);

    /**
     * 批量创建端口
     */
    List<ChargingPort> batchCreate(Long deviceId, int count);

    /**
     * 更新端口状态
     */
    void updateStatus(Long id, String status);

    /**
     * 统计各状态端口数量
     */
    Map<String, Long> countByStatus(Long communityId);
}
