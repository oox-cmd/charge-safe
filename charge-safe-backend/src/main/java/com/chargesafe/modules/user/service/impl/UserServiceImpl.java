package com.chargesafe.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.user.dto.*;
import com.chargesafe.modules.user.entity.SysRole;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.entity.SysUserRole;
import com.chargesafe.modules.user.mapper.SysRoleMapper;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.mapper.SysUserRoleMapper;
import com.chargesafe.modules.user.service.UserService;
import com.chargesafe.modules.user.vo.UserVO;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final CommunityMapper communityMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public PageResult<UserVO> pageList(UserQueryDTO query, PageQuery pageQuery) {
        Page<SysUser> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getPhone()), SysUser::getPhone, query.getPhone())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(query.getCommunityId() != null, SysUser::getCommunityId, query.getCommunityId())
                .orderByDesc(SysUser::getCreatedTime);

        if (query.getRoleId() != null) {
            List<Long> userIds = userRoleMapper.selectList(
                            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, query.getRoleId()))
                    .stream()
                    .map(SysUserRole::getUserId)
                    .distinct()
                    .toList();
            if (userIds.isEmpty()) {
                return new PageResult<>(0, pageQuery.getPage(), pageQuery.getSize(), List.of());
            }
            wrapper.in(SysUser::getId, userIds);
        }

        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        List<UserVO> records = result.getRecords().stream().map(this::toUserVO).toList();
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    @Transactional
    public UserVO create(UserCreateDTO dto) {
        ensureUsernameUnique(dto.getUsername(), null);

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user, "password", "roleIds");
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        userMapper.insert(user);
        replaceUserRoles(user.getId(), dto.getRoleIds());
        return toUserVO(userMapper.selectById(user.getId()));
    }

    @Override
    @Transactional
    public UserVO update(Long id, UserUpdateDTO dto) {
        SysUser user = getUserEntity(id);
        BeanUtils.copyProperties(dto, user, "roleIds");
        userMapper.updateById(user);
        if (dto.getRoleIds() != null) {
            replaceUserRoles(id, dto.getRoleIds());
        }
        return toUserVO(userMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getUserEntity(id);
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = getUserEntity(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long id, AssignRoleDTO dto) {
        getUserEntity(id);
        replaceUserRoles(id, dto.getRoleIds());
    }

    @Override
    public void resetPassword(Long id, String password) {
        SysUser user = getUserEntity(id);
        user.setPassword(passwordEncoder.encode(StringUtils.hasText(password) ? password : "123456"));
        userMapper.updateById(user);
    }

    private SysUser getUserEntity(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return user;
    }

    private void ensureUsernameUnique(String username, Long ignoreId) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .ne(ignoreId != null, SysUser::getId, ignoreId));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST);
        }
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        List<Long> distinctRoleIds = roleIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctRoleIds.isEmpty()) {
            return;
        }
        Long validRoleCount = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().in(SysRole::getId, distinctRoleIds));
        if (validRoleCount != distinctRoleIds.size()) {
            throw new BusinessException("存在无效角色");
        }
        for (Long roleId : distinctRoleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    private UserVO toUserVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo, "password");

        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        vo.setRoleIds(roleIds);

        if (!roleIds.isEmpty()) {
            Map<Long, SysRole> roleMap = roleMapper.selectBatchIds(roleIds).stream()
                    .collect(Collectors.toMap(SysRole::getId, role -> role));
            vo.setRoleNames(roleIds.stream()
                    .map(roleMap::get)
                    .filter(Objects::nonNull)
                    .map(SysRole::getRoleName)
                    .toList());
            vo.setRoleCodes(roleIds.stream()
                    .map(roleMap::get)
                    .filter(Objects::nonNull)
                    .map(SysRole::getRoleCode)
                    .toList());
        } else {
            vo.setRoleNames(List.of());
            vo.setRoleCodes(List.of());
        }

        // Fill communityName
        if (user.getCommunityId() != null) {
            Community community = communityMapper.selectById(user.getCommunityId());
            if (community != null) {
                vo.setCommunityName(community.getName());
            }
        }

        return vo;
    }
}
