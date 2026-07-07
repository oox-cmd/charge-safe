<template>
  <view class="page">
    <view class="card">
      <text class="card-title">提交报修</text>
      <view class="field"><text class="lbl">设备/端口</text><input v-model="form.deviceName" placeholder="请输入设备或端口编号" placeholder-style="color:#c0c4cc" class="input" /></view>
      <view class="field"><text class="lbl">故障类型</text>
        <picker :range="faultTypes" @change="onType"><view class="picker">{{ form.faultType || '请选择故障类型' }}</view></picker>
      </view>
      <view class="field"><text class="lbl">故障描述</text><textarea v-model="form.description" placeholder="请详细描述故障情况" placeholder-style="color:#c0c4cc" class="textarea" /></view>
    </view>
    <button :loading="loading" class="btn-submit" @click="handleSubmit">提交报修</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
const faultTypes = ['无法充电', '端口损坏', '线路故障', '设备离线', '其他']
const loading = ref(false)
const form = ref({ deviceName: '', faultType: '', description: '' })
const onType = (e) => { form.value.faultType = faultTypes[e.detail.value] }
const handleSubmit = () => {
  if (!form.value.deviceName) return uni.showToast({ title: '请输入设备/端口', icon: 'none' })
  if (!form.value.faultType) return uni.showToast({ title: '请选择故障类型', icon: 'none' })
  if (!form.value.description) return uni.showToast({ title: '请输入故障描述', icon: 'none' })
  loading.value = true; setTimeout(() => { loading.value = false; uni.showToast({ title: '提交成功', icon: 'success' }); setTimeout(() => uni.navigateBack(), 1500) }, 1000)
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 40rpx 28rpx; }
.card { background: #fff; border-radius: 22rpx; padding: 36rpx 28rpx; margin-bottom: 40rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.card-title { font-size: 34rpx; font-weight: 800; color: #1e3a8a; display: block; margin-bottom: 32rpx; }
.field { margin-bottom: 28rpx; }
.lbl { font-size: 28rpx; color: #475569; margin-bottom: 12rpx; display: block; font-weight: 500; }
.input { border: 2rpx solid #e8ecf0; border-radius: 14rpx; padding: 22rpx; font-size: 28rpx; background: #f8fafc; color: #1e293b; }
.picker { border: 2rpx solid #e8ecf0; border-radius: 14rpx; padding: 22rpx; font-size: 28rpx; background: #f8fafc; color: #1e293b; }
.textarea { border: 2rpx solid #e8ecf0; border-radius: 14rpx; padding: 22rpx; font-size: 28rpx; height: 200rpx; background: #f8fafc; color: #1e293b; }
.btn-submit { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af, #1d4ed8); color: #fff; border: none; border-radius: 18rpx; font-size: 32rpx; font-weight: 600; box-shadow: 0 8rpx 24rpx rgba(30,64,175,.22); }
</style>
