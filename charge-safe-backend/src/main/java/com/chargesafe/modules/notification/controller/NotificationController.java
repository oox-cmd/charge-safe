package com.chargesafe.modules.notification.controller;

import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "消息通知管理")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "分页查询当前用户的通知")
    @GetMapping
    public Result<PageResult<Notification>> pageMyNotifications(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead,
            PageQuery pageQuery) {
        return Result.success(notificationService.pageMyNotifications(type, isRead, pageQuery));
    }

    @Operation(summary = "标记通知为已读")
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "标记所有通知为已读")
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }

    @Operation(summary = "统计未读通知数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> countUnread() {
        return Result.success(notificationService.countUnread());
    }
}
