<template>
  <view class="notification-list-page">
    <view class="notification-item" v-for="item in notifications" :key="item.id" :class="{ unread: !item.isRead }">
      <view class="notif-header">
        <text class="notif-title">{{ item.title }}</text>
        <text class="notif-time">{{ item.createdTime }}</text>
      </view>
      <text class="notif-content">{{ item.content }}</text>
    </view>
    <view class="empty-tip" v-if="notifications.length === 0">
      <text>暂无消息</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const notifications = ref([
  { id: 1, title: '预约成功通知', content: '您已成功预约1号端口，请在30分钟内到达扫码充电。', isRead: false, createdTime: '2026-07-04 08:00' },
  { id: 2, title: '充电完成通知', content: '您的充电已完成，时长120分钟，费用3.00元。', isRead: true, createdTime: '2026-07-03 16:00' }
])
</script>

<style scoped>
.notification-list-page {
  padding: 20rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.notification-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  border-left: 4px solid transparent;
}
.notification-item.unread {
  border-left-color: #409eff;
}
.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}
.notif-title {
  font-size: 28rpx;
  font-weight: bold;
}
.notif-time {
  font-size: 22rpx;
  color: #999;
}
.notif-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}
.empty-tip {
  text-align: center;
  padding: 80rpx;
  color: #999;
}
</style>
