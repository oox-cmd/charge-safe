package com.chargesafe.modules.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.community.dto.CommunityDTO;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.mapper.CommunityMapper;
import com.chargesafe.modules.community.service.CommunityService;
import com.chargesafe.security.DataScope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityMapper communityMapper;

    @Override
    public PageResult<Community> pageList(PageQuery pageQuery) {
        Page<Community> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<Community> wrapper = new LambdaQueryWrapper<Community>()
                .eq(!DataScope.isPlatformAdmin(), Community::getId, DataScope.currentCommunityId())
                .orderByDesc(Community::getCreatedTime);

        Page<Community> result = communityMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public List<Community> listAll() {
        if (!DataScope.isPlatformAdmin() && DataScope.currentCommunityId() == null) {
            return List.of();
        }
        return communityMapper.selectList(
                new LambdaQueryWrapper<Community>()
                        .eq(!DataScope.isPlatformAdmin(), Community::getId, DataScope.currentCommunityId())
                        .eq(Community::getStatus, 1)
                        .orderByDesc(Community::getCreatedTime)
        );
    }

    @Override
    public Community getById(Long id) {
        Community community = communityMapper.selectById(id);
        if (community == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        assertCommunityVisible(community);
        return community;
    }

    @Override
    public Community create(CommunityDTO dto) {
        if (!DataScope.isPlatformAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        Community community = new Community();
        BeanUtils.copyProperties(dto, community);
        community.setStatus(1);
        communityMapper.insert(community);
        return community;
    }

    @Override
    public Community update(CommunityDTO dto) {
        Community community = getById(dto.getId());
        BeanUtils.copyProperties(dto, community, "id", "status", "createdTime", "updatedTime", "deleted");
        communityMapper.updateById(community);
        return community;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Community community = getById(id);
        community.setStatus(status);
        communityMapper.updateById(community);
    }

    private void assertCommunityVisible(Community community) {
        if (!DataScope.isPlatformAdmin() && !community.getId().equals(DataScope.currentCommunityId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
