package com.chargesafe.modules.user.service;

import com.chargesafe.modules.user.dto.MenuCreateDTO;
import com.chargesafe.modules.user.dto.MenuUpdateDTO;
import com.chargesafe.modules.user.vo.MenuVO;

import java.util.List;
import java.util.Set;

public interface MenuService {

    List<MenuVO> listTree(Boolean onlyEnabled);

    List<MenuVO> listCurrentUserMenus();

    /**
     * 查询指定用户拥有的所有权限码（含菜单级和按钮级）。
     * 平台管理员返回所有启用的权限码。
     */
    Set<String> listPermissionCodesByUserId(Long userId);

    MenuVO create(MenuCreateDTO dto);

    MenuVO update(Long id, MenuUpdateDTO dto);

    void delete(Long id);
}
