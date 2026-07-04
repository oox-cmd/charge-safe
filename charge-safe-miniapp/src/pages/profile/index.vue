<template>
  <view class="profile-page">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="avatar">👤</view>
      <view class="user-info">
        <text class="nickname">{{ userInfo.nickname || '居民用户' }}</text>
        <text class="phone">{{ userInfo.phone || '未设置手机号' }}</text>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="goOrder">
        <text class="menu-icon">📄</text>
        <text class="menu-text">我的订单</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goRepair">
        <text class="menu-icon">🔧</text>
        <text class="menu-text">我的报修</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goNotification">
        <text class="menu-icon">🔔</text>
        <text class="menu-text">消息通知</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-area">
      <button class="btn-logout" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const userInfo = ref({})

const goOrder = () => uni.navigateTo({ url: '/pages/order/list' })
const goRepair = () => uni.navigateTo({ url: '/pages/repair/list' })
const goNotification = () => uni.navigateTo({ url: '/pages/notification/list' })

const handleLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        uni.redirectTo({ url: '/pages/login.vue' })
      }
    }
  })
}

onMounted(() => {
  const info = uni.getStorageSync('userInfo')
  if (info) userInfo.value = info
})
</script>

<style scoped>
.profile-page {
  background: #f5f7fa;
  min-height: 100vh;
}
.user-card {
  background: #409eff;
  padding: 60rpx 40rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.avatar {
  width: 100rpx;
  height: 100rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
}
.nickname {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  display: block;
}
.phone {
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
  margin-top: 8rpx;
  display: block;
}
.menu-list {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f0f0f0;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}
.menu-text {
  flex: 1;
  font-size: 28rpx;
}
.menu-arrow {
  font-size: 32rpx;
  color: #ccc;
}
.logout-area {
  padding: 60rpx 40rpx;
}
.btn-logout {
  background: #fff;
  color: #f56c6c;
  border: 1px solid #f56c6c;
  border-radius: 12rpx;
}
</style>
