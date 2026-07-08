<template>
  <view class="page">
    <view class="bg-pattern"></view>
    <view class="card">
      <view class="card-header">
        <view class="brand">
          <view class="logo-icon">✏️</view>
          <view class="brand-text">
            <text class="brand-name">编辑资料</text>
            <text class="brand-desc">修改您的个人信息</text>
          </view>
        </view>
      </view>

      <!-- 头像 -->
      <view class="avatar-row" @click="changeAvatar">
        <image v-if="userStore.avatar" :src="userStore.avatar" class="avatar-img" mode="aspectFill" />
        <view v-else class="avatar-place"><text class="avatar-initial">{{ initial }}</text></view>
        <view class="avatar-badge">📷</view>
      </view>

      <view class="form-body">
        <view class="field">
          <view class="field-icon">👤</view>
          <input v-model="form.nickname" maxlength="20" placeholder="请输入昵称" placeholder-style="color:#c0c4cc" class="field-input" />
        </view>
        <view class="field">
          <view class="field-icon">📱</view>
          <input v-model="form.phone" type="number" maxlength="11" placeholder="如需换绑请输入新手机号" placeholder-style="color:#c0c4cc" class="field-input" />
        </view>
        <view class="field" v-if="phoneChanged">
          <view class="field-icon">💬</view>
          <input v-model="form.smsCode" type="number" maxlength="6" placeholder="请输入验证码" placeholder-style="color:#c0c4cc" class="field-input field-flex" />
          <view :class="['sms-tag', { off: countdown > 0 }]" @click="sendSms">{{ countdown > 0 ? countdown + 's 后重发' : '获取验证码' }}</view>
        </view>
        <button :loading="saving" class="btn-submit" @click="handleSave">保 存</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/auth'
import { navigateBack, showToast, showLoading, hideLoading, isValidPhone } from '@/utils'

const userStore = useUserStore()
const saving = ref(false); const countdown = ref(0); let timer = null
const form = reactive({ nickname: userStore.nickname || '', phone: userStore.phone || '', smsCode: '' })
const originalPhone = userStore.phone
const phoneChanged = computed(() => form.phone !== originalPhone)
const initial = computed(() => (userStore.nickname || '?').charAt(0).toUpperCase())

function changeAvatar() {
  uni.chooseImage({ count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'],
    success: async (res) => { showLoading('上传中...'); try { const url = await userStore.uploadAvatar(res.tempFilePaths[0]); if (url) showToast('头像更新成功') } catch { showToast('上传失败') } finally { hideLoading() } }
  })
}
async function sendSms() {
  if (countdown.value > 0) return
  if (!isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  try { await sendSmsCode({ phone: form.phone, type: 'bind' }); showToast('验证码已发送（Mock: 123456）'); countdown.value = 60; timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000) } catch { /* handled */ }
}
async function handleSave() {
  if (!form.nickname.trim()) return showToast('昵称不能为空')
  if (phoneChanged.value && !isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  if (phoneChanged.value && !form.smsCode) return showToast('修改手机号需要短信验证')
  saving.value = true; showLoading('保存中...')
  try { const params = { nickname: form.nickname.trim() }; if (phoneChanged.value) { params.phone = form.phone; params.smsCode = form.smsCode }; const ok = await userStore.updateProfile(params); if (ok) { showToast('保存成功'); setTimeout(navigateBack, 600) } } catch { /* handled */ }
  finally { saving.value = false; hideLoading() }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 40rpx 50rpx; background: radial-gradient(ellipse at 20% 0%, rgba(59,130,246,.1) 0%, transparent 60%), radial-gradient(ellipse at 80% 100%, rgba(99,102,241,.08) 0%, transparent 55%), linear-gradient(175deg, #eef6ff 0%, #e0edfb 20%, #dce8f8 45%, #e8f0fc 70%, #f0f6ff 100%); position: relative; overflow: hidden; }
.bg-pattern { position: absolute; inset: 0; opacity: .04; background-image: repeating-linear-gradient(90deg, transparent, transparent 96rpx, #1e3a8a 96rpx, #1e3a8a 97rpx), repeating-linear-gradient(0deg, transparent, transparent 96rpx, #1e3a8a 96rpx, #1e3a8a 97rpx); background-size: 96rpx 96rpx, 96rpx 96rpx; pointer-events: none; }
.card { width: 100%; max-width: 640rpx; background: #fff; border-radius: 32rpx; box-shadow: 0 8rpx 50rpx rgba(30,58,138,.08), 0 2rpx 16rpx rgba(0,0,0,.04); padding: 50rpx 44rpx 40rpx; margin: 0 auto; position: relative; z-index: 1; }
.card-header { margin-bottom: 30rpx; }
.brand { display: flex; align-items: center; }
.logo-icon { width: 70rpx; height: 70rpx; background: linear-gradient(135deg, #e8f0fe, #d0e0fc); border-radius: 18rpx; display: flex; align-items: center; justify-content: center; font-size: 38rpx; margin-right: 20rpx; }
.brand-name { font-size: 34rpx; font-weight: 700; color: #1e3a8a; display: block; }
.brand-desc { font-size: 24rpx; color: #94a3b8; margin-top: 4rpx; display: block; }

.avatar-row { display: flex; justify-content: center; margin-bottom: 30rpx; position: relative; width: 120rpx; height: 120rpx; margin-left: auto; margin-right: auto; }
.avatar-img { width: 120rpx; height: 120rpx; border-radius: 50%; border: 4rpx solid #e8f0fe; }
.avatar-place { width: 120rpx; height: 120rpx; border-radius: 50%; background: linear-gradient(135deg, #2563eb, #1e40af); display: flex; align-items: center; justify-content: center; border: 4rpx solid #e8f0fe; }
.avatar-initial { font-size: 48rpx; font-weight: 700; color: #fff; }
.avatar-badge { position: absolute; bottom: 0; right: 0; width: 40rpx; height: 40rpx; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 22rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,.1); }

.form-body { display: flex; flex-direction: column; }
.field { display: flex; align-items: center; height: 88rpx; background: #f8fafc; border: 2rpx solid #e8ecf0; border-radius: 16rpx; padding: 0 20rpx; margin-bottom: 18rpx; transition: border-color .2s, box-shadow .2s; }
.field:focus-within { border-color: #2563eb; background: #fff; box-shadow: 0 0 0 6rpx rgba(37,99,235,.05); }
.field-icon { font-size: 32rpx; width: 48rpx; text-align: center; flex-shrink: 0; }
.field-input { flex: 1; height: 100%; font-size: 28rpx; color: #1e293b; padding: 0 8rpx; }
.field-flex { padding-right: 20rpx; }
.sms-tag { flex-shrink: 0; padding: 10rpx 22rpx; font-size: 24rpx; color: #2563eb; background: #eff6ff; border-radius: 12rpx; font-weight: 500; white-space: nowrap; }
.sms-tag.off { color: #94a3b8; background: #f1f5f9; }
.btn-submit { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af, #1d4ed8); color: #fff; border: none; border-radius: 18rpx; font-size: 32rpx; font-weight: 600; letter-spacing: 8rpx; box-shadow: 0 8rpx 24rpx rgba(30,64,175,.22); margin-top: 4rpx; }
</style>
