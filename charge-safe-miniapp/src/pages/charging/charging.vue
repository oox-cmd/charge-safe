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
      <view class="row"><text class="lbl">端口</text><text class="val">{{ order.portName || '—' }}</text></view>
      <view class="row"><text class="lbl">车棚</text><text class="val">{{ order.parkingName || '—' }}</text></view>
      <view class="row"><text class="lbl">开始时间</text><text class="val">{{ formatTime(order.startTime) }}</text></view>
      <view class="row"><text class="lbl">当前费用</text><text class="val cost">¥{{ amount }}</text></view>
    </view>

    <button class="btn-stop" :disabled="stopping" @click="handleStop">结束充电</button>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { finishCharging } from '@/api/charging'
import { getMyOrderById } from '@/api/order'

const order = ref({})
const orderId = ref(null)
const elapsed = ref(0)
const stopping = ref(false)
let timer = null

const duration = computed(() => {
  const h = Math.floor(elapsed.value / 3600)
  const m = Math.floor((elapsed.value % 3600) / 60)
  const s = elapsed.value % 60
  return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
})

const amount = computed(() => Math.max(0.50, (elapsed.value / 3600) * 1.5).toFixed(2))

function formatTime(iso) {
  if (!iso) return '--'
  try { return iso.replace('T', ' ').substring(0, 16) } catch { return iso }
}

async function handleStop() {
  uni.showModal({
    title: '确认结束', content: '确定要结束充电吗？结束后将生成订单。',
    confirmText: '确认结束', confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm && orderId.value) {
        stopping.value = true
        try {
          const r = await finishCharging(orderId.value)
          if (r.code === 200) {
            if (timer) clearInterval(timer)
            uni.showToast({ title: '充电已结束', icon: 'success' })
            setTimeout(() => uni.redirectTo({ url: `/pages/order/detail?id=${orderId.value}` }), 1000)
          }
        } catch { /* 错误由 request.js 统一 toast */ }
        stopping.value = false
      }
    }
  })
}

onLoad(async (options) => {
  orderId.value = options.orderId ? Number(options.orderId) : null
  if (!orderId.value) return

  try {
    const res = await getMyOrderById(orderId.value)
    if (res.code === 200 && res.data) {
      order.value = res.data
      if (res.data.startTime) {
        const start = new Date(res.data.startTime).getTime()
        const now = Date.now()
        elapsed.value = Math.max(0, Math.floor((now - start) / 1000))
      }
      timer = setInterval(() => elapsed.value++, 1000)
    }
  } catch { /* 错误由 request.js 统一 toast */ }
})

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
.btn-stop:disabled { opacity: 0.5; }
</style>
