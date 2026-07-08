<template>
  <view class="page">
    <!-- 用户卡片 -->
    <view class="user-card">
      <view class="card-inner">
        <view class="avatar-wrap" @click="changeAvatar">
          <image v-if="userStore.avatar" :src="userStore.avatar" class="avatar-img" mode="aspectFill" />
          <view v-else class="avatar-knockout"><text class="avatar-letter">{{ initial }}</text></view>
          <view class="avatar-cam">📷</view>
        </view>
        <view class="user-col">
          <text class="user-name">{{ userStore.nickname || '未设置昵称' }}</text>
          <text class="user-phone">{{ maskPhone(userStore.phone) }}</text>
        </view>
        <view class="edit-tag" @click="goEditProfile"><text>编辑资料 ›</text></view>
      </view>
    </view>

    <!-- 菜单 -->
    <view class="menu-card">
      <view class="menu-item" @click="goOrder">
        <view class="mi-icon" style="background:#e8f0fe;"><text>📄</text></view>
        <text class="mi-label">我的订单</text>
        <text class="mi-arrow">›</text>
      </view>
      <view class="menu-item" @click="goReservation">
        <view class="mi-icon" style="background:#fef3e2;"><text>📅</text></view>
        <text class="mi-label">我的预约</text>
        <text class="mi-arrow">›</text>
      </view>
      <view class="menu-item" @click="goRepair">
        <view class="mi-icon" style="background:#fce4ec;"><text>🔧</text></view>
        <text class="mi-label">我的报修</text>
        <text class="mi-arrow">›</text>
      </view>
      <view class="menu-item" @click="goNotification">
        <view class="mi-icon" style="background:#e8f5e9;"><text>🔔</text></view>
        <text class="mi-label">消息通知</text>
        <view class="mi-right">
          <view class="mi-badge" v-if="unread > 0"><text>{{ unread > 99 ? '99+' : unread }}</text></view>
          <text class="mi-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 设置 -->
    <view class="menu-card">
      <view class="menu-item" @click="goSettings">
        <view class="mi-icon" style="background:#f1f5f9;"><text>⚙️</text></view>
        <text class="mi-label">设置</text>
        <text class="mi-arrow">›</text>
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

onShow(async () => { try { const res = await getUnreadCount(); if (res.code === 200 && res.data) unread.value = res.data.count } catch { /* */ } })

function changeAvatar() {
  uni.chooseImage({ count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'],
    success: async (res) => { showLoading('上传中...'); try { const url = await userStore.uploadAvatar(res.tempFilePaths[0]); if (url) showToast('头像更新成功') } catch { showToast('上传失败') } finally { hideLoading() } }
  })
}
function goEditProfile() { navigateTo('/pages/profile/edit-profile') }
function goSettings() { navigateTo('/pages/profile/settings') }
function goOrder() { navigateTo('/pages/order/list') }
function goReservation() { navigateTo('/pages/reservation/index') }
function goRepair() { navigateTo('/pages/repair/list') }
function goNotification() { navigateTo('/pages/notification/list') }
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding-bottom: 40rpx; }

.user-card { background: linear-gradient(150deg, #1e3a8a 0%, #1e40af 40%, #2563eb 100%); padding: 48rpx 30rpx 40rpx; }
.card-inner { display: flex; align-items: center; }
.avatar-wrap { position: relative; width: 120rpx; height: 120rpx; border-radius: 50%; border: 4rpx solid rgba(255,255,255,.4); overflow: visible; flex-shrink: 0; }
.avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.avatar-knockout { width: 100%; height: 100%; border-radius: 50%; background: rgba(255,255,255,.15); display: flex; align-items: center; justify-content: center; }
.avatar-letter { font-size: 52rpx; font-weight: 700; color: #fff; }
.avatar-cam { position: absolute; bottom: -4rpx; right: -4rpx; width: 44rpx; height: 44rpx; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,.15); }
.user-col { flex: 1; margin-left: 26rpx; }
.user-name { font-size: 38rpx; font-weight: 700; color: #fff; display: block; }
.user-phone { font-size: 26rpx; color: rgba(255,255,255,.7); margin-top: 6rpx; display: block; }
.edit-tag { padding: 12rpx 24rpx; background: rgba(255,255,255,.15); border-radius: 24rpx; border: 2rpx solid rgba(255,255,255,.25); font-size: 24rpx; color: #fff; }

.menu-card { background: #fff; border-radius: 22rpx; margin: 20rpx 28rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); overflow: hidden; }
.menu-item { display: flex; align-items: center; padding: 28rpx; border-bottom: 2rpx solid #f8fafc; }
.menu-item:last-child { border-bottom: none; }
.menu-item:active { background: #f8fafc; }
.mi-icon { width: 72rpx; height: 72rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; font-size: 34rpx; margin-right: 22rpx; }
.mi-label { flex: 1; font-size: 30rpx; color: #1e293b; font-weight: 500; }
.mi-arrow { font-size: 32rpx; color: #cbd5e1; }
.mi-right { display: flex; align-items: center; }
.mi-badge { min-width: 36rpx; height: 36rpx; background: #ef4444; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; padding: 0 8rpx; margin-right: 10rpx; font-size: 20rpx; color: #fff; font-weight: 700; }
</style>
