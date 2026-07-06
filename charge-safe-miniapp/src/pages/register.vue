<template>
  <view class="page">
    <view class="card">
      <text class="card-title">创建账号</text>

      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号" class="input" />
      </view>
      <view class="form-item">
        <text class="label">昵称</text>
        <input v-model="form.nickname" maxlength="20" placeholder="给自己取个名字" class="input" />
      </view>
      <view class="form-item">
        <text class="label">密码</text>
        <view class="input-wrap">
          <input v-model="form.password" :type="showPwd ? 'text' : 'password'" placeholder="6-20位密码" class="input input-flex" />
          <text class="eye" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁️' }}</text>
        </view>
      </view>
      <view class="form-item">
        <text class="label">确认密码</text>
        <view class="input-wrap">
          <input v-model="form.confirmPassword" :type="showCfm ? 'text' : 'password'" placeholder="请再次输入密码" class="input input-flex" />
          <text class="eye" @click="showCfm = !showCfm">{{ showCfm ? '🙈' : '👁️' }}</text>
        </view>
      </view>

      <!-- 协议 -->
      <view class="agreement" @click="agreed = !agreed">
        <view :class="['checkbox', { checked: agreed }]"><text v-if="agreed" class="check">✓</text></view>
        <text class="agree-text">已阅读并同意<text class="link">《用户协议》</text>和<text class="link">《隐私政策》</text></text>
      </view>

      <button :loading="loading" :disabled="!agreed" class="btn-submit" @click="handleRegister">注 册</button>
      <view class="bottom-link"><text class="link" @click="goBack">已有账号？去登录</text></view>
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
    if (res.code === 200 && res.data) {
      userStore.saveLoginState(res.data)
      showToast('注册成功')
      setTimeout(() => switchTab('/pages/index/index'), 500)
    }
  } catch { /* handled */ }
  finally { loading.value = false }
}

function goBack() { uni.navigateBack() }
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; padding: 40rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 40rpx 30rpx; }
.card-title { font-size: 40rpx; font-weight: bold; display: block; margin-bottom: 40rpx; color: #333; }
.form-item { margin-bottom: 30rpx; }
.label { font-size: 28rpx; color: #333; margin-bottom: 12rpx; display: block; }
.input { border: 1px solid #ddd; border-radius: 12rpx; padding: 24rpx; font-size: 28rpx; background: #fafafa; }
.input-wrap { position: relative; }
.input-flex { padding-right: 80rpx; }
.eye { position: absolute; right: 20rpx; top: 50%; transform: translateY(-70%); font-size: 36rpx; }

.agreement { display: flex; align-items: flex-start; margin: 10rpx 0 30rpx; }
.checkbox { width: 34rpx; height: 34rpx; border: 2px solid #ddd; border-radius: 50%; margin-right: 12rpx; flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.checkbox.checked { background: #409eff; border-color: #409eff; }
.check { font-size: 22rpx; color: #fff; font-weight: bold; }
.agree-text { font-size: 24rpx; color: #999; line-height: 1.6; }
.link { color: #409eff; }

.btn-submit { background: #409eff; color: #fff; border-radius: 12rpx; font-size: 30rpx; }
.btn-submit[disabled] { opacity: 0.5; }
.bottom-link { text-align: center; margin-top: 36rpx; }
</style>
