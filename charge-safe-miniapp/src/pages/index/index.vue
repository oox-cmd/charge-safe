<template>
  <view class="index-page">
    <!-- 顶部信息 -->
    <view class="header">
      <view class="user-info">
        <text class="greeting">您好，{{ userInfo.nickname || '居民' }}</text>
        <text class="community">阳光花园小区</text>
      </view>
      <view class="notification" @click="goNotification">
        <text class="badge" v-if="unreadCount > 0">{{ unreadCount }}</text>
        <text class="icon">🔔</text>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="features">
      <view class="feature-item" @click="goParking">
        <text class="feature-icon">⚡</text>
        <text class="feature-text">充电车棚</text>
      </view>
      <view class="feature-item" @click="goReservation">
        <text class="feature-icon">📋</text>
        <text class="feature-text">我的预约</text>
      </view>
      <view class="feature-item" @click="goOrder">
        <text class="feature-icon">📄</text>
        <text class="feature-text">充电订单</text>
      </view>
      <view class="feature-item" @click="goRepair">
        <text class="feature-icon">🔧</text>
        <text class="feature-text">设备报修</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-card" @click="goParking">
        <view class="action-info">
          <text class="action-title">查看空闲端口</text>
          <text class="action-desc">查看附近车棚可用充电端口</text>
        </view>
        <text class="action-arrow">→</text>
      </view>
      <view class="action-card" @click="goScan">
        <view class="action-info">
          <text class="action-title">扫码充电</text>
          <text class="action-desc">扫描端口二维码开始充电</text>
        </view>
        <text class="action-arrow">→</text>
      </view>
    </view>

    <!-- 最近订单 -->
    <view class="recent-orders">
      <view class="section-header">
        <text class="section-title">最近订单</text>
        <text class="section-more" @click="goOrder">查看更多</text>
      </view>
      <view class="order-list">
        <view class="order-item" v-for="order in recentOrders" :key="order.id">
          <view class="order-info">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="order-time">{{ order.startTime }}</text>
          </view>
          <view class="order-status">
            <text :class="['status-tag', order.status]">{{ getStatusText(order.status) }}</text>
            <text class="order-amount" v-if="order.amount">¥{{ order.amount }}</text>
          </view>
        </view>
        <view class="empty-tip" v-if="recentOrders.length === 0">
          <text>暂无订单记录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const userInfo = ref({})
const unreadCount = ref(3)
const recentOrders = ref([
  { id: 1, orderNo: 'ORD202601010001', startTime: '2026-01-01 08:00', status: 'COMPLETED', amount: '3.75' },
  { id: 2, orderNo: 'ORD202601020001', startTime: '2026-01-02 09:30', status: 'CHARGING', amount: null }
])

const getStatusText = (status) => {
  const map = { 'CHARGING': '充电中', 'COMPLETED': '已完成', 'SETTLED': '已结算' }
  return map[status] || status
}

const goParking = () => uni.navigateTo({ url: '/pages/parking/index' })
const goReservation = () => uni.navigateTo({ url: '/pages/reservation/index' })
const goOrder = () => uni.navigateTo({ url: '/pages/order/index' })
const goRepair = () => uni.navigateTo({ url: '/pages/repair/index' })
const goNotification = () => uni.navigateTo({ url: '/pages/notification/index' })
const goScan = () => {
  uni.scanCode({
    success: (res) => {
      uni.showToast({ title: '扫码成功: ' + res.result, icon: 'none' })
    }
  })
}

onMounted(() => {
  const info = uni.getStorageSync('userInfo')
  if (info) userInfo.value = info
})
</script>

<style scoped>
.index-page {
  padding: 20rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background: #409eff;
  border-radius: 16rpx;
  color: #fff;
  margin-bottom: 30rpx;
}
.greeting {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
}
.community {
  font-size: 24rpx;
  opacity: 0.8;
  margin-top: 8rpx;
  display: block;
}
.notification {
  position: relative;
}
.icon {
  font-size: 40rpx;
}
.badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  background: #f56c6c;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 20rpx;
}
.features {
  display: flex;
  justify-content: space-around;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx 0;
  margin-bottom: 30rpx;
}
.feature-item {
  text-align: center;
}
.feature-icon {
  font-size: 48rpx;
  display: block;
}
.feature-text {
  font-size: 24rpx;
  color: #666;
  margin-top: 10rpx;
  display: block;
}
.quick-actions {
  margin-bottom: 30rpx;
}
.action-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}
.action-title {
  font-size: 30rpx;
  font-weight: bold;
  display: block;
}
.action-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}
.action-arrow {
  font-size: 32rpx;
  color: #999;
}
.recent-orders {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}
.section-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: bold;
}
.section-more {
  font-size: 24rpx;
  color: #409eff;
}
.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1px solid #f0f0f0;
}
.order-no {
  font-size: 28rpx;
  display: block;
}
.order-time {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}
.status-tag {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  display: block;
}
.status-tag.CHARGING {
  background: #e6a23c;
  color: #fff;
}
.status-tag.COMPLETED {
  background: #67c23a;
  color: #fff;
}
.order-amount {
  font-size: 28rpx;
  color: #f56c6c;
  margin-top: 8rpx;
  display: block;
}
.empty-tip {
  text-align: center;
  padding: 40rpx;
  color: #999;
}
</style>
