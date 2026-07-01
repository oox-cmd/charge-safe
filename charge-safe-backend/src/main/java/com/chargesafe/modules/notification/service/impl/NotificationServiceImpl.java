package com.chargesafe.modules.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.notification.service.NotificationService;
import com.chargesafe.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

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
                .eq(isRead != null, Notification::getIsRead, isRead ? 1 : 0)
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
}
