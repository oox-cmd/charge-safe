<template>
  <view class="page">
    <view class="all-read-bar" v-if="hasUnread" @click="markAllRead"><text class="all-read-text">全部标为已读</text></view>
    <view :class="['card', { fresh: !item.isRead }]" v-for="item in notifications" :key="item.id" @click="openMsg(item)">
      <view class="top">
        <view class="left"><view class="dot" v-if="!item.isRead"></view><text :class="['title', { bold: !item.isRead }]">{{ item.title }}</text></view>
        <text class="time">{{ item.createdTime }}</text>
      </view>
      <text class="body">{{ item.content }}</text>
    </view>
    <view class="empty-block" v-if="!loading && notifications.length === 0"><text>暂无消息</text></view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { markAllNotificationsRead, markNotificationRead, pageMyNotifications } from '@/api/notification'

const notifications = ref([])
const loading = ref(false)
const hasUnread = computed(() => notifications.value.some((item) => !item.isRead))

async function fetchNotifications() {
  loading.value = true
  try {
    const res = await pageMyNotifications({ page: 1, size: 50 })
    if (res.code === 200 && Array.isArray(res.data?.records)) {
      notifications.value = res.data.records.map((item) => ({ ...item, isRead: Number(item.isRead) === 1 }))
    }
  } catch {
    // request.js 已统一展示错误提示
  } finally {
    loading.value = false
  }
}

async function openMsg(item) {
  if (item.isRead) return
  try {
    const res = await markNotificationRead(item.id)
    if (res.code === 200) item.isRead = true
  } catch {
    // request.js 已统一展示错误提示
  }
}

async function markAllRead() {
  try {
    const res = await markAllNotificationsRead()
    if (res.code === 200) notifications.value.forEach((item) => { item.isRead = true })
  } catch {
    // request.js 已统一展示错误提示
  }
}

onShow(fetchNotifications)
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
.title { font-size: 28rpx; color: #1e293b; }.title.bold { font-weight: 700; }
.time { font-size: 22rpx; color: #94a3b8; flex-shrink: 0; margin-left: 16rpx; }
.body { font-size: 26rpx; color: #64748b; line-height: 1.6; display: block; }
.empty-block { text-align: center; padding: 100rpx 0; color: #94a3b8; font-size: 28rpx; }
</style>
