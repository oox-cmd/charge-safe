<template>
  <view class="login-page">
    <view class="login-header">
      <text class="title">ChargeSafe</text>
      <text class="subtitle">社区充电安全运维平台</text>
    </view>
    <view class="login-form">
      <input v-model="username" placeholder="请输入用户名" class="input" />
      <input v-model="password" type="password" placeholder="请输入密码" class="input" />
      <button @click="handleLogin" :loading="loading" class="btn-login">登 录</button>
    </view>
    <view class="login-tips">
      <text>默认账号: resident01 / 123456</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const username = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = () => {
  if (!username.value || !password.value) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  // 模拟登录
  setTimeout(() => {
    uni.setStorageSync('token', 'mock-token')
    uni.setStorageSync('userInfo', {
      userId: 5,
      username: username.value,
      nickname: '居民赵六',
      roleCode: 'RESIDENT'
    })
    loading.value = false
    uni.switchTab({ url: '/pages/index/index' })
  }, 1000)
}
</script>

<style scoped>
.login-page {
  padding: 100rpx 60rpx;
}
.login-header {
  text-align: center;
  margin-bottom: 80rpx;
}
.title {
  font-size: 48rpx;
  font-weight: bold;
  display: block;
}
.subtitle {
  font-size: 28rpx;
  color: #666;
  margin-top: 16rpx;
  display: block;
}
.login-form {
  margin-top: 40rpx;
}
.input {
  border: 1px solid #ddd;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  font-size: 28rpx;
}
.btn-login {
  background: #409eff;
  color: #fff;
  border-radius: 12rpx;
  margin-top: 40rpx;
}
.login-tips {
  text-align: center;
  margin-top: 40rpx;
  font-size: 24rpx;
  color: #999;
}
</style>
