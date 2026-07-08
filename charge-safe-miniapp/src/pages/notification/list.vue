<template>
  <view class="page">
    <!-- 全部已读 -->
    <view class="all-read-bar" v-if="hasUnread" @click="markAllRead">
      <text class="all-read-text">全部标为已读</text>
    </view>

    <view :class="['card', { fresh: !item.isRead }]" v-for="item in notifications" :key="item.id" @click="openMsg(item)">
      <view class="top">
        <view class="left">
          <view class="dot" v-if="!item.isRead"></view>
          <text :class="['title', { bold: !item.isRead }]">{{ item.title }}</text>
        </view>
        <text class="time">{{ item.createdTime }}</text>
      </view>
      <text class="body">{{ item.content }}</text>
    </view>
    <view class="empty-block" v-if="notifications.length === 0"><text>暂无消息</text></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
const notifications = ref([
  { id: 1, title: '预约成功通知', content: '您已成功预约1号端口，请在30分钟内到达扫码充电。', isRead: false, createdTime: '2026-07-04 08:00' },
  { id: 2, title: '充电完成通知', content: '您的充电已完成，时长120分钟，费用3.00元。', isRead: true, createdTime: '2026-07-03 16:00' },
  { id: 3, title: '故障提醒', content: '您报修的A1-4号端口已维修完成，可正常使用。', isRead: false, createdTime: '2026-07-02 10:00' }
])
const hasUnread = computed(() => notifications.value.some(n => !n.isRead))

function openMsg(item) { item.isRead = true }
function markAllRead() { notifications.value.forEach(n => n.isRead = true) }
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 24rpx 28rpx; }
.all-read-bar { text-align: right; padding: 0 8rpx 16rpx; }
.all-read-text { font-size: 26rpx; color: #2563eb; }

.card { background: #fff; border-radius: 22rpx; padding: 28rpx; margin-bottom: 18rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.card.fresh { background: #fafcff; }
.top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14rpx; }
.left { display: flex; align-items: center; flex: 1; }
.dot { width: 16rpx; height: 16rpx; border-radius: 50%; background: #2563eb; margin-right: 14rpx; flex-shrink: 0; }
.title { font-size: 28rpx; color: #1e293b; }
.title.bold { font-weight: 700; }
.time { font-size: 22rpx; color: #94a3b8; flex-shrink: 0; margin-left: 16rpx; }
.body { font-size: 26rpx; color: #64748b; line-height: 1.6; display: block; }
.empty-block { text-align: center; padding: 100rpx 0; color: #94a3b8; font-size: 28rpx; }
</style>
