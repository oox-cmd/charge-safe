package com.chargesafe.modules.user.service;

import com.chargesafe.modules.user.dto.MenuCreateDTO;
import com.chargesafe.modules.user.dto.MenuUpdateDTO;
import com.chargesafe.modules.user.vo.MenuVO;

import java.util.List;

public interface MenuService {

    List<MenuVO> listTree(Boolean onlyEnabled);

    List<MenuVO> listCurrentUserMenus();

    MenuVO create(MenuCreateDTO dto);

    MenuVO update(Long id, MenuUpdateDTO dto);

    void delete(Long id);
}
