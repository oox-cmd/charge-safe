package com.chargesafe.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.user.entity.SysDept;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysDeptMapper;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private static final long ROOT_DEPT_ID = 1L;

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public SysDept create(SysDept input) {
        normalize(input);
        validateParent(null, input.getParentId());
        ensureCodeUnique(input.getCode(), null);

        input.setId(null);
        input.setDeleted(null);
        input.setCreateTime(null);
        input.setUpdateTime(null);
        if (input.getStatus() == null) {
            input.setStatus(1);
        }
        ensureStatusValue(input.getStatus());
        deptMapper.insert(input);
        return input;
    }

    @Override
    @Transactional
    public SysDept update(Long id, SysDept input) {
        SysDept existing = getRequired(id);
        normalize(input);
        validateParent(id, input.getParentId());
        ensureCodeUnique(input.getCode(), id);

        existing.setParentId(input.getParentId());
        existing.setName(input.getName());
        existing.setCode(input.getCode());
        existing.setLeader(input.getLeader());
        existing.setPhone(input.getPhone());
        existing.setSort(input.getSort());
        if (input.getStatus() != null) {
            ensureStatusValue(input.getStatus());
            existing.setStatus(input.getStatus());
        }
        deptMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (Long.valueOf(ROOT_DEPT_ID).equals(id)) {
            throw new BusinessException("根部门不能删除");
        }
        getRequired(id);
        if (deptMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, id)) > 0) {
            throw new BusinessException("部门下仍有子部门，不能删除");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, id)) > 0) {
            throw new BusinessException("部门仍有绑定用户，不能删除");
        }
        deptMapper.deleteById(id);
    }

    private SysDept getRequired(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return dept;
    }

    private void validateParent(Long currentId, Long parentId) {
        if (parentId == null) {
            return;
        }
        if (Objects.equals(currentId, parentId)) {
            throw new BusinessException("部门不能设置为自己的上级部门");
        }

        Set<Long> visited = new HashSet<>();
        Long cursor = parentId;
        while (cursor != null) {
            if (!visited.add(cursor) || Objects.equals(currentId, cursor)) {
                throw new BusinessException("上级部门关系不能形成循环");
            }
            SysDept parent = deptMapper.selectById(cursor);
            if (parent == null || !Integer.valueOf(1).equals(parent.getStatus())) {
                throw new BusinessException("上级部门不存在或已停用");
            }
            cursor = parent.getParentId();
        }
    }

    private void ensureCodeUnique(String code, Long ignoredId) {
        Long count = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getCode, code)
                .ne(ignoredId != null, SysDept::getId, ignoredId));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST);
        }
    }

    private void ensureStatusValue(Integer status) {
        if (!Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void normalize(SysDept dept) {
        dept.setName(trim(dept.getName()));
        dept.setCode(trim(dept.getCode()));
        dept.setLeader(trim(dept.getLeader()));
        dept.setPhone(trim(dept.getPhone()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
