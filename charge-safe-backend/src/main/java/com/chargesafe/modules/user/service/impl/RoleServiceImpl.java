package com.chargesafe.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.user.dto.*;
import com.chargesafe.modules.user.entity.SysRole;
import com.chargesafe.modules.user.entity.SysRoleMenu;
import com.chargesafe.modules.user.entity.SysUserRole;
import com.chargesafe.modules.user.mapper.SysRoleMapper;
import com.chargesafe.modules.user.mapper.SysRoleMenuMapper;
import com.chargesafe.modules.user.mapper.SysUserRoleMapper;
import com.chargesafe.modules.user.service.RoleService;
import com.chargesafe.modules.user.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public PageResult<RoleVO> pageList(RoleQueryDTO query, PageQuery pageQuery) {
        Page<SysRole> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(query.getRoleName()), SysRole::getRoleName, query.getRoleName())
                .like(StringUtils.hasText(query.getRoleCode()), SysRole::getRoleCode, query.getRoleCode())
                .eq(query.getStatus() != null, SysRole::getStatus, query.getStatus())
                .orderByDesc(SysRole::getCreatedTime);
        Page<SysRole> result = roleMapper.selectPage(page, wrapper);
        List<RoleVO> records = result.getRecords().stream().map(this::toRoleVO).toList();
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public List<RoleVO> listEnabled() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getId))
                .stream()
                .map(this::toRoleVO)
                .toList();
    }

    @Override
    @Transactional
    public RoleVO create(RoleCreateDTO dto) {
        ensureRoleCodeUnique(dto.getRoleCode(), null);
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role, "menuIds");
        role.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        roleMapper.insert(role);
        replaceRoleMenus(role.getId(), dto.getMenuIds());
        return toRoleVO(roleMapper.selectById(role.getId()));
    }

    @Override
    @Transactional
    public RoleVO update(Long id, RoleUpdateDTO dto) {
        SysRole role = getRoleEntity(id);
        BeanUtils.copyProperties(dto, role, "roleCode", "menuIds");
        roleMapper.updateById(role);
        if (dto.getMenuIds() != null) {
            replaceRoleMenus(id, dto.getMenuIds());
        }
        return toRoleVO(roleMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getRoleEntity(id);
        Long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException("角色已分配用户，不能删除");
        }
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
    }

    @Override
    @Transactional
    public void assignMenus(Long id, AssignMenuDTO dto) {
        getRoleEntity(id);
        replaceRoleMenus(id, dto.getMenuIds());
    }

    private SysRole getRoleEntity(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return role;
    }

    private void ensureRoleCodeUnique(String roleCode, Long ignoreId) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .ne(ignoreId != null, SysRole::getId, ignoreId));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST);
        }
    }

    private void replaceRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (CollectionUtils.isEmpty(menuIds)) {
            return;
        }
        for (Long menuId : menuIds.stream().filter(Objects::nonNull).distinct().toList()) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }

    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        vo.setMenuIds(roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getId()))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList());
        return vo;
    }
}
