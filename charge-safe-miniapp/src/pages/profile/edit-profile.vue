<template>
  <view class="page">
    <!-- 头像 -->
    <view class="avatar-section" @click="changeAvatar">
      <image v-if="userStore.avatar" :src="userStore.avatar" class="avatar-img" mode="aspectFill" />
      <view v-else class="avatar-placeholder"><text class="avatar-text">{{ initial }}</text></view>
      <view class="avatar-mask"><text class="mask-text">更换头像</text></view>
    </view>

    <!-- 表单 -->
    <view class="card">
      <view class="form-item">
        <text class="label">昵称</text>
        <input v-model="form.nickname" maxlength="20" placeholder="请输入昵称" class="input" />
      </view>
      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="form.phone" type="number" maxlength="11" placeholder="如需换绑请输入新手机号" class="input" />
      </view>
      <view class="form-item" v-if="phoneChanged">
        <text class="label">短信验证码</text>
        <view class="input-wrap">
          <input v-model="form.smsCode" type="number" maxlength="6" placeholder="请输入验证码" class="input input-flex" />
          <text :class="['sms-btn', { disabled: countdown > 0 }]" @click="sendSms">{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</text>
        </view>
      </view>
      <button :loading="saving" class="btn-save" @click="handleSave">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/auth'
import { navigateBack, showToast, showLoading, hideLoading, isValidPhone } from '@/utils'

const userStore = useUserStore()
const saving = ref(false)
const countdown = ref(0)
let timer = null

const form = reactive({ nickname: userStore.nickname || '', phone: userStore.phone || '', smsCode: '' })
const originalPhone = userStore.phone
const phoneChanged = computed(() => form.phone !== originalPhone)
const initial = computed(() => (userStore.nickname || '?').charAt(0).toUpperCase())

function changeAvatar() {
  uni.chooseImage({
    count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'],
    success: async (res) => {
      showLoading('上传中...')
      try { const url = await userStore.uploadAvatar(res.tempFilePaths[0]); if (url) showToast('头像更新成功') }
      catch { showToast('上传失败') }
      finally { hideLoading() }
    }
  })
}

async function sendSms() {
  if (countdown.value > 0) return
  if (!isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  try {
    await sendSmsCode({ phone: form.phone, type: 'bind' })
    showToast('验证码已发送（Mock: 123456）')
    countdown.value = 60
    timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch { /* handled */ }
}

async function handleSave() {
  if (!form.nickname.trim()) return showToast('昵称不能为空')
  if (phoneChanged.value && !isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  if (phoneChanged.value && !form.smsCode) return showToast('修改手机号需要短信验证')

  saving.value = true; showLoading('保存中...')
  try {
    const params = { nickname: form.nickname.trim() }
    if (phoneChanged.value) { params.phone = form.phone; params.smsCode = form.smsCode }
    const ok = await userStore.updateProfile(params)
    if (ok) { showToast('保存成功'); setTimeout(navigateBack, 600) }
  } catch { /* handled */ }
  finally { saving.value = false; hideLoading() }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; }
.avatar-section { display: flex; justify-content: center; padding: 60rpx 0 40rpx; }
.avatar-section > view, .avatar-section > image { width: 140rpx; height: 140rpx; border-radius: 50%; overflow: hidden; position: relative; }
.avatar-img { width: 140rpx; height: 140rpx; border-radius: 50%; }
.avatar-placeholder { width: 140rpx; height: 140rpx; border-radius: 50%; background: #a0cfff; display: flex; align-items: center; justify-content: center; }
.avatar-text { font-size: 60rpx; font-weight: bold; color: #fff; }
.avatar-mask { position: absolute; bottom: 0; left: 0; right: 0; height: 44rpx; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; }
.mask-text { font-size: 22rpx; color: #fff; }

.card { margin: 0 20rpx; background: #fff; border-radius: 16rpx; padding: 20rpx 30rpx 40rpx; }
.form-item { padding: 26rpx 0; border-bottom: 2rpx solid #f5f7fa; }
.form-item:last-of-type { border-bottom: none; }
.label { font-size: 28rpx; color: #333; margin-bottom: 12rpx; display: block; }
.input { border: 1px solid #ddd; border-radius: 12rpx; padding: 24rpx; font-size: 28rpx; background: #fafafa; }
.input-wrap { position: relative; }
.input-flex { padding-right: 200rpx; }
.sms-btn { position: absolute; right: 20rpx; top: 50%; transform: translateY(-70%); font-size: 24rpx; color: #409eff; padding: 8rpx 20rpx; background: #ecf5ff; border-radius: 8rpx; }
.sms-btn.disabled { color: #999; background: #f0f0f0; }
.btn-save { background: #409eff; color: #fff; border-radius: 12rpx; font-size: 30rpx; margin-top: 40rpx; }
</style>
