<template>
  <view class="page">
    <view class="card" v-if="step === 1">
      <text class="card-title">重置密码</text>
      <text class="card-desc">请输入注册时使用的手机号</text>

      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="form.phone" type="number" maxlength="11" placeholder="请输入注册手机号" class="input" />
      </view>
      <view class="form-item">
        <text class="label">短信验证码</text>
        <view class="input-wrap">
          <input v-model="form.smsCode" type="number" maxlength="6" placeholder="请输入验证码" class="input input-flex" />
          <text :class="['sms-btn', { disabled: countdown > 0 }]" @click="sendSms">{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</text>
        </view>
      </view>

      <button :loading="loading" class="btn-submit" @click="handleVerify">下一步</button>
    </view>

    <view class="card" v-if="step === 2">
      <text class="card-title">设置新密码</text>

      <view class="form-item">
        <text class="label">新密码</text>
        <view class="input-wrap">
          <input v-model="form.newPassword" :type="showPwd ? 'text' : 'password'" placeholder="6-20位新密码" class="input input-flex" />
          <text class="eye" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁️' }}</text>
        </view>
      </view>
      <view class="form-item">
        <text class="label">确认密码</text>
        <view class="input-wrap">
          <input v-model="form.confirmPassword" :type="showCfm ? 'text' : 'password'" placeholder="请再次输入" class="input input-flex" />
          <text class="eye" @click="showCfm = !showCfm">{{ showCfm ? '🙈' : '👁️' }}</text>
        </view>
      </view>

      <button :loading="resetting" class="btn-submit" @click="handleReset">重置密码</button>
      <view class="bottom-link"><text class="link" @click="step = 1">返回上一步</text></view>
    </view>

    <view class="card success-card" v-if="step === 3">
      <text class="success-icon">✅</text>
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

const step = ref(1)
const showPwd = ref(false)
const showCfm = ref(false)
const loading = ref(false)
const resetting = ref(false)
const countdown = ref(0)
let timer = null

const form = reactive({ phone: '', smsCode: '', newPassword: '', confirmPassword: '' })

async function sendSms() {
  if (countdown.value > 0) return
  if (!isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  try {
    await sendSmsCode({ phone: form.phone, type: 'reset' })
    showToast('验证码已发送（Mock: 123456）')
    countdown.value = 60
    timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch { /* handled */ }
}

async function handleVerify() {
  if (!isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  if (!form.smsCode) return showToast('请输入验证码')
  loading.value = true
  setTimeout(() => { loading.value = false; step.value = 2 }, 500)
}

async function handleReset() {
  if (!isValidPassword(form.newPassword)) return showToast('新密码至少6位')
  if (form.newPassword !== form.confirmPassword) return showToast('两次密码输入不一致')
  resetting.value = true
  try {
    const res = await resetPassword({ phone: form.phone, smsCode: form.smsCode, newPassword: form.newPassword, confirmPassword: form.confirmPassword })
    if (res.code === 200) { resetting.value = false; step.value = 3 }
  } catch { resetting.value = false }
}

function goLogin() { uni.navigateBack() }
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; padding: 40rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 40rpx 30rpx; }
.card-title { font-size: 40rpx; font-weight: bold; display: block; margin-bottom: 12rpx; color: #333; }
.card-desc { font-size: 26rpx; color: #999; display: block; margin-bottom: 36rpx; }
.form-item { margin-bottom: 30rpx; }
.label { font-size: 28rpx; color: #333; margin-bottom: 12rpx; display: block; }
.input { border: 1px solid #ddd; border-radius: 12rpx; padding: 24rpx; font-size: 28rpx; background: #fafafa; }
.input-wrap { position: relative; }
.input-flex { padding-right: 200rpx; }
.eye { position: absolute; right: 20rpx; top: 50%; transform: translateY(-70%); font-size: 36rpx; }
.sms-btn { position: absolute; right: 20rpx; top: 50%; transform: translateY(-70%); font-size: 24rpx; color: #409eff; padding: 8rpx 20rpx; background: #ecf5ff; border-radius: 8rpx; }
.sms-btn.disabled { color: #999; background: #f0f0f0; }
.btn-submit { background: #409eff; color: #fff; border-radius: 12rpx; font-size: 30rpx; margin-top: 16rpx; }
.bottom-link { text-align: center; margin-top: 30rpx; }
.link { font-size: 26rpx; color: #999; }

.success-card { text-align: center; padding: 80rpx 40rpx; }
.success-icon { font-size: 100rpx; display: block; margin-bottom: 30rpx; }
.success-title { font-size: 36rpx; font-weight: bold; display: block; margin-bottom: 12rpx; color: #333; }
.success-desc { font-size: 28rpx; color: #999; display: block; margin-bottom: 50rpx; }
</style>
