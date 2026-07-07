<template>
  <view class="page">
    <view class="hero-card">
      <text class="hero-bolt">⚡</text>
      <text class="hero-status">充电中</text>
      <view class="timer-block">
        <text class="timer">{{ duration }}</text>
        <text class="timer-label">已充电时长</text>
      </view>
    </view>
    <view class="info-card">
      <view class="row"><text class="lbl">订单号</text><text class="val">{{ order.orderNo }}</text></view>
      <view class="row"><text class="lbl">开始时间</text><text class="val">{{ order.startTime }}</text></view>
      <view class="row"><text class="lbl">当前费用</text><text class="val cost">¥{{ amount }}</text></view>
    </view>
    <button class="btn-stop" @click="handleStop">结束充电</button>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
const order = ref({ orderNo: 'ORD202607040001', startTime: '2026-07-04 08:00', status: 'CHARGING' })
const elapsed = ref(0); let timer = null
const duration = computed(() => { const h = Math.floor(elapsed.value / 3600); const m = Math.floor((elapsed.value % 3600) / 60); const s = elapsed.value % 60; return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}` })
const amount = computed(() => Math.max(0.50, (elapsed.value / 3600) * 1.5).toFixed(2))
const handleStop = () => uni.showModal({ title: '确认结束', content: '确定要结束充电吗？', success: (res) => { if (res.confirm) { uni.showToast({ title: '充电已结束', icon: 'success' }); setTimeout(() => uni.navigateBack(), 1500) } } })
onMounted(() => { timer = setInterval(() => elapsed.value++, 1000) })
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 24rpx 28rpx; }
.hero-card { background: linear-gradient(150deg, #1e3a8a 0%, #1e40af 40%, #2563eb 100%); border-radius: 24rpx; padding: 60rpx 32rpx; text-align: center; margin-bottom: 24rpx; }
.hero-bolt { font-size: 80rpx; display: block; }
.hero-status { font-size: 32rpx; font-weight: 700; color: #fbbf24; margin-top: 12rpx; display: block; }
.timer-block { margin-top: 40rpx; }
.timer { font-size: 72rpx; font-weight: 800; color: #fff; letter-spacing: 6rpx; display: block; font-variant-numeric: tabular-nums; }
.timer-label { font-size: 24rpx; color: rgba(255,255,255,.6); margin-top: 10rpx; display: block; }
.info-card { background: #fff; border-radius: 22rpx; padding: 28rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.row { display: flex; justify-content: space-between; padding: 20rpx 0; border-bottom: 2rpx solid #f8fafc; }
.row:last-child { border-bottom: none; }
.lbl { font-size: 28rpx; color: #64748b; }
.val { font-size: 28rpx; font-weight: 600; color: #1e293b; }
.val.cost { font-size: 36rpx; color: #ef4444; }
.btn-stop { width: 100%; height: 96rpx; background: #fff; color: #ef4444; border: 2rpx solid #fecaca; border-radius: 20rpx; font-size: 32rpx; font-weight: 600; }
</style>
