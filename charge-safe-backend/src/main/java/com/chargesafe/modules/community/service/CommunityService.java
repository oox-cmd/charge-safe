package com.chargesafe.modules.community.service;

import com.chargesafe.common.PageResult;
import com.chargesafe.common.PageQuery;
import com.chargesafe.modules.community.dto.CommunityDTO;
import com.chargesafe.modules.community.entity.Community;

import java.util.List;

public interface CommunityService {

    /**
     * 分页查询社区列表
     */
    PageResult<Community> pageList(PageQuery pageQuery);

    /**
     * 查询所有启用的社区
     */
    List<Community> listAll();

    /**
     * 根据ID查询社区
     */
    Community getById(Long id);

    /**
     * 创建社区
     */
    Community create(CommunityDTO dto);

    /**
     * 更新社区
     */
    Community update(CommunityDTO dto);

    /**
     * 启用/停用社区
     */
    void updateStatus(Long id, Integer status);
}
