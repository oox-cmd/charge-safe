<template>
  <view class="login-page">
    <!-- 底纹背景层 -->
    <view class="bg-pattern"></view>

    <!-- 登录卡片 -->
    <view class="login-card">
      <!-- 品牌头部 -->
      <view class="card-header">
        <view class="brand">
          <view class="logo-icon">
            <text class="logo-shield">🛡️</text>
            <text class="logo-bolt">⚡</text>
          </view>
          <view class="brand-text">
            <text class="brand-name">ChargeSafe</text>
            <text class="brand-desc">社区充电安全运维平台</text>
          </view>
        </view>
      </view>

      <!-- 双Tab切换 -->
      <view class="tab-bar">
        <view
          :class="['tab', { active: mode === 'password' }]"
          @click="mode = 'password'"
        >
          <text class="tab-text">密码登录</text>
          <view class="tab-line"></view>
        </view>
        <view
          :class="['tab', { active: mode === 'sms' }]"
          @click="mode = 'sms'"
        >
          <text class="tab-text">验证码登录</text>
          <view class="tab-line"></view>
        </view>
      </view>

      <!-- 表单 -->
      <view class="form-body">
        <!-- 手机号 -->
        <view class="field">
          <view class="field-icon">📱</view>
          <input
            class="field-input"
            v-model="form.phone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-style="color:#c0c4cc"
          />
        </view>

        <!-- 密码模式 -->
        <view class="field" v-if="mode === 'password'">
          <view class="field-icon">🔒</view>
          <input
            class="field-input"
            v-model="form.password"
            :type="showPwd ? 'text' : 'password'"
            maxlength="20"
            placeholder="请输入密码"
            placeholder-style="color:#c0c4cc"
          />
          <view class="field-suffix" @click="showPwd = !showPwd">
            <text>{{ showPwd ? '🙈' : '👁' }}</text>
          </view>
        </view>

        <!-- 验证码模式 -->
        <view class="field" v-if="mode === 'sms'">
          <view class="field-icon">💬</view>
          <input
            class="field-input field-flex"
            v-model="form.smsCode"
            type="number"
            maxlength="6"
            placeholder="请输入短信验证码"
            placeholder-style="color:#c0c4cc"
          />
          <view
            :class="['sms-tag', { disabled: countdown > 0 }]"
            @click="sendSms"
          >
            <text>{{ countdown > 0 ? countdown + 's 后重发' : '获取验证码' }}</text>
          </view>
        </view>

        <!-- 登录按钮 -->
        <button :loading="loading" class="btn-login" @click="handleLogin">登 录</button>

        <!-- 底部链接 -->
        <view class="card-footer">
          <text class="footer-link" @click="goRegister">去注册</text>
          <view class="footer-divider"></view>
          <text class="footer-link" @click="goForgot">忘记密码</text>
        </view>
      </view>

      <!-- Mock 账号提示 -->
      <view v-if="USE_MOCK" class="mock-tips">
        <text class="mock-item">开发模拟数据模式（非真实业务数据）</text>
        <text class="mock-item">Mock 测试：13800000001 / 123456</text>
        <text class="mock-item">验证码统一：123456</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/auth'
import { USE_MOCK } from '@/api/request'
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
/* =========== 背景层 =========== */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60rpx 50rpx;
  /* 多层渐变：顶部亮 → 中部天蓝 → 底部蓝紫 */
  background:
    radial-gradient(ellipse at 20% 0%, rgba(59, 130, 246, 0.12) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 100%, rgba(99, 102, 241, 0.10) 0%, transparent 55%),
    radial-gradient(ellipse at 50% 40%, rgba(14, 165, 233, 0.08) 0%, transparent 50%),
    linear-gradient(175deg, #eef6ff 0%, #e0edfb 20%, #dce8f8 45%, #e8f0fc 70%, #f0f6ff 100%);
  position: relative;
  overflow: hidden;
}

/* 细线条底纹 — 网格 + 疏密错落的盾牌点阵 */
.bg-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.05;
  background-image:
    /* 竖线 */ repeating-linear-gradient(90deg, transparent, transparent 96rpx, #1e3a8a 96rpx, #1e3a8a 97rpx),
    /* 横线 */ repeating-linear-gradient(0deg, transparent, transparent 96rpx, #1e3a8a 96rpx, #1e3a8a 97rpx),
    /* 盾牌点 — 十字分布 */ radial-gradient(circle at 48rpx 48rpx, #2563eb 3rpx, transparent 4rpx);
  background-size: 96rpx 96rpx, 96rpx 96rpx, 192rpx 192rpx;
  pointer-events: none;
}

/* =========== 卡片 =========== */
.login-card {
  width: 100%;
  max-width: 640rpx;
  background: #ffffff;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 50rpx rgba(30, 58, 138, 0.10), 0 2rpx 16rpx rgba(0,0,0,0.04);
  padding: 50rpx 44rpx 40rpx;
  position: relative;
  z-index: 1;
}

/* =========== 品牌头部 =========== */
.card-header {
  margin-bottom: 36rpx;
}
.brand {
  display: flex;
  align-items: center;
}
.logo-icon {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #e8f0fe 0%, #d0e0fc 100%);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-right: 22rpx;
}
.logo-shield {
  font-size: 44rpx;
}
.logo-bolt {
  font-size: 22rpx;
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
}
.brand-text {
  display: flex;
  flex-direction: column;
}
.brand-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #1e3a8a;
  letter-spacing: 2rpx;
}
.brand-desc {
  font-size: 24rpx;
  color: #94a3b8;
  margin-top: 4rpx;
}

/* =========== Tab 切换 =========== */
.tab-bar {
  display: flex;
  border-bottom: 2rpx solid #f0f2f5;
  margin-bottom: 36rpx;
}
.tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 18rpx;
  position: relative;
  cursor: pointer;
}
.tab-text {
  font-size: 30rpx;
  color: #94a3b8;
  font-weight: 500;
}
.tab.active .tab-text {
  color: #1e40af;
  font-weight: 600;
}
.tab-line {
  position: absolute;
  bottom: -2rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 6rpx;
  background: transparent;
  border-radius: 3rpx;
}
.tab.active .tab-line {
  background: #2563eb;
  width: 80rpx;
}

/* =========== 表单输入 =========== */
.form-body {
  display: flex;
  flex-direction: column;
}
.field {
  display: flex;
  align-items: center;
  height: 92rpx;
  background: #f8fafc;
  border: 2rpx solid #e8ecf0;
  border-radius: 18rpx;
  padding: 0 20rpx;
  margin-bottom: 22rpx;
  transition: border-color 0.2s;
}
.field:focus-within {
  border-color: #2563eb;
  background: #ffffff;
  box-shadow: 0 0 0 6rpx rgba(37, 99, 235, 0.06);
}
.field-icon {
  font-size: 34rpx;
  width: 52rpx;
  text-align: center;
  flex-shrink: 0;
}
.field-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #1e293b;
  padding: 0 8rpx;
}
.field-flex {
  padding-right: 20rpx;
}
.field-suffix {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
  color: #94a3b8;
}
.field-suffix:active {
  background: #f1f5f9;
}

/* 验证码按钮 */
.sms-tag {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  font-size: 24rpx;
  color: #2563eb;
  background: #eff6ff;
  border-radius: 12rpx;
  font-weight: 500;
  white-space: nowrap;
}
.sms-tag:active {
  background: #dbeafe;
}
.sms-tag.disabled {
  color: #94a3b8;
  background: #f1f5f9;
}

/* =========== 登录按钮 =========== */
.btn-login {
  width: 100%;
  height: 92rpx;
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 50%, #1d4ed8 100%);
  color: #ffffff;
  border: none;
  border-radius: 18rpx;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 8rpx;
  margin-top: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(30, 64, 175, 0.25);
  transition: all 0.2s;
}
.btn-login:active {
  transform: scale(0.985);
  box-shadow: 0 4rpx 12rpx rgba(30, 64, 175, 0.20);
}

/* =========== 底部链接 =========== */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 36rpx;
  gap: 20rpx;
}
.footer-link {
  font-size: 26rpx;
  color: #94a3b8;
  padding: 4rpx 8rpx;
}
.footer-divider {
  width: 2rpx;
  height: 20rpx;
  background: #e2e8f0;
}

/* =========== Mock 提示 =========== */
.mock-tips {
  margin-top: 28rpx;
  padding: 18rpx 24rpx;
  background: #f8fafc;
  border: 2rpx solid #f1f5f9;
  border-radius: 14rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}
.mock-item {
  font-size: 22rpx;
  color: #c0c8d4;
}
</style>
