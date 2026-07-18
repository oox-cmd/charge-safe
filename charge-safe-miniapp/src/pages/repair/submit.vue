<template>
  <view class="page" v-if="!submitted">
    <view class="card">
      <text class="card-title">提交报修</text>
      <view class="field">
        <text class="lbl">设备/端口</text>
        <text class="readonly">{{ deviceLabel || '请从车棚端口详情进入报修' }}</text>
      </view>
      <view class="field">
        <text class="lbl">故障类型</text>
        <view class="tag-row">
          <text v-for="type in faultTypes" :key="type" :class="['tag', { on: form.faultType === type }]" @click="form.faultType = type">{{ type }}</text>
        </view>
      </view>
      <view class="field">
        <text class="lbl">问题描述（至少 10 个字）</text>
        <textarea v-model="form.faultDesc" placeholder="请详细描述故障情况" placeholder-style="color:#c0c4cc" class="textarea" :maxlength="500" />
        <text class="count">{{ form.faultDesc.length }}/500</text>
      </view>
      <text class="notice">现场图片上传尚未开放，因此本次报修不会伪造图片附件。</text>
    </view>
    <button :loading="loading" :disabled="!valid" class="btn-submit" @click="handleSubmit">提交报修</button>
  </view>
  <view class="success-page" v-else>
    <view class="success-card">
      <text class="check">✓</text>
      <text class="stitle">报修已提交</text>
      <text class="sno">报修单 #{{ reportId }}</text>
      <text class="stip">物业人员会在后台受理；进度会显示在“我的报修”和消息通知中。</text>
      <button class="btn-back" @click="goList">查看报修记录</button>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createRepairReport } from '@/api/repair'

const faultTypes = ['无法充电', '端口损坏', '线路故障', '设备离线', '其他']
const loading = ref(false)
const submitted = ref(false)
const reportId = ref('')
const deviceId = ref(null)
const portId = ref(null)
const deviceLabel = ref('')
const form = reactive({ faultType: '', faultDesc: '' })
const valid = computed(() => deviceId.value && form.faultType && form.faultDesc.trim().length >= 10)

onLoad((options) => {
  const parsedDeviceId = Number(options.deviceId)
  const parsedPortId = Number(options.portId)
  deviceId.value = Number.isFinite(parsedDeviceId) && parsedDeviceId > 0 ? parsedDeviceId : null
  portId.value = Number.isFinite(parsedPortId) && parsedPortId > 0 ? parsedPortId : null
  deviceLabel.value = options.portName || options.deviceName || ''
})

async function handleSubmit() {
  if (!valid.value) return
  loading.value = true
  try {
    const res = await createRepairReport({
      deviceId: deviceId.value,
      portId: portId.value,
      faultType: form.faultType,
      faultDesc: form.faultDesc.trim()
    })
    if (res.code === 200 && res.data?.id) {
      reportId.value = res.data.id
      submitted.value = true
    }
  } catch {
    // request.js 已统一展示错误提示
  } finally {
    loading.value = false
  }
}

function goList() { uni.redirectTo({ url: '/pages/repair/list' }) }
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 30rpx 28rpx; }
.card { background: #fff; border-radius: 22rpx; padding: 36rpx 28rpx; margin-bottom: 36rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.card-title { font-size: 34rpx; font-weight: 800; color: #1e3a8a; display: block; margin-bottom: 32rpx; }
.field { margin-bottom: 28rpx; }
.lbl { font-size: 28rpx; color: #475569; margin-bottom: 12rpx; display: block; font-weight: 500; }
.readonly { display: block; border: 2rpx solid #e8ecf0; border-radius: 14rpx; padding: 22rpx; font-size: 28rpx; background: #f8fafc; color: #64748b; }
.textarea { border: 2rpx solid #e8ecf0; border-radius: 14rpx; padding: 22rpx; font-size: 28rpx; height: 180rpx; background: #f8fafc; color: #1e293b; width: 100%; box-sizing: border-box; }
.count { font-size: 22rpx; color: #94a3b8; text-align: right; display: block; margin-top: 8rpx; }
.tag-row { display: flex; flex-wrap: wrap; gap: 16rpx; }
.tag { padding: 14rpx 28rpx; font-size: 26rpx; color: #64748b; background: #f8fafc; border: 2rpx solid #e8ecf0; border-radius: 14rpx; }
.tag.on { color: #2563eb; background: #eff6ff; border-color: #2563eb; font-weight: 600; }
.notice { display: block; font-size: 24rpx; color: #64748b; background: #f8fafc; padding: 18rpx; border-radius: 12rpx; line-height: 1.5; }
.btn-submit, .btn-back { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af, #1d4ed8); color: #fff; border: none; border-radius: 18rpx; font-size: 32rpx; font-weight: 600; }
.btn-submit[disabled] { opacity: .45; }
.success-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 40rpx; background: #f0f4f8; }
.success-card { background: #fff; border-radius: 28rpx; padding: 60rpx 40rpx; text-align: center; width: 100%; max-width: 600rpx; box-shadow: 0 8rpx 40rpx rgba(30,58,138,.06); }
.check { width: 100rpx; height: 100rpx; line-height: 100rpx; border-radius: 50%; background: #dcfce7; color: #15803d; font-size: 60rpx; display: block; margin: 0 auto 20rpx; }
.stitle { font-size: 36rpx; font-weight: 800; color: #1e3a8a; display: block; }
.sno { font-size: 30rpx; font-weight: 600; color: #1e293b; margin-top: 20rpx; display: block; padding: 20rpx; background: #f8fafc; border-radius: 14rpx; }
.stip { font-size: 26rpx; color: #64748b; margin-top: 16rpx; display: block; line-height: 1.6; }
.btn-back { margin-top: 40rpx; font-size: 30rpx; }
</style>
