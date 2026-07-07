package com.chargesafe.modules.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.notification.service.NotificationService;
import com.chargesafe.modules2.notification.dto.NotificationCreateDTO;
import com.chargesafe.modules2.notification.dto.NotificationQueryDTO;
import com.chargesafe.modules2.notification.dto.NotificationUpdateDTO;
import com.chargesafe.modules2.notification.vo.NotificationVO;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResult<Notification> pageMyNotifications(String type, Boolean isRead, PageQuery pageQuery) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Page<Notification> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(StringUtils.hasText(type), Notification::getType, type)
                .eq(isRead != null, Notification::getIsRead, Boolean.TRUE.equals(isRead) ? 1 : 0)
                .orderByDesc(Notification::getCreatedTime);

        Page<Notification> result = notificationMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public void markAsRead(Long notificationId) {
        Long userId = UserContext.getUserId();
        Notification notification = notificationMapper.selectById(notificationId);

        if (notification == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    @Override
    public void markAllAsRead() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return;
        }

        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
                        .set(Notification::getIsRead, 1)
        );
    }

    @Override
    public Map<String, Long> countUnread() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Map.of("total", 0L);
        }

        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );

        Map<String, Long> result = new HashMap<>();
        result.put("total", count);
        return result;
    }

    @Override
    public PageResult<NotificationVO> pageList(NotificationQueryDTO query, PageQuery pageQuery) {
        Page<Notification> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(Notification::getTitle, query.getTitle());
        }
        if (query.getType() != null) {
            wrapper.eq(Notification::getType, query.getType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Notification::getIsRead, query.getStatus());
        }
        if (query.getReceiverId() != null) {
            wrapper.eq(Notification::getUserId, query.getReceiverId());
        }

        wrapper.orderByDesc(Notification::getCreatedTime);
        Page<Notification> result = notificationMapper.selectPage(page, wrapper);

        List<NotificationVO> voList = result.getRecords().stream().map(this::convertToVO).toList();
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public NotificationVO create(NotificationCreateDTO dto) {
        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setType(dto.getType().toString());
        notification.setUserId(dto.getReceiverId());
        notification.setIsRead(0);
        notification.setCreatedTime(LocalDateTime.now());

        notificationMapper.insert(notification);
        return convertToVO(notification);
    }

    @Override
    public NotificationVO update(Long id, NotificationUpdateDTO dto) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        if (dto.getTitle() != null) {
            notification.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            notification.setContent(dto.getContent());
        }
        if (dto.getType() != null) {
            notification.setType(dto.getType().toString());
        }
        if (dto.getStatus() != null) {
            notification.setIsRead(dto.getStatus());
        }
        if (dto.getReceiverId() != null) {
            notification.setUserId(dto.getReceiverId());
        }

        notificationMapper.updateById(notification);
        return convertToVO(notification);
    }

    @Override
    public void delete(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        notificationMapper.deleteById(id);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return 0L;
        }
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );
    }

    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);
        vo.setId(notification.getId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setType(notification.getType() != null ? Integer.parseInt(notification.getType()) : null);
        vo.setStatus(notification.getIsRead());
        vo.setReceiverId(notification.getUserId());
        vo.setCreateTime(notification.getCreatedTime());

        if (notification.getUserId() != null) {
            SysUser user = userMapper.selectById(notification.getUserId());
            if (user != null) {
                vo.setReceiverName(user.getNickname());
            }
        }

        return vo;
    }
}
