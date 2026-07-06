<template>
  <view class="profile-page">
    <!-- 用户卡片 -->
    <view class="user-card">
      <view class="avatar-box" @click="changeAvatar">
        <image v-if="userStore.avatar" :src="userStore.avatar" class="avatar-img" mode="aspectFill" />
        <view v-else class="avatar-placeholder"><text class="avatar-text">{{ initial }}</text></view>
        <view class="camera-tag">📷</view>
      </view>
      <view class="user-text">
        <text class="nickname">{{ userStore.nickname || '未设置昵称' }}</text>
        <text class="phone">{{ maskPhone(userStore.phone) }}</text>
      </view>
      <view class="edit-btn" @click="goEditProfile">
        <text class="edit-text">编辑资料 ›</text>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="menu-list">
      <view class="menu-item" @click="goOrder">
        <text class="menu-icon">📄</text>
        <text class="menu-text">我的订单</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goReservation">
        <text class="menu-icon">📅</text>
        <text class="menu-text">我的预约</text>
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
        <view class="menu-right">
          <view class="badge" v-if="unread > 0"><text class="badge-text">{{ unread > 99 ? '99+' : unread }}</text></view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 设置 -->
    <view class="menu-list">
      <view class="menu-item" @click="goSettings">
        <text class="menu-icon">⚙️</text>
        <text class="menu-text">设置</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/user'
import { navigateTo, showToast, showLoading, hideLoading, maskPhone } from '@/utils'

const userStore = useUserStore()
const unread = ref(0)

const initial = computed(() => (userStore.nickname || '?').charAt(0).toUpperCase())

onShow(async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200 && res.data) unread.value = res.data.count
  } catch { /* ignore */ }
})

function changeAvatar() {
  uni.chooseImage({
    count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'],
    success: async (res) => {
      showLoading('上传中...')
      try {
        const url = await userStore.uploadAvatar(res.tempFilePaths[0])
        if (url) showToast('头像更新成功')
      } catch { showToast('上传失败') }
      finally { hideLoading() }
    }
  })
}

function goOrder() { navigateTo('/pages/order/list') }
function goReservation() { navigateTo('/pages/reservation/index') }
function goRepair() { navigateTo('/pages/repair/list') }
function goNotification() { navigateTo('/pages/notification/list') }
function goEditProfile() { navigateTo('/pages/profile/edit-profile') }
function goSettings() { navigateTo('/pages/profile/settings') }
</script>

<style scoped>
.profile-page { background: #f5f7fa; min-height: 100vh; padding-bottom: 40rpx; }

.user-card { background: #409eff; padding: 40rpx 30rpx; display: flex; align-items: center; }
.avatar-box { position: relative; width: 120rpx; height: 120rpx; border-radius: 50%; border: 4rpx solid rgba(255,255,255,.5); overflow: hidden; flex-shrink: 0; }
.avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.avatar-placeholder { width: 100%; height: 100%; background: rgba(255,255,255,.2); display: flex; align-items: center; justify-content: center; }
.avatar-text { font-size: 48rpx; font-weight: bold; color: #fff; }
.camera-tag { position: absolute; bottom: -4rpx; right: -4rpx; width: 40rpx; height: 40rpx; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 22rpx; }
.user-text { flex: 1; margin-left: 24rpx; }
.nickname { font-size: 36rpx; font-weight: bold; color: #fff; display: block; }
.phone { font-size: 24rpx; color: rgba(255,255,255,.8); margin-top: 8rpx; display: block; }
.edit-btn { padding: 10rpx 24rpx; background: rgba(255,255,255,.2); border-radius: 24rpx; border: 2rpx solid rgba(255,255,255,.3); }
.edit-text { font-size: 24rpx; color: #fff; }

.menu-list { background: #fff; margin: 20rpx; border-radius: 16rpx; }
.menu-item { display: flex; align-items: center; padding: 30rpx; border-bottom: 2rpx solid #f5f7fa; }
.menu-item:last-child { border-bottom: none; }
.menu-icon { font-size: 36rpx; margin-right: 20rpx; }
.menu-text { flex: 1; font-size: 28rpx; color: #333; }
.menu-arrow { font-size: 32rpx; color: #ccc; }
.menu-right { display: flex; align-items: center; }
.badge { min-width: 36rpx; height: 36rpx; background: #f56c6c; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; padding: 0 8rpx; margin-right: 12rpx; }
.badge-text { font-size: 20rpx; color: #fff; font-weight: bold; }
</style>
