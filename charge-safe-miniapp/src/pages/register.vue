<template>
  <view class="page">
    <view class="bg-pattern"></view>
    <view class="card">
      <!-- 头部 -->
      <view class="card-header">
        <view class="brand">
          <view class="logo-icon">🛡️</view>
          <view class="brand-text">
            <text class="brand-name">创建账号</text>
            <text class="brand-desc">注册后即可使用充电服务</text>
          </view>
        </view>
      </view>

      <view class="form-body">
        <view class="field">
          <view class="field-icon">📱</view>
          <input v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号" placeholder-style="color:#c0c4cc" class="field-input" />
        </view>
        <view class="field">
          <view class="field-icon">👤</view>
          <input v-model="form.nickname" maxlength="20" placeholder="给自己取个名字" placeholder-style="color:#c0c4cc" class="field-input" />
        </view>
        <view class="field">
          <view class="field-icon">🔒</view>
          <input v-model="form.password" :type="showPwd ? 'text' : 'password'" placeholder="6-20位密码" placeholder-style="color:#c0c4cc" class="field-input field-flex" />
          <view class="field-suffix" @click="showPwd = !showPwd"><text>{{ showPwd ? '🙈' : '👁' }}</text></view>
        </view>
        <view class="field">
          <view class="field-icon">🔒</view>
          <input v-model="form.confirmPassword" :type="showCfm ? 'text' : 'password'" placeholder="请再次输入密码" placeholder-style="color:#c0c4cc" class="field-input field-flex" />
          <view class="field-suffix" @click="showCfm = !showCfm"><text>{{ showCfm ? '🙈' : '👁' }}</text></view>
        </view>

        <view class="agreement" @click="agreed = !agreed">
          <view :class="['check-box', { on: agreed }]"><text v-if="agreed" class="check-mark">✓</text></view>
          <text class="agree-text">已阅读并同意<text class="agree-link">《用户协议》</text>和<text class="agree-link">《隐私政策》</text></text>
        </view>

        <button :loading="loading" :disabled="!agreed" class="btn-submit" @click="handleRegister">注 册</button>

        <view class="card-footer">
          <text class="footer-link" @click="goBack">已有账号？去登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/auth'
import { switchTab, showToast, isValidPhone, isValidPassword } from '@/utils'

const userStore = useUserStore()
const showPwd = ref(false)
const showCfm = ref(false)
const agreed = ref(false)
const loading = ref(false)
const form = reactive({ phone: '', nickname: '', password: '', confirmPassword: '' })

async function handleRegister() {
  if (!isValidPhone(form.phone)) return showToast('请输入正确的手机号')
  if (!form.nickname.trim()) return showToast('请输入昵称')
  if (!isValidPassword(form.password)) return showToast('密码至少6位')
  if (form.password !== form.confirmPassword) return showToast('两次密码输入不一致')
  if (!agreed.value) return showToast('请先阅读并同意用户协议')
  loading.value = true
  try {
    const res = await register({ phone: form.phone, password: form.password, confirmPassword: form.confirmPassword, nickname: form.nickname.trim() })
    if (res.code === 200 && res.data) { userStore.saveLoginState(res.data); showToast('注册成功'); setTimeout(() => switchTab('/pages/index/index'), 500) }
  } catch { /* handled */ }
  finally { loading.value = false }
}
function goBack() { uni.navigateBack() }
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

.agreement { display: flex; align-items: flex-start; margin: 12rpx 0 24rpx; }
.check-box { width: 32rpx; height: 32rpx; border: 2rpx solid #cbd5e1; border-radius: 8rpx; margin-right: 12rpx; flex-shrink: 0; display: flex; align-items: center; justify-content: center; margin-top: 2rpx; }
.check-box.on { background: #2563eb; border-color: #2563eb; }
.check-mark { font-size: 20rpx; color: #fff; font-weight: bold; }
.agree-text { font-size: 24rpx; color: #94a3b8; line-height: 1.6; }
.agree-link { color: #2563eb; }

.btn-submit { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af, #1d4ed8); color: #fff; border: none; border-radius: 18rpx; font-size: 32rpx; font-weight: 600; letter-spacing: 8rpx; box-shadow: 0 8rpx 24rpx rgba(30,64,175,.22); }
.btn-submit[disabled] { opacity: .45; box-shadow: none; }

.card-footer { text-align: center; margin-top: 32rpx; }
.footer-link { font-size: 26rpx; color: #94a3b8; }
</style>
