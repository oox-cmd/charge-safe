package com.chargesafe.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.user.dto.MenuCreateDTO;
import com.chargesafe.modules.user.dto.MenuUpdateDTO;
import com.chargesafe.modules.user.entity.*;
import com.chargesafe.modules.user.mapper.*;
import com.chargesafe.modules.user.service.MenuService;
import com.chargesafe.modules.user.vo.MenuVO;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuVO> listTree(Boolean onlyEnabled) {
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(Boolean.TRUE.equals(onlyEnabled), SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getId));
        return buildTree(menus.stream().map(this::toMenuVO).toList());
    }

    @Override
    public List<MenuVO> listCurrentUserMenus() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        boolean platformAdmin = roles.stream()
                .anyMatch(role -> "PLATFORM_ADMIN".equals(role.getRoleCode()) && Objects.equals(role.getStatus(), 1));

        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .ne(SysMenu::getMenuType, 3)
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getId);
        if (!platformAdmin) {
            List<Long> menuIds = roleMenuMapper.selectList(
                            new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
                    .stream()
                    .map(SysRoleMenu::getMenuId)
                    .distinct()
                    .toList();
            if (menuIds.isEmpty()) {
                return List.of();
            }
            wrapper.in(SysMenu::getId, menuIds);
        }
        return buildTree(menuMapper.selectList(wrapper).stream().map(this::toMenuVO).toList());
    }

    @Override
    public Set<String> listPermissionCodesByUserId(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Set.of();
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        boolean platformAdmin = roles.stream()
                .anyMatch(role -> "PLATFORM_ADMIN".equals(role.getRoleCode()) && Objects.equals(role.getStatus(), 1));

        // 平台管理员拥有所有启用的权限码
        if (platformAdmin) {
            return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getStatus, 1)
                            .isNotNull(SysMenu::getPermissionCode)
                            .select(SysMenu::getPermissionCode))
                    .stream()
                    .map(SysMenu::getPermissionCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        // 非管理员：根据角色菜单关联查询权限码
        List<Long> menuIds = roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .toList();
        if (menuIds.isEmpty()) {
            return Set.of();
        }

        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .eq(SysMenu::getStatus, 1)
                        .isNotNull(SysMenu::getPermissionCode)
                        .select(SysMenu::getPermissionCode))
                .stream()
                .map(SysMenu::getPermissionCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public MenuVO create(MenuCreateDTO dto) {
        ensurePermissionUnique(dto.getPermissionCode(), null);
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menu.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        menu.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        menu.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        menuMapper.insert(menu);
        return toMenuVO(menuMapper.selectById(menu.getId()));
    }

    @Override
    @Transactional
    public MenuVO update(Long id, MenuUpdateDTO dto) {
        SysMenu menu = getMenuEntity(id);
        if (Objects.equals(id, dto.getParentId())) {
            throw new BusinessException("父级菜单不能选择自身");
        }
        ensurePermissionUnique(dto.getPermissionCode(), id);
        BeanUtils.copyProperties(dto, menu);
        menu.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        menuMapper.updateById(menu);
        return toMenuVO(menuMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getMenuEntity(id);
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，不能删除");
        }
        menuMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, id));
    }

    public static List<MenuVO> buildTree(List<MenuVO> menus) {
        Map<Long, MenuVO> menuMap = menus.stream()
                .peek(menu -> menu.setChildren(new ArrayList<>()))
                .collect(Collectors.toMap(MenuVO::getId, Function.identity(), (left, right) -> left));

        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO menu : menus) {
            Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();
            MenuVO parent = menuMap.get(parentId);
            if (parentId == 0L || parent == null) {
                roots.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }
        sortTree(roots);
        return roots;
    }

    private static void sortTree(List<MenuVO> menus) {
        menus.sort(Comparator
                .comparing((MenuVO menu) -> Optional.ofNullable(menu.getSortOrder()).orElse(0))
                .thenComparing(menu -> Optional.ofNullable(menu.getId()).orElse(0L)));
        for (MenuVO menu : menus) {
            sortTree(menu.getChildren());
        }
    }

    private SysMenu getMenuEntity(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return menu;
    }

    private void ensurePermissionUnique(String permissionCode, Long ignoreId) {
        if (!StringUtils.hasText(permissionCode)) {
            return;
        }
        Long count = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getPermissionCode, permissionCode)
                .ne(ignoreId != null, SysMenu::getId, ignoreId));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST);
        }
    }

    private MenuVO toMenuVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }
}
