<template>
  <view class="home">
    <!-- 头部 -->
    <view class="header">
      <view class="header-top">
        <view class="user-row">
          <view class="avatar-sm">{{ userStore.nickname ? userStore.nickname.charAt(0).toUpperCase() : '?' }}</view>
          <view class="user-col">
            <text class="greeting">{{ greeting }}</text>
            <text class="user-name">{{ userStore.nickname || '居民用户' }}</text>
          </view>
        </view>
        <view class="bell-btn" @click="goNotification">
          <text class="bell-icon">🔔</text>
          <view class="bell-badge" v-if="unreadCount > 0"><text>{{ unreadCount > 99 ? '99+' : unreadCount }}</text></view>
        </view>
      </view>
      <view class="location-bar">
        <text class="loc-dot">📍</text>
        <text class="loc-name">{{ assignedCommunityName || '未绑定社区' }}</text>
        <text class="loc-switch">账号所属社区</text>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="feature-row">
      <view class="feat" @click="goParking">
        <view class="feat-icon-box" style="background:#e8f0fe;"><text class="feat-icon">⚡</text></view>
        <text class="feat-text">充电车棚</text>
      </view>
      <view class="feat" @click="goReservation">
        <view class="feat-icon-box" style="background:#fef3e2;"><text class="feat-icon">📋</text></view>
        <text class="feat-text">我的预约</text>
      </view>
      <view class="feat" @click="goOrder">
        <view class="feat-icon-box" style="background:#e8f5e9;"><text class="feat-icon">📄</text></view>
        <text class="feat-text">充电订单</text>
      </view>
      <view class="feat" @click="goRepair">
        <view class="feat-icon-box" style="background:#fce4ec;"><text class="feat-icon">🔧</text></view>
        <text class="feat-text">设备报修</text>
      </view>
    </view>

    <!-- 充电帮助入口 -->
    <view class="help-entry" @click="goAssistant">
      <view class="help-icon">🤖</view>
      <view class="help-text">
        <text class="help-title">充电帮助</text>
        <text class="help-desc">有问题？问我就好了</text>
      </view>
      <text class="help-arrow">→</text>
    </view>

    <!-- 快捷卡片 -->
    <view class="quick">
      <view class="quick-item" @click="goParking">
        <view class="quick-text"><text class="quick-title">查看空闲端口</text><text class="quick-sub">查看附近车棚可用充电端口</text></view>
        <text class="quick-arrow">→</text>
      </view>
      <view class="quick-item" @click="goScan">
        <view class="quick-text"><text class="quick-title">扫码充电</text><text class="quick-sub">扫描端口二维码开始充电</text></view>
        <text class="quick-arrow">→</text>
      </view>
    </view>

    <!-- 最近订单 -->
    <view class="section">
      <view class="section-head">
        <text class="section-title">最近订单</text>
        <text class="section-more" @click="goOrder">查看更多</text>
      </view>
      <view class="order-card" v-for="o in recentOrders" :key="o.id">
        <view class="order-top">
          <text class="order-no">{{ o.orderNo }}</text>
          <text :class="['order-tag', o.status]">{{ statusMap[o.status] || o.status }}</text>
        </view>
        <view class="order-mid"><text class="order-time">{{ o.startTime }}</text></view>
        <text class="order-cost" v-if="o.amount">¥{{ o.amount }}</text>
      </view>
      <view class="empty-block" v-if="recentOrders.length === 0"><text>暂无订单记录</text></view>
    </view>

  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getResidentDashboard } from '@/api/parking'
import { getUnreadNotificationCount } from '@/api/notification'
import { pageMyOrders } from '@/api/order'

const userStore = useUserStore()
const unreadCount = ref(0)
const assignedCommunityName = ref('')
const recentOrders = ref([])
const statusMap = { 'CHARGING': '充电中', 'COMPLETED': '已完成', 'SETTLED': '已结算' }
const greeting = computed(() => { const h = new Date().getHours(); return h < 6 ? '夜深了 🌙' : h < 12 ? '早上好 ☀️' : h < 18 ? '下午好 🌤️' : '晚上好 🌙' })

onMounted(async () => {
  try { const res = await getResidentDashboard(); if (res.code === 200 && res.data) assignedCommunityName.value = res.data.communityName || '' } catch { /* */ }
  try { const res = await getUnreadNotificationCount(); if (res.code === 200 && res.data) unreadCount.value = res.data.total || 0 } catch { /* */ }
  try {
    const res = await pageMyOrders({ page: 1, size: 3 })
    if (res.code === 200 && res.data?.records) recentOrders.value = res.data.records
  } catch { /* */ }
})

const goParking = () => uni.navigateTo({ url: '/pages/parking/index' })
const goReservation = () => uni.navigateTo({ url: '/pages/reservation/index' })
const goOrder = () => uni.navigateTo({ url: '/pages/order/list' })
const goRepair = () => uni.navigateTo({ url: '/pages/repair/list' })
const goNotification = () => uni.navigateTo({ url: '/pages/notification/list' })
const goAssistant = () => uni.navigateTo({ url: '/pages/assistant/index' })
const goScan = () => uni.showModal({ title: '功能未开放', content: '扫码启动充电尚未接入真实订单流程。', showCancel: false })
</script>

<style scoped>
.home { min-height: 100vh; background: #f0f4f8; padding: 0 28rpx 40rpx; }

/* 头部 */
.header { background: linear-gradient(150deg, #1e3a8a 0%, #1e40af 40%, #2563eb 100%); border-radius: 0 0 36rpx 36rpx; padding: 40rpx 32rpx 32rpx; margin: 0 -28rpx 24rpx; }
.header-top { display: flex; justify-content: space-between; align-items: center; }
.user-row { display: flex; align-items: center; }
.avatar-sm { width: 72rpx; height: 72rpx; border-radius: 50%; background: rgba(255,255,255,.2); display: flex; align-items: center; justify-content: center; font-size: 32rpx; font-weight: 700; color: #fff; margin-right: 18rpx; }
.greeting { font-size: 24rpx; color: rgba(255,255,255,.7); display: block; }
.user-name { font-size: 30rpx; font-weight: 600; color: #fff; margin-top: 2rpx; display: block; }
.bell-btn { position: relative; width: 72rpx; height: 72rpx; border-radius: 50%; background: rgba(255,255,255,.12); display: flex; align-items: center; justify-content: center; }
.bell-icon { font-size: 34rpx; }
.bell-badge { position: absolute; top: -4rpx; right: -4rpx; min-width: 32rpx; height: 32rpx; background: #ef4444; border-radius: 16rpx; display: flex; align-items: center; justify-content: center; padding: 0 6rpx; font-size: 18rpx; color: #fff; font-weight: 700; }

.location-bar { display: flex; align-items: center; margin-top: 24rpx; padding: 16rpx 20rpx; background: rgba(255,255,255,.1); border-radius: 16rpx; border: 2rpx solid rgba(255,255,255,.15); }
.loc-dot { font-size: 26rpx; margin-right: 8rpx; }
.loc-name { flex: 1; font-size: 26rpx; color: rgba(255,255,255,.9); }
.loc-switch { font-size: 24rpx; color: rgba(255,255,255,.6); }

/* 功能入口 */
.feature-row { display: flex; justify-content: space-between; margin-bottom: 24rpx; }
.feat { display: flex; flex-direction: column; align-items: center; }
.feat-icon-box { width: 96rpx; height: 96rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; margin-bottom: 10rpx; }
.feat-icon { font-size: 44rpx; }
.feat-text { font-size: 24rpx; color: #475569; font-weight: 500; }

/* 快捷卡片 */
.quick { margin-bottom: 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.quick-item { display: flex; justify-content: space-between; align-items: center; background: #fff; border-radius: 22rpx; padding: 28rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.quick-title { font-size: 30rpx; font-weight: 600; color: #1e293b; display: block; }
.quick-sub { font-size: 24rpx; color: #94a3b8; margin-top: 6rpx; display: block; }
.quick-arrow { font-size: 36rpx; color: #cbd5e1; }

/* 最近订单 */
.section { background: #fff; border-radius: 22rpx; padding: 28rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.section-head { display: flex; justify-content: space-between; margin-bottom: 20rpx; }
.section-title { font-size: 30rpx; font-weight: 700; color: #1e293b; }
.section-more { font-size: 26rpx; color: #2563eb; }
.order-card { padding: 20rpx 0; border-bottom: 2rpx solid #f8fafc; }
.order-card:last-child { border-bottom: none; }
.order-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.order-no { font-size: 28rpx; font-weight: 600; color: #1e293b; }
.order-tag { font-size: 22rpx; padding: 4rpx 14rpx; border-radius: 8rpx; }
.order-tag.CHARGING { background: #fef3e2; color: #e67e22; }
.order-tag.COMPLETED { background: #e8f5e9; color: #2e7d32; }
.order-mid { margin-bottom: 4rpx; }
.order-time { font-size: 24rpx; color: #94a3b8; }
.order-cost { font-size: 30rpx; font-weight: 700; color: #ef4444; }
.empty-block { text-align: center; padding: 40rpx; color: #94a3b8; font-size: 26rpx; }

/* ===== 弹窗 ===== */
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 999; display: flex; align-items: flex-end; }
.sheet { width: 100%; max-height: 70vh; background: #fff; border-radius: 32rpx 32rpx 0 0; display: flex; flex-direction: column; }
.sheet-head { display: flex; justify-content: space-between; align-items: center; padding: 32rpx 32rpx 0; }
.sheet-title { font-size: 34rpx; font-weight: 700; color: #1e3a8a; }
.sheet-close { font-size: 36rpx; color: #94a3b8; padding: 8rpx; }
.search-row { display: flex; align-items: center; margin: 20rpx 32rpx; background: #f8fafc; border-radius: 16rpx; padding: 0 20rpx; height: 80rpx; border: 2rpx solid #e8ecf0; }
.search-icon { font-size: 30rpx; margin-right: 12rpx; }
.search-input { flex: 1; font-size: 28rpx; color: #1e293b; }
.locate-tag { font-size: 24rpx; color: #2563eb; padding: 10rpx 20rpx; background: #eff6ff; border-radius: 10rpx; flex-shrink: 0; margin-left: 12rpx; }
.sheet-list { flex: 1; overflow-y: auto; padding: 0 32rpx 40rpx; }
.sheet-item { display: flex; justify-content: space-between; align-items: center; padding: 28rpx 0; border-bottom: 2rpx solid #f8fafc; }
.sheet-item.on { background: #eff6ff; margin: 0 -10rpx; padding: 28rpx 10rpx; border-radius: 12rpx; border-bottom: none; }
.sheet-name { font-size: 30rpx; font-weight: 600; color: #1e293b; display: block; }
.sheet-addr { font-size: 24rpx; color: #94a3b8; margin-top: 4rpx; display: block; }
.sheet-extra { display: flex; align-items: center; gap: 16rpx; }
.sheet-ports { font-size: 24rpx; color: #2e7d32; }
.sheet-check { font-size: 32rpx; color: #2563eb; font-weight: 700; }
.sheet-empty { text-align: center; padding: 80rpx 0; color: #94a3b8; font-size: 28rpx; }

/* 充电帮助入口 */
.help-entry {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 22rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
}

.help-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.help-text {
  flex: 1;
}

.help-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
  display: block;
}

.help-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4rpx;
  display: block;
}

.help-arrow {
  font-size: 36rpx;
  color: rgba(255, 255, 255, 0.8);
}
</style>
