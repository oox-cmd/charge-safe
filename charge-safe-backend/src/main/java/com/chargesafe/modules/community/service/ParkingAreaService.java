package com.chargesafe.modules.community.service;

import com.chargesafe.common.PageResult;
import com.chargesafe.common.PageQuery;
import com.chargesafe.modules.community.dto.ParkingAreaDTO;
import com.chargesafe.modules.community.entity.ParkingArea;

import java.util.List;

public interface ParkingAreaService {

    /**
     * 分页查询车棚列表
     */
    PageResult<ParkingArea> pageList(Long communityId, PageQuery pageQuery);

    /**
     * 查询社区下所有车棚
     */
    List<ParkingArea> listByCommunityId(Long communityId);

    /**
     * 根据ID查询车棚
     */
    ParkingArea getById(Long id);

    /**
     * 创建车棚
     */
    ParkingArea create(ParkingAreaDTO dto);

    /**
     * 更新车棚
     */
    ParkingArea update(ParkingAreaDTO dto);

    /**
     * 删除车棚
     */
    void delete(Long id);
}
