<template>
  <view class="order-detail-page">
    <view class="detail-card">
      <view class="status-header" :class="order.status">
        <text class="status-icon">{{ getStatusIcon(order.status) }}</text>
        <text class="status-text">{{ getStatusText(order.status) }}</text>
      </view>

      <view class="detail-info">
        <view class="info-row">
          <text class="label">订单号</text>
          <text class="value">{{ order.orderNo }}</text>
        </view>
        <view class="info-row">
          <text class="label">开始时间</text>
          <text class="value">{{ order.startTime }}</text>
        </view>
        <view class="info-row" v-if="order.endTime">
          <text class="label">结束时间</text>
          <text class="value">{{ order.endTime }}</text>
        </view>
        <view class="info-row" v-if="order.durationMinutes">
          <text class="label">充电时长</text>
          <text class="value">{{ order.durationMinutes }}分钟</text>
        </view>
        <view class="info-row">
          <text class="label">费用</text>
          <text class="value price">¥{{ order.amount || '0.00' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const order = ref({})

const getStatusText = (status) => {
  const map = { 'CHARGING': '充电中', 'COMPLETED': '已完成', 'SETTLED': '已结算', 'CANCELLED': '已取消' }
  return map[status] || status
}

const getStatusIcon = (status) => {
  const map = { 'CHARGING': '⚡', 'COMPLETED': '✅', 'SETTLED': '💰', 'CANCELLED': '❌' }
  return map[status] || '📄'
}

onLoad((options) => {
  // 模拟数据
  order.value = {
    id: options.id || 1,
    orderNo: 'ORD202607030001',
    status: 'COMPLETED',
    startTime: '2026-07-03 14:00',
    endTime: '2026-07-03 16:00',
    durationMinutes: 120,
    amount: '3.00'
  }
})
</script>

<style scoped>
.order-detail-page {
  padding: 20rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.detail-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}
.status-header {
  padding: 40rpx;
  text-align: center;
}
.status-header.CHARGING { background: #fdf6ec; }
.status-header.COMPLETED { background: #f0f9eb; }
.status-icon {
  font-size: 60rpx;
  display: block;
}
.status-text {
  font-size: 32rpx;
  font-weight: bold;
  margin-top: 12rpx;
  display: block;
}
.detail-info {
  padding: 30rpx;
}
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1px solid #f0f0f0;
}
.info-row:last-child {
  border-bottom: none;
}
.label {
  font-size: 28rpx;
  color: #666;
}
.value {
  font-size: 28rpx;
  font-weight: bold;
}
.value.price {
  color: #f56c6c;
}
</style>
