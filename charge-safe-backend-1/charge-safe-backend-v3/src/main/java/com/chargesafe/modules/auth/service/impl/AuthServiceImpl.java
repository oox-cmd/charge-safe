package com.chargesafe.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.RedisKeys;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.auth.dto.LoginDTO;
import com.chargesafe.modules.auth.dto.RegisterDTO;
import com.chargesafe.modules.auth.dto.ResetPasswordDTO;
import com.chargesafe.modules.auth.dto.SmsLoginDTO;
import com.chargesafe.modules.auth.service.AuthService;
import com.chargesafe.modules.auth.vo.LoginVO;
import com.chargesafe.modules.auth.vo.PermissionVO;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.entity.SysUserRole;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.mapper.SysUserRoleMapper;
import com.chargesafe.modules.user.mapper.SysMenuMapper;
import com.chargesafe.modules.user.entity.SysMenu;
import com.chargesafe.security.JwtUtils;
import com.chargesafe.security.LoginUser;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        return buildLoginVO(user);
    }

    @Override
    public LoginVO smsLogin(SmsLoginDTO dto) {
        String cacheCode = redisTemplate.opsForValue().get(RedisKeys.smsCode(dto.getPhone()));
        if (cacheCode == null) {
            throw new BusinessException(ResultCode.SMS_CODE_EXPIRED);
        }
        if (!cacheCode.equals(dto.getCode())) {
            throw new BusinessException(ResultCode.SMS_CODE_ERROR);
        }
        redisTemplate.delete(RedisKeys.smsCode(dto.getPhone()));

        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, dto.getPhone()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return buildLoginVO(user);
    }

    @Override
    public void sendSmsCode(String phone) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(RedisKeys.smsCode(phone), code, 5, TimeUnit.MINUTES);
        log.info("短信验证码: phone={}, code={}", phone, code);
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }

        Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, dto.getPhone()));
        if (phoneCount > 0) {
            throw new BusinessException(ResultCode.PHONE_EXIST);
        }

        String cacheCode = redisTemplate.opsForValue().get(RedisKeys.smsCode(dto.getPhone()));
        if (cacheCode == null) {
            throw new BusinessException(ResultCode.SMS_CODE_EXPIRED);
        }
        if (!cacheCode.equals(dto.getCode())) {
            throw new BusinessException(ResultCode.SMS_CODE_ERROR);
        }
        redisTemplate.delete(RedisKeys.smsCode(dto.getPhone()));

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setCommunityId(dto.getCommunityId());
        user.setStatus(1);
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(5L);
        userRoleMapper.insert(userRole);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDTO dto) {
        String cacheCode = redisTemplate.opsForValue().get(RedisKeys.smsCode(dto.getPhone()));
        if (cacheCode == null) {
            throw new BusinessException(ResultCode.SMS_CODE_EXPIRED);
        }
        if (!cacheCode.equals(dto.getCode())) {
            throw new BusinessException(ResultCode.SMS_CODE_ERROR);
        }
        redisTemplate.delete(RedisKeys.smsCode(dto.getPhone()));

        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, dto.getPhone()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public LoginVO me() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return buildLoginVO(user);
    }

    @Override
    public PermissionVO getPermissions() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Set<String> roles = userMapper.selectRoleCodesByUserId(userId);
        Set<String> permissions = userMapper.selectPermissionsByUserId(userId);
        List<SysMenu> menuList = menuMapper.selectListByUserId(userId);

        List<PermissionVO.MenuVO> menuTree = buildMenuTree(menuList);

        return PermissionVO.builder()
                .roles(new ArrayList<>(roles))
                .permissions(new ArrayList<>(permissions))
                .menus(menuTree)
                .build();
    }

    private LoginVO buildLoginVO(SysUser user) {
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        Set<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        Set<String> permissions = userMapper.selectPermissionsByUserId(user.getId());

        LoginVO.UserInfo userInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .communityId(user.getCommunityId())
                .roles(roles != null ? new ArrayList<>(roles) : new ArrayList<>())
                .permissions(permissions != null ? new ArrayList<>(permissions) : new ArrayList<>())
                .build();

        return LoginVO.builder()
                .token(token)
                .user(userInfo)
                .build();
    }

    private List<PermissionVO.MenuVO> buildMenuTree(List<SysMenu> menuList) {
        Map<Long, List<PermissionVO.MenuVO>> parentMap = new LinkedHashMap<>();
        for (SysMenu menu : menuList) {
            PermissionVO.MenuVO vo = PermissionVO.MenuVO.builder()
                    .id(menu.getId())
                    .parentId(menu.getParentId())
                    .name(menu.getName())
                    .path(menu.getPath())
                    .component(menu.getComponent())
                    .icon(menu.getIcon())
                    .sort(menu.getSort() != null ? menu.getSort() : 0)
                    .perms(menu.getPerms())
                    .type(menu.getType())
                    .children(new ArrayList<>())
                    .build();
            parentMap.computeIfAbsent(menu.getParentId() != null ? menu.getParentId() : 0,
                    k -> new ArrayList<>()).add(vo);
        }

        List<PermissionVO.MenuVO> result = new ArrayList<>();
        for (PermissionVO.MenuVO menu : parentMap.getOrDefault(0L, new ArrayList<>())) {
            fillChildren(menu, parentMap);
            result.add(menu);
        }
        result.sort(Comparator.comparingInt(PermissionVO.MenuVO::getSort));
        return result;
    }

    private void fillChildren(PermissionVO.MenuVO parent, Map<Long, List<PermissionVO.MenuVO>> parentMap) {
        List<PermissionVO.MenuVO> children = parentMap.get(parent.getId());
        if (children != null) {
            children.sort(Comparator.comparingInt(PermissionVO.MenuVO::getSort));
            parent.setChildren(children);
            for (PermissionVO.MenuVO child : children) {
                fillChildren(child, parentMap);
            }
        }
    }
}
