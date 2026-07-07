package com.chargesafe.modules.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.community.dto.ParkingAreaDTO;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.community.service.ParkingAreaService;
import com.chargesafe.security.DataScope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingAreaServiceImpl implements ParkingAreaService {

    private final ParkingAreaMapper parkingAreaMapper;

    @Override
    public PageResult<ParkingArea> pageList(Long communityId, PageQuery pageQuery) {
        Long scopedCommunityId = resolveCommunityId(communityId);
        Page<ParkingArea> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<ParkingArea> wrapper = new LambdaQueryWrapper<ParkingArea>()
                .eq(scopedCommunityId != null, ParkingArea::getCommunityId, scopedCommunityId)
                .orderByDesc(ParkingArea::getCreatedTime);

        Page<ParkingArea> result = parkingAreaMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public List<ParkingArea> listByCommunityId(Long communityId) {
        Long scopedCommunityId = resolveCommunityId(communityId);
        return parkingAreaMapper.selectList(
                new LambdaQueryWrapper<ParkingArea>()
                        .eq(scopedCommunityId != null, ParkingArea::getCommunityId, scopedCommunityId)
                        .eq(ParkingArea::getStatus, 1)
                        .orderByDesc(ParkingArea::getCreatedTime)
        );
    }

    /**
     * 解析社区ID：非平台管理员强制使用登录用户所属社区，忽略前端传入值
     */
    private Long resolveCommunityId(Long communityId) {
        if (DataScope.isPlatformAdmin()) {
            return communityId;
        }
        return DataScope.currentCommunityId();
    }

    @Override
    public ParkingArea getById(Long id) {
        ParkingArea parkingArea = parkingAreaMapper.selectById(id);
        if (parkingArea == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return parkingArea;
    }

    @Override
    public ParkingArea create(ParkingAreaDTO dto) {
        ParkingArea parkingArea = new ParkingArea();
        BeanUtils.copyProperties(dto, parkingArea);
        parkingArea.setStatus(1);
        parkingArea.setPortCount(0);
        parkingAreaMapper.insert(parkingArea);
        return parkingArea;
    }

    @Override
    public ParkingArea update(ParkingAreaDTO dto) {
        ParkingArea parkingArea = getById(dto.getId());
        BeanUtils.copyProperties(dto, parkingArea, "id", "portCount", "status", "createdTime", "updatedTime", "deleted");
        parkingAreaMapper.updateById(parkingArea);
        return parkingArea;
    }

    @Override
    public void delete(Long id) {
        ParkingArea parkingArea = getById(id);
        if (parkingArea.getPortCount() > 0) {
            throw new BusinessException("车棚下存在充电设备，无法删除");
        }
        parkingAreaMapper.deleteById(id);
    }
}
