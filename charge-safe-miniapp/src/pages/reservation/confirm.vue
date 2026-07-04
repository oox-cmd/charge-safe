<template>
  <view class="reservation-confirm-page">
    <view class="confirm-card">
      <text class="card-title">确认预约</text>

      <view class="info-item">
        <text class="label">端口</text>
        <text class="value">{{ portName }}</text>
      </view>
      <view class="info-item">
        <text class="label">有效期</text>
        <text class="value">30分钟</text>
      </view>
      <view class="info-item">
        <text class="label">说明</text>
        <text class="value tip">请在预约后30分钟内到达扫码充电</text>
      </view>
    </view>

    <view class="action-area">
      <button class="btn-confirm" :loading="loading" @click="handleConfirm">确认预约</button>
      <button class="btn-cancel" @click="goBack">取消</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const portId = ref('')
const portName = ref('')
const loading = ref(false)

const handleConfirm = () => {
  loading.value = true
  // 模拟预约
  setTimeout(() => {
    loading.value = false
    uni.showToast({ title: '预约成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 1000)
}

const goBack = () => {
  uni.navigateBack()
}

// 获取页面参数
onLoad((options) => {
  portId.value = options.portId || ''
  portName.value = options.portName || ''
})
</script>

<style scoped>
.reservation-confirm-page {
  padding: 40rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.confirm-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
}
.card-title {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 40rpx;
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
.value.tip {
  color: #e6a23c;
  font-weight: normal;
  font-size: 24rpx;
}
.action-area {
  padding: 0 20rpx;
}
.btn-confirm {
  background: #409eff;
  color: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}
.btn-cancel {
  background: #fff;
  color: #666;
  border-radius: 12rpx;
}
</style>
