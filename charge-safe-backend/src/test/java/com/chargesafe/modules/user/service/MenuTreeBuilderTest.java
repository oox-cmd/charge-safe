package com.chargesafe.modules.user.service;

import com.chargesafe.modules.user.service.impl.MenuServiceImpl;
import com.chargesafe.modules.user.vo.MenuVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MenuTreeBuilderTest {

    @Test
    void buildTreeOrdersMenusAndNestsChildren() {
        MenuVO child = new MenuVO();
        child.setId(2L);
        child.setParentId(1L);
        child.setMenuName("用户管理");
        child.setSortOrder(20);

        MenuVO root = new MenuVO();
        root.setId(1L);
        root.setParentId(0L);
        root.setMenuName("系统管理");
        root.setSortOrder(10);

        MenuVO anotherRoot = new MenuVO();
        anotherRoot.setId(3L);
        anotherRoot.setParentId(0L);
        anotherRoot.setMenuName("工作台");
        anotherRoot.setSortOrder(1);

        List<MenuVO> tree = MenuServiceImpl.buildTree(List.of(child, root, anotherRoot));

        assertThat(tree).extracting(MenuVO::getMenuName)
                .containsExactly("工作台", "系统管理");
        assertThat(tree.get(1).getChildren()).extracting(MenuVO::getMenuName)
                .containsExactly("用户管理");
    }
}
