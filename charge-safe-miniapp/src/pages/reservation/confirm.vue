<template>
  <view class="page">
    <view class="card">
      <text class="card-title">确认预约</text>
      <view class="row"><text class="lbl">端口</text><text class="val">{{ portName }}</text></view>
      <view class="row"><text class="lbl">有效期</text><text class="val">30 分钟</text></view>

      <view class="row"><text class="lbl">说明</text><text class="val hint">预约成功后，请在有效期内前往「我的预约」开始充电</text></view>
    </view>
    <view class="actions">
      <button :loading="loading" class="btn-ok" @click="handleConfirm">确认预约</button>
      <button class="btn-no" @click="goBack">取消</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createReservation } from '@/api/charging'

const portId = ref('')
const portName = ref('')
const loading = ref(false)
async function handleConfirm() {
  if (!portId.value) return
  loading.value = true
  try {
    const res = await createReservation(Number(portId.value))
    if (res.code === 200) {
      uni.showToast({ title: '预约成功', icon: 'success' })
      setTimeout(() => uni.redirectTo({ url: '/pages/reservation/index' }), 1200)
    }
  } catch { /* 错误由 request.js 统一 toast */ }
  loading.value = false
}

function goBack() { uni.navigateBack() }

onLoad((options) => {
  portId.value = options.portId || ''
  portName.value = options.portName || ''
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 40rpx 28rpx; }
.card { background: #fff; border-radius: 22rpx; padding: 36rpx 28rpx; margin-bottom: 36rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.card-title { font-size: 34rpx; font-weight: 800; color: #1e3a8a; display: block; margin-bottom: 32rpx; }
.row { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; border-bottom: 2rpx solid #f8fafc; }
.lbl { font-size: 28rpx; color: #64748b; }
.val { font-size: 28rpx; font-weight: 600; color: #1e293b; }
.val.hint { font-size: 24rpx; color: #e67e22; font-weight: 400; }

.actions { padding: 0; }
.btn-ok { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af, #1d4ed8); color: #fff; border: none; border-radius: 18rpx; font-size: 32rpx; font-weight: 600; box-shadow: 0 8rpx 24rpx rgba(30,64,175,.22); margin-bottom: 20rpx; }
.btn-no { width: 100%; height: 88rpx; background: #fff; color: #64748b; border: 2rpx solid #e2e8f0; border-radius: 18rpx; font-size: 30rpx; }
</style>
