<template>
  <view class="page" v-if="!submitted">
    <view class="card">
      <text class="card-title">提交报修</text>

      <!-- 设备 -->
      <view class="field"><text class="lbl">设备/端口</text><input v-model="form.deviceName" placeholder="请输入设备或端口编号" placeholder-style="color:#c0c4cc" class="input" /></view>

      <!-- 故障类型 -->
      <view class="field"><text class="lbl">故障类型</text>
        <view class="tag-row">
          <text v-for="t in faultTypes" :key="t" :class="['tag', { on: form.faultType === t }]" @click="form.faultType = t">{{ t }}</text>
        </view>
      </view>

      <!-- 描述 -->
      <view class="field"><text class="lbl">问题描述（至少10个字）</text>
        <textarea v-model="form.description" placeholder="请详细描述故障情况" placeholder-style="color:#c0c4cc" class="textarea" :maxlength="500" />
        <text class="count">{{ form.description.length }}/500</text>
      </view>

      <!-- 上传图片 -->
      <view class="field"><text class="lbl">现场照片（最多3张）</text>
        <view class="photo-grid">
          <view class="photo-item" v-for="(img, i) in form.images" :key="i">
            <image :src="img" class="photo-thumb" mode="aspectFill" />
            <view class="photo-del" @click="form.images.splice(i, 1)">✕</view>
          </view>
          <view class="photo-add" v-if="form.images.length < 3" @click="addPhoto">
            <text class="add-icon">+</text>
            <text class="add-text">{{ form.images.length === 0 ? '添加照片' : form.images.length + '/3' }}</text>
          </view>
        </view>
      </view>
    </view>

    <button :loading="loading" :disabled="!valid" class="btn-submit" @click="handleSubmit">提交报修</button>
  </view>

  <!-- 成功页 -->
  <view class="success-page" v-else>
    <view class="success-card">
      <text class="check">✅</text>
      <text class="stitle">提交成功</text>
      <text class="sno">工单号：{{ ticketNo }}</text>
      <text class="stip">维修人员将尽快处理，请耐心等待</text>
      <button class="btn-back" @click="goList">查看报修记录</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
const faultTypes = ['无法充电', '端口损坏', '线路故障', '设备离线', '其他']
const loading = ref(false); const submitted = ref(false); const ticketNo = ref('')
const form = reactive({ deviceName: '', faultType: '', description: '', images: [] })
const valid = computed(() => form.deviceName && form.faultType && form.description.length >= 10)

function addPhoto() {
  uni.chooseImage({
    count: 3 - form.images.length, sizeType: ['compressed'], sourceType: ['album', 'camera'],
    success: (res) => { form.images.push(...res.tempFilePaths) }
  })
}
function handleSubmit() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    const ts = new Date().toISOString().slice(0, 10).replace(/-/g, '')
    ticketNo.value = 'TK' + ts + String(Math.floor(Math.random() * 1000)).padStart(3, '0')
    submitted.value = true
  }, 1000)
}
function goList() { uni.redirectTo({ url: '/pages/repair/list' }) }
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 30rpx 28rpx; }
.card { background: #fff; border-radius: 22rpx; padding: 36rpx 28rpx; margin-bottom: 36rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.card-title { font-size: 34rpx; font-weight: 800; color: #1e3a8a; display: block; margin-bottom: 32rpx; }
.field { margin-bottom: 28rpx; }
.lbl { font-size: 28rpx; color: #475569; margin-bottom: 12rpx; display: block; font-weight: 500; }
.input { border: 2rpx solid #e8ecf0; border-radius: 14rpx; padding: 22rpx; font-size: 28rpx; background: #f8fafc; color: #1e293b; }
.textarea { border: 2rpx solid #e8ecf0; border-radius: 14rpx; padding: 22rpx; font-size: 28rpx; height: 180rpx; background: #f8fafc; color: #1e293b; width: 100%; box-sizing: border-box; }
.count { font-size: 22rpx; color: #94a3b8; text-align: right; display: block; margin-top: 8rpx; }

.tag-row { display: flex; flex-wrap: wrap; gap: 16rpx; }
.tag { padding: 14rpx 28rpx; font-size: 26rpx; color: #64748b; background: #f8fafc; border: 2rpx solid #e8ecf0; border-radius: 14rpx; }
.tag.on { color: #2563eb; background: #eff6ff; border-color: #2563eb; font-weight: 600; }

.photo-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.photo-item { position: relative; width: 160rpx; height: 160rpx; }
.photo-thumb { width: 100%; height: 100%; border-radius: 14rpx; }
.photo-del { position: absolute; top: -10rpx; right: -10rpx; width: 40rpx; height: 40rpx; background: #ef4444; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 22rpx; color: #fff; }
.photo-add { width: 160rpx; height: 160rpx; border: 2rpx dashed #cbd5e1; border-radius: 14rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; background: #f8fafc; }
.add-icon { font-size: 48rpx; color: #94a3b8; }
.add-text { font-size: 22rpx; color: #94a3b8; margin-top: 6rpx; }

.btn-submit { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af, #1d4ed8); color: #fff; border: none; border-radius: 18rpx; font-size: 32rpx; font-weight: 600; box-shadow: 0 8rpx 24rpx rgba(30,64,175,.22); }
.btn-submit[disabled] { opacity: .45; box-shadow: none; }

.success-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 40rpx; background: #f0f4f8; }
.success-card { background: #fff; border-radius: 28rpx; padding: 60rpx 40rpx; text-align: center; width: 100%; max-width: 600rpx; box-shadow: 0 8rpx 40rpx rgba(30,58,138,.06); }
.check { font-size: 100rpx; display: block; margin-bottom: 20rpx; }
.stitle { font-size: 36rpx; font-weight: 800; color: #1e3a8a; display: block; }
.sno { font-size: 30rpx; font-weight: 600; color: #1e293b; margin-top: 20rpx; display: block; padding: 20rpx; background: #f8fafc; border-radius: 14rpx; }
.stip { font-size: 26rpx; color: #94a3b8; margin-top: 16rpx; display: block; }
.btn-back { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af, #1d4ed8); color: #fff; border: none; border-radius: 18rpx; font-size: 30rpx; font-weight: 600; margin-top: 40rpx; }
</style>
