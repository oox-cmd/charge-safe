package com.chargesafe.modules.notification.service;

import com.chargesafe.common.PageResult;
import com.chargesafe.common.PageQuery;

import java.util.Map;

public interface NotificationService {

    /**
     * 分页查询当前用户的通知
     */
    PageResult<com.chargesafe.modules.notification.entity.Notification> pageMyNotifications(
            String type, Boolean isRead, PageQuery pageQuery);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead();

    /**
     * 统计未读通知数量
     */
    Map<String, Long> countUnread();
}
