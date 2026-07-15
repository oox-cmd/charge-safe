<template>
  <view class="page">
    <view :class="['status-bar', order.status]">
      <text class="status-icon">{{ iconMap[order.status] || '📄' }}</text>
      <text class="status-text">{{ statusMap[order.status] || order.status }}</text>
    </view>

    <view class="card">
      <view class="row"><text class="lbl">订单号</text><text class="val">{{ order.orderNo }}</text></view>
      <view class="row"><text class="lbl">端口</text><text class="val">{{ order.portName || '—' }}</text></view>
      <view class="row"><text class="lbl">开始时间</text><text class="val">{{ formatTime(order.startTime) }}</text></view>
      <view class="row" v-if="order.endTime"><text class="lbl">结束时间</text><text class="val">{{ formatTime(order.endTime) }}</text></view>
      <view class="row" v-if="order.durationMinutes != null"><text class="lbl">充电时长</text><text class="val">{{ order.durationMinutes }} 分钟</text></view>
      <view class="row"><text class="lbl">单价</text><text class="val">¥1.50 / 小时</text></view>
      <view class="row"><text class="lbl">金额</text><text class="val cost">¥{{ order.amount != null ? order.amount : '—' }}</text></view>
    </view>

    <button v-if="order.status === 'CHARGING'" class="btn-stop" @click="handleStop">结束充电</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { finishCharging } from '@/api/charging'
import { getMyOrderById } from '@/api/order'

const order = ref({})
const statusMap = { 'CHARGING': '充电中', 'COMPLETED': '已完成', 'SETTLED': '已结算', 'CANCELLED': '已取消' }
const iconMap = { 'CHARGING': '⚡', 'COMPLETED': '✅', 'SETTLED': '💰', 'CANCELLED': '❌' }

function formatTime(iso) {
  if (!iso) return '--'
  try { return iso.replace('T', ' ').substring(0, 16) } catch { return iso }
}

function handleStop() {
  const orderId = order.value.id
  if (!orderId) return
  uni.showModal({
    title: '确认结束', content: '确定要结束充电吗？',
    confirmText: '确认结束', confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          const r = await finishCharging(orderId)
          if (r.code === 200) {
            order.value.status = 'COMPLETED'
            uni.showToast({ title: '充电已结束', icon: 'success' })
          }
        } catch { /* 错误由 request.js 统一 toast */ }
      }
    }
  })
}

onLoad(async (options) => {
  const id = options.id ? Number(options.id) : null
  if (!id) return

  try {
    const res = await getMyOrderById(id)
    if (res.code === 200 && res.data) {
      order.value = res.data
    }
  } catch { /* 错误由 request.js 统一 toast */ }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 24rpx 28rpx; }
.status-bar { text-align: center; padding: 60rpx 32rpx; border-radius: 24rpx; margin-bottom: 24rpx; }
.status-bar.CHARGING { background: linear-gradient(150deg, #e67e22, #f59e0b); }
.status-bar.COMPLETED { background: linear-gradient(150deg, #2e7d32, #43a047); }
.status-bar.SETTLED { background: linear-gradient(150deg, #1e3a8a, #2563eb); }
.status-bar.CANCELLED { background: linear-gradient(150deg, #94a3b8, #b0bec5); }
.status-icon { font-size: 64rpx; display: block; }
.status-text { font-size: 34rpx; font-weight: 800; color: #fff; margin-top: 16rpx; display: block; }
.card { background: #fff; border-radius: 22rpx; padding: 28rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); margin-bottom: 24rpx; }
.row { display: flex; justify-content: space-between; padding: 20rpx 0; border-bottom: 2rpx solid #f8fafc; }
.row:last-child { border-bottom: none; }
.lbl { font-size: 28rpx; color: #64748b; }
.val { font-size: 28rpx; font-weight: 600; color: #1e293b; }
.val.cost { font-size: 36rpx; color: #ef4444; }
.btn-stop { width: 100%; height: 88rpx; background: #fff; color: #ef4444; border: 2rpx solid #fecaca; border-radius: 18rpx; font-size: 30rpx; font-weight: 600; }
</style>
