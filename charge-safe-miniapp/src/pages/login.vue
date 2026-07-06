<template>
  <view class="login-page">
    <view class="login-header">
      <text class="title">ChargeSafe</text>
      <text class="subtitle">社区充电安全运维平台</text>
    </view>

    <!-- 登录模式切换 -->
    <view class="login-tabs">
      <text :class="['tab', { active: mode === 'password' }]" @click="mode = 'password'">密码登录</text>
      <text :class="['tab', { active: mode === 'sms' }]" @click="mode = 'sms'">验证码登录</text>
    </view>

    <!-- 表单 -->
    <view class="login-form">
      <input v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号" class="input" />

      <!-- 密码模式 -->
      <view class="input-wrap" v-if="mode === 'password'">
        <input v-model="form.password" :type="showPwd ? 'text' : 'password'" placeholder="请输入密码" class="input input-flex" />
        <text class="eye" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁️' }}</text>
      </view>

      <!-- 验证码模式 -->
      <view class="input-wrap" v-if="mode === 'sms'">
        <input v-model="form.smsCode" type="number" maxlength="6" placeholder="请输入验证码" class="input input-flex" />
        <text :class="['sms-btn', { disabled: countdown > 0 }]" @click="sendSms">{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</text>
      </view>

      <button :loading="loading" class="btn-login" @click="handleLogin">登 录</button>

      <view class="links">
        <text class="link" @click="goRegister">还没有账号？去注册</text>
        <text class="link" @click="goForgot">忘记密码？</text>
      </view>
    </view>

    <!-- 测试账号 -->
    <view class="tips">
      <text class="tip-title">Mock 测试账号：</text>
      <text class="tip">13800000001 / 123456（验证码：123456）</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/auth'
import { switchTab, showToast, isValidPhone } from '@/utils'

const userStore = useUserStore()
const mode = ref('password')
const showPwd = ref(false)
const loading = ref(false)
const countdown = ref(0)
let timer = null

const form = reactive({ phone: '', password: '', smsCode: '' })

async function sendSms() {
  if (countdown.value > 0) return
  if (!isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  try {
    await sendSmsCode({ phone: form.phone, type: 'login' })
    showToast('验证码已发送（Mock: 123456）')
    countdown.value = 60
    timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch { /* handled */ }
}

async function handleLogin() {
  if (!isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  if (mode.value === 'password' && !form.password) return showToast('请输入密码')
  if (mode.value === 'sms' && !form.smsCode) return showToast('请输入验证码')

  loading.value = true
  try {
    let ok = false
    if (mode.value === 'password') {
      ok = await userStore.loginWithPassword({ phone: form.phone, password: form.password })
    } else {
      ok = await userStore.loginWithSms({ phone: form.phone, smsCode: form.smsCode })
    }
    if (ok) {
      showToast('登录成功')
      setTimeout(() => switchTab('/pages/index/index'), 500)
    }
  } catch { /* handled */ }
  finally { loading.value = false }
}

function goRegister() { uni.navigateTo({ url: '/pages/register' }) }
function goForgot() { uni.navigateTo({ url: '/pages/forgot-password' }) }
</script>

<style scoped>
.login-page { padding: 100rpx 60rpx; min-height: 100vh; background: #f5f7fa; }
.login-header { text-align: center; margin-bottom: 60rpx; }
.title { font-size: 48rpx; font-weight: bold; display: block; color: #333; }
.subtitle { font-size: 26rpx; color: #999; margin-top: 12rpx; display: block; }

.login-tabs { display: flex; justify-content: center; gap: 60rpx; margin-bottom: 40rpx; }
.tab { font-size: 30rpx; color: #999; padding-bottom: 12rpx; border-bottom: 4rpx solid transparent; }
.tab.active { color: #409eff; border-bottom-color: #409eff; font-weight: 500; }

.login-form { background: #fff; border-radius: 16rpx; padding: 40rpx 30rpx; }
.input { border: 1px solid #ddd; border-radius: 12rpx; padding: 24rpx; font-size: 28rpx; margin-bottom: 24rpx; background: #fafafa; }
.input-wrap { position: relative; }
.input-flex { padding-right: 180rpx; }
.eye { position: absolute; right: 20rpx; top: 50%; transform: translateY(-70%); font-size: 36rpx; }
.sms-btn { position: absolute; right: 20rpx; top: 50%; transform: translateY(-70%); font-size: 24rpx; color: #409eff; padding: 8rpx 20rpx; background: #ecf5ff; border-radius: 8rpx; }
.sms-btn.disabled { color: #999; background: #f0f0f0; }
.btn-login { background: #409eff; color: #fff; border-radius: 12rpx; margin-top: 16rpx; font-size: 30rpx; }

.links { display: flex; justify-content: space-between; margin-top: 30rpx; }
.link { font-size: 26rpx; color: #409eff; }

.tips { margin-top: 60rpx; text-align: center; }
.tip-title { font-size: 24rpx; color: #999; display: block; }
.tip { font-size: 22rpx; color: #bbb; display: block; margin-top: 6rpx; }
</style>
