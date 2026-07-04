<template>
  <view class="charging-page">
    <view class="charging-card">
      <view class="status-icon">⚡</view>
      <text class="status-text">充电中</text>

      <view class="charging-info">
        <view class="info-item">
          <text class="label">订单号</text>
          <text class="value">{{ order.orderNo }}</text>
        </view>
        <view class="info-item">
          <text class="label">开始时间</text>
          <text class="value">{{ order.startTime }}</text>
        </view>
        <view class="info-item">
          <text class="label">已充电时长</text>
          <text class="value highlight">{{ chargingDuration }}</text>
        </view>
        <view class="info-item">
          <text class="label">当前费用</text>
          <text class="value price">¥{{ currentAmount }}</text>
        </view>
      </view>
    </view>

    <view class="action-area">
      <button class="btn-stop" @click="handleStop">结束充电</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const order = ref({
  orderNo: 'ORD202607040001',
  startTime: '2026-07-04 08:00',
  status: 'CHARGING'
})

const elapsedSeconds = ref(0)
let timer = null

const chargingDuration = computed(() => {
  const hours = Math.floor(elapsedSeconds.value / 3600)
  const minutes = Math.floor((elapsedSeconds.value % 3600) / 60)
  const seconds = elapsedSeconds.value % 60
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

const currentAmount = computed(() => {
  const hours = elapsedSeconds.value / 3600
  const amount = Math.max(0.5, hours * 1.5).toFixed(2)
  return amount
})

const handleStop = () => {
  uni.showModal({
    title: '确认结束',
    content: '确定要结束充电吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '充电已结束', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    }
  })
}

onMounted(() => {
  // 模拟计时
  timer = setInterval(() => {
    elapsedSeconds.value++
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.charging-page {
  padding: 40rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.charging-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  margin-bottom: 40rpx;
}
.status-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}
.status-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #e6a23c;
  display: block;
  margin-bottom: 40rpx;
}
.charging-info {
  text-align: left;
}
.info-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1px solid #f0f0f0;
}
.label {
  font-size: 28rpx;
  color: #666;
}
.value {
  font-size: 28rpx;
  font-weight: bold;
}
.value.highlight {
  color: #409eff;
  font-size: 32rpx;
}
.value.price {
  color: #f56c6c;
  font-size: 32rpx;
}
.action-area {
  padding: 0 20rpx;
}
.btn-stop {
  background: #f56c6c;
  color: #fff;
  border-radius: 12rpx;
}
</style>
