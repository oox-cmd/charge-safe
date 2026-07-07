package com.chargesafe.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT r.role_code FROM sys_user_role ur " +
            "JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0")
    Set<String> selectRoleCodesByUserId(Long userId);

    @Select("SELECT m.permission_code FROM sys_role_menu rm " +
            "JOIN sys_menu m ON rm.menu_id = m.id " +
            "JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.permission_code IS NOT NULL AND m.permission_code != ''")
    Set<String> selectPermissionsByUserId(Long userId);

    @Select("SELECT community_id FROM sys_user WHERE id = #{userId}")
    Long selectCommunityIdById(Long userId);
}
