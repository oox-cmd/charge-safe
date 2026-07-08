<template>
  <view class="page">
    <view class="bg-pattern"></view>

    <!-- 步骤1：验证身份 -->
    <view class="card" v-if="step === 1">
      <view class="card-header">
        <view class="brand">
          <view class="logo-icon">🔐</view>
          <view class="brand-text">
            <text class="brand-name">重置密码</text>
            <text class="brand-desc">请输入注册时使用的手机号</text>
          </view>
        </view>
      </view>
      <view class="form-body">
        <view class="field">
          <view class="field-icon">📱</view>
          <input v-model="form.phone" type="number" maxlength="11" placeholder="请输入注册手机号" placeholder-style="color:#c0c4cc" class="field-input" />
        </view>
        <view class="field">
          <view class="field-icon">💬</view>
          <input v-model="form.smsCode" type="number" maxlength="6" placeholder="请输入验证码" placeholder-style="color:#c0c4cc" class="field-input field-flex" />
          <view :class="['sms-tag', { off: countdown > 0 }]" @click="sendSms">{{ countdown > 0 ? countdown + 's 后重发' : '获取验证码' }}</view>
        </view>
        <button :loading="loading" class="btn-submit" @click="handleVerify">下一步</button>
      </view>
    </view>

    <!-- 步骤2：设置新密码 -->
    <view class="card" v-if="step === 2">
      <view class="card-header">
        <view class="brand">
          <view class="logo-icon">🔑</view>
          <view class="brand-text">
            <text class="brand-name">设置新密码</text>
            <text class="brand-desc">请设置6-20位新密码</text>
          </view>
        </view>
      </view>
      <view class="form-body">
        <view class="field">
          <view class="field-icon">🔒</view>
          <input v-model="form.newPassword" :type="s1 ? 'text' : 'password'" placeholder="新密码" placeholder-style="color:#c0c4cc" class="field-input field-flex" />
          <view class="field-suffix" @click="s1 = !s1"><text>{{ s1 ? '🙈' : '👁' }}</text></view>
        </view>
        <view class="field">
          <view class="field-icon">🔒</view>
          <input v-model="form.confirmPassword" :type="s2 ? 'text' : 'password'" placeholder="确认新密码" placeholder-style="color:#c0c4cc" class="field-input field-flex" />
          <view class="field-suffix" @click="s2 = !s2"><text>{{ s2 ? '🙈' : '👁' }}</text></view>
        </view>
        <button :loading="resetting" class="btn-submit" @click="handleReset">重置密码</button>
        <view class="card-footer"><text class="footer-link" @click="step = 1">返回上一步</text></view>
      </view>
    </view>

    <!-- 步骤3：完成 -->
    <view class="card success-card" v-if="step === 3">
      <view class="success-icon">✅</view>
      <text class="success-title">密码重置成功</text>
      <text class="success-desc">请使用新密码登录</text>
      <button class="btn-submit" @click="goLogin">返回登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { sendSmsCode, resetPassword } from '@/api/auth'
import { showToast, isValidPhone, isValidPassword } from '@/utils'

const step = ref(1); const s1 = ref(false); const s2 = ref(false)
const loading = ref(false); const resetting = ref(false); const countdown = ref(0)
let timer = null
const form = reactive({ phone: '', smsCode: '', newPassword: '', confirmPassword: '' })

async function sendSms() {
  if (countdown.value > 0) return
  if (!isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  try { await sendSmsCode({ phone: form.phone, type: 'reset' }); showToast('验证码已发送（Mock: 123456）'); countdown.value = 60; timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000) } catch { /* handled */ }
}
async function handleVerify() {
  if (!isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  if (!form.smsCode) return showToast('请输入验证码')
  loading.value = true; setTimeout(() => { loading.value = false; step.value = 2 }, 500)
}
async function handleReset() {
  if (!isValidPassword(form.newPassword)) return showToast('新密码至少6位')
  if (form.newPassword !== form.confirmPassword) return showToast('两次密码输入不一致')
  resetting.value = true
  try { const res = await resetPassword({ phone: form.phone, smsCode: form.smsCode, newPassword: form.newPassword, confirmPassword: form.confirmPassword }); if (res.code === 200) { resetting.value = false; step.value = 3 } } catch { resetting.value = false }
}
function goLogin() { uni.navigateBack() }
</script>

<style scoped>
.page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 60rpx 50rpx; background: radial-gradient(ellipse at 20% 0%, rgba(59,130,246,.1) 0%, transparent 60%), radial-gradient(ellipse at 80% 100%, rgba(99,102,241,.08) 0%, transparent 55%), linear-gradient(175deg, #eef6ff 0%, #e0edfb 20%, #dce8f8 45%, #e8f0fc 70%, #f0f6ff 100%); position: relative; overflow: hidden; }
.bg-pattern { position: absolute; inset: 0; opacity: .04; background-image: repeating-linear-gradient(90deg, transparent, transparent 96rpx, #1e3a8a 96rpx, #1e3a8a 97rpx), repeating-linear-gradient(0deg, transparent, transparent 96rpx, #1e3a8a 96rpx, #1e3a8a 97rpx); background-size: 96rpx 96rpx, 96rpx 96rpx; pointer-events: none; }

.card { width: 100%; max-width: 640rpx; background: #fff; border-radius: 32rpx; box-shadow: 0 8rpx 50rpx rgba(30,58,138,.08), 0 2rpx 16rpx rgba(0,0,0,.04); padding: 50rpx 44rpx 40rpx; position: relative; z-index: 1; }
.card-header { margin-bottom: 36rpx; }
.brand { display: flex; align-items: center; }
.logo-icon { width: 80rpx; height: 80rpx; background: linear-gradient(135deg, #e8f0fe, #d0e0fc); border-radius: 20rpx; display: flex; align-items: center; justify-content: center; font-size: 44rpx; margin-right: 22rpx; }
.brand-name { font-size: 38rpx; font-weight: 700; color: #1e3a8a; display: block; }
.brand-desc { font-size: 24rpx; color: #94a3b8; margin-top: 4rpx; display: block; }

.form-body { display: flex; flex-direction: column; }
.field { display: flex; align-items: center; height: 88rpx; background: #f8fafc; border: 2rpx solid #e8ecf0; border-radius: 16rpx; padding: 0 20rpx; margin-bottom: 18rpx; transition: border-color .2s, box-shadow .2s; }
.field:focus-within { border-color: #2563eb; background: #fff; box-shadow: 0 0 0 6rpx rgba(37,99,235,.05); }
.field-icon { font-size: 32rpx; width: 48rpx; text-align: center; flex-shrink: 0; }
.field-input { flex: 1; height: 100%; font-size: 28rpx; color: #1e293b; padding: 0 8rpx; }
.field-flex { padding-right: 20rpx; }
.field-suffix { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; font-size: 30rpx; border-radius: 10rpx; flex-shrink: 0; color: #94a3b8; }
.sms-tag { flex-shrink: 0; padding: 10rpx 22rpx; font-size: 24rpx; color: #2563eb; background: #eff6ff; border-radius: 12rpx; font-weight: 500; white-space: nowrap; }
.sms-tag.off { color: #94a3b8; background: #f1f5f9; }

.btn-submit { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af, #1d4ed8); color: #fff; border: none; border-radius: 18rpx; font-size: 32rpx; font-weight: 600; letter-spacing: 8rpx; box-shadow: 0 8rpx 24rpx rgba(30,64,175,.22); margin-top: 4rpx; }
.card-footer { text-align: center; margin-top: 28rpx; }
.footer-link { font-size: 26rpx; color: #94a3b8; }

.success-card { text-align: center; padding: 80rpx 50rpx !important; }
.success-icon { font-size: 100rpx; display: block; margin-bottom: 24rpx; }
.success-title { font-size: 36rpx; font-weight: 700; color: #1e3a8a; display: block; margin-bottom: 10rpx; }
.success-desc { font-size: 28rpx; color: #94a3b8; display: block; margin-bottom: 48rpx; }
</style>
