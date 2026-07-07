<template>
  <view class="page">
    <view :class="['card', { fresh: !item.isRead }]" v-for="item in notifications" :key="item.id">
      <view class="top"><text class="title">{{ item.title }}</text><text class="time">{{ item.createdTime }}</text></view>
      <text class="body">{{ item.content }}</text>
    </view>
    <view class="empty-block" v-if="notifications.length === 0"><text>暂无消息</text></view>
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
.page { min-height: 100vh; background: #f0f4f8; padding: 24rpx 28rpx; }
.card { background: #fff; border-radius: 22rpx; padding: 28rpx; margin-bottom: 18rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); border-left: 6rpx solid transparent; }
.card.fresh { border-left-color: #2563eb; background: #fafcff; }
.top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14rpx; }
.title { font-size: 28rpx; font-weight: 700; color: #1e293b; }
.time { font-size: 22rpx; color: #94a3b8; }
.body { font-size: 26rpx; color: #64748b; line-height: 1.6; display: block; }
.empty-block { text-align: center; padding: 100rpx 0; color: #94a3b8; font-size: 28rpx; }
</style>
