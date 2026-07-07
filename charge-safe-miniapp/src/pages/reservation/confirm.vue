<template>
  <view class="page">
    <view class="card">
      <text class="card-title">确认预约</text>
      <view class="row"><text class="lbl">端口</text><text class="val">{{ portName }}</text></view>
      <view class="row"><text class="lbl">有效期</text><text class="val">30 分钟</text></view>

      <!-- 时段选择 -->
      <view class="row">
        <text class="lbl">开始时间</text>
        <picker mode="time" :value="startTime" @change="onStartChange">
          <view class="val picker-val">{{ startTime || '现在' }}</view>
        </picker>
      </view>
      <view class="row">
        <text class="lbl">预计时长</text>
        <picker :range="durations" @change="onDurChange">
          <view class="val picker-val">{{ selectedDur }}</view>
        </picker>
      </view>

      <!-- 预估费用 -->
      <view class="fee-card">
        <text class="fee-label">预估费用</text>
        <text class="fee-amount">¥{{ estimatedFee }}</text>
        <text class="fee-note">1.50元/小时，最低0.50元</text>
      </view>

      <view class="row"><text class="lbl">说明</text><text class="val hint">请在预约后30分钟内到达扫码充电</text></view>
    </view>
    <view class="actions">
      <button :loading="loading" class="btn-ok" @click="handleConfirm">确认预约</button>
      <button class="btn-no" @click="goBack">取消</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
const portId = ref(''); const portName = ref(''); const loading = ref(false)
const startTime = ref(''); const selectedDur = ref('2 小时')
const durations = ['30 分钟', '1 小时', '1.5 小时', '2 小时', '3 小时', '4 小时']

const estimatedFee = computed(() => {
  const map = { '30 分钟': '0.75', '1 小时': '1.50', '1.5 小时': '2.25', '2 小时': '3.00', '3 小时': '4.50', '4 小时': '6.00' }
  return map[selectedDur.value] || '3.00'
})

function onStartChange(e) { startTime.value = e.detail.value }
function onDurChange(e) { selectedDur.value = durations[e.detail.value] }
function handleConfirm() {
  loading.value = true; setTimeout(() => { loading.value = false; uni.showToast({ title: '预约成功', icon: 'success' }); setTimeout(() => uni.redirectTo({ url: '/pages/reservation/index' }), 1200) }, 1000)
}
function goBack() { uni.navigateBack() }
onLoad((options) => { portId.value = options.portId || ''; portName.value = options.portName || '' })
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 40rpx 28rpx; }
.card { background: #fff; border-radius: 22rpx; padding: 36rpx 28rpx; margin-bottom: 36rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.card-title { font-size: 34rpx; font-weight: 800; color: #1e3a8a; display: block; margin-bottom: 32rpx; }
.row { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; border-bottom: 2rpx solid #f8fafc; }
.lbl { font-size: 28rpx; color: #64748b; }
.val { font-size: 28rpx; font-weight: 600; color: #1e293b; }
.picker-val { color: #2563eb; }
.val.hint { font-size: 24rpx; color: #e67e22; font-weight: 400; }

.fee-card { background: #f8fafc; border-radius: 16rpx; padding: 28rpx; text-align: center; margin: 20rpx 0; border: 2rpx solid #e8ecf0; }
.fee-label { font-size: 26rpx; color: #64748b; display: block; }
.fee-amount { font-size: 56rpx; font-weight: 800; color: #1e3a8a; display: block; margin: 8rpx 0; }
.fee-note { font-size: 22rpx; color: #94a3b8; display: block; }

.actions { padding: 0; }
.btn-ok { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af, #1d4ed8); color: #fff; border: none; border-radius: 18rpx; font-size: 32rpx; font-weight: 600; box-shadow: 0 8rpx 24rpx rgba(30,64,175,.22); margin-bottom: 20rpx; }
.btn-no { width: 100%; height: 88rpx; background: #fff; color: #64748b; border: 2rpx solid #e2e8f0; border-radius: 18rpx; font-size: 30rpx; }
</style>
