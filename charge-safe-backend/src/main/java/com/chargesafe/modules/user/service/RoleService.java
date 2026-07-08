package com.chargesafe.modules.user.service;

import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.modules.user.dto.*;
import com.chargesafe.modules.user.vo.RoleVO;

import java.util.List;

public interface RoleService {

    PageResult<RoleVO> pageList(RoleQueryDTO query, PageQuery pageQuery);

    List<RoleVO> listEnabled();

    RoleVO create(RoleCreateDTO dto);

    RoleVO update(Long id, RoleUpdateDTO dto);

    void delete(Long id);

    void assignMenus(Long id, AssignMenuDTO dto);
}
