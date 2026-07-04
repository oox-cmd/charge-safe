<template>
  <view class="repair-submit-page">
    <view class="form-card">
      <text class="card-title">提交报修</text>

      <view class="form-item">
        <text class="label">设备/端口</text>
        <input v-model="form.deviceName" placeholder="请输入设备或端口编号" class="input" />
      </view>
      <view class="form-item">
        <text class="label">故障类型</text>
        <picker :range="faultTypes" @change="onFaultTypeChange">
          <view class="picker">{{ form.faultType || '请选择故障类型' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">故障描述</text>
        <textarea v-model="form.description" placeholder="请详细描述故障情况" class="textarea" />
      </view>
    </view>

    <view class="action-area">
      <button class="btn-submit" :loading="loading" @click="handleSubmit">提交报修</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const faultTypes = ['无法充电', '端口损坏', '线路故障', '设备离线', '其他']
const loading = ref(false)

const form = ref({
  deviceName: '',
  faultType: '',
  description: ''
})

const onFaultTypeChange = (e) => {
  form.value.faultType = faultTypes[e.detail.value]
}

const handleSubmit = () => {
  if (!form.value.deviceName) {
    uni.showToast({ title: '请输入设备/端口', icon: 'none' })
    return
  }
  if (!form.value.faultType) {
    uni.showToast({ title: '请选择故障类型', icon: 'none' })
    return
  }
  if (!form.value.description) {
    uni.showToast({ title: '请输入故障描述', icon: 'none' })
    return
  }

  loading.value = true
  setTimeout(() => {
    loading.value = false
    uni.showToast({ title: '提交成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  }, 1000)
}
</script>

<style scoped>
.repair-submit-page {
  padding: 40rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.form-card {
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
.form-item {
  margin-bottom: 30rpx;
}
.label {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 12rpx;
}
.input {
  border: 1px solid #ddd;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
}
.textarea {
  border: 1px solid #ddd;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
  height: 200rpx;
}
.picker {
  border: 1px solid #ddd;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
  color: #666;
}
.action-area {
  padding: 0 20rpx;
}
.btn-submit {
  background: #409eff;
  color: #fff;
  border-radius: 12rpx;
}
</style>
