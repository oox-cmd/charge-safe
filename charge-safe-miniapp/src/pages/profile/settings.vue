<template>
  <view class="page">
    <!-- 账号安全 -->
    <view class="section-label">账号安全</view>
    <view class="card">
      <view class="item" @click="showPwdModal = true">
        <text class="item-icon">🔑</text>
        <text class="item-text">修改密码</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 通用 -->
    <view class="section-label">通用</view>
    <view class="card">
      <view class="item" @click="clearCache">
        <text class="item-icon">🧹</text>
        <text class="item-text">清除缓存</text>
        <view class="right"><text class="hint">{{ cacheSize }}</text><text class="arrow">›</text></view>
      </view>
      <view class="item" @click="showAbout">
        <text class="item-icon">ℹ️</text>
        <text class="item-text">关于我们</text>
        <view class="right"><text class="hint">v1.0.0</text><text class="arrow">›</text></view>
      </view>
    </view>

    <!-- 退出 -->
    <view class="logout-area">
      <button class="btn-logout" @click="handleLogout">退出登录</button>
    </view>

    <!-- ====== 修改密码弹窗 ====== -->
    <view class="modal-mask" v-if="showPwdModal" @click="closePwdModal">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">修改密码</text>
          <text class="modal-close" @click="closePwdModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="pwd-item">
            <text class="pwd-label">旧密码</text>
            <input v-model="pwd.oldPassword" :type="s1 ? 'text' : 'password'" placeholder="请输入旧密码" class="pwd-input" />
            <text class="toggle" @click="s1 = !s1">{{ s1 ? '🙈' : '👁️' }}</text>
          </view>
          <view class="pwd-item">
            <text class="pwd-label">新密码</text>
            <input v-model="pwd.newPassword" :type="s2 ? 'text' : 'password'" placeholder="6-20位新密码" class="pwd-input" />
            <text class="toggle" @click="s2 = !s2">{{ s2 ? '🙈' : '👁️' }}</text>
          </view>
          <view class="pwd-item">
            <text class="pwd-label">确认新密码</text>
            <input v-model="pwd.confirmPassword" :type="s3 ? 'text' : 'password'" placeholder="请再次输入" class="pwd-input" />
            <text class="toggle" @click="s3 = !s3">{{ s3 ? '🙈' : '👁️' }}</text>
          </view>
          <text class="pwd-hint">Mock 模式旧密码为：123456</text>
        </view>
        <view class="modal-footer">
          <view class="btn-cancel" @click="closePwdModal">取消</view>
          <view :class="['btn-confirm', { disabled: changing }]" @click="handleChangePwd">{{ changing ? '修改中...' : '确认修改' }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { redirectTo, showToast, showLoading, hideLoading } from '@/utils'

const userStore = useUserStore()
const cacheSize = ref('2.3 MB')
const showPwdModal = ref(false)
const s1 = ref(false); const s2 = ref(false); const s3 = ref(false)
const changing = ref(false)
const pwd = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

function closePwdModal() {
  showPwdModal.value = false
  Object.assign(pwd, { oldPassword: '', newPassword: '', confirmPassword: '' })
  s1.value = s2.value = s3.value = false
}

async function handleChangePwd() {
  if (!pwd.oldPassword) return showToast('请输入旧密码')
  if (!pwd.newPassword || pwd.newPassword.length < 6) return showToast('新密码至少6位')
  if (pwd.newPassword !== pwd.confirmPassword) return showToast('两次密码输入不一致')
  changing.value = true; showLoading('修改中...')
  try {
    const ok = await userStore.changePassword(pwd)
    if (ok) {
      showToast('密码修改成功，请重新登录')
      closePwdModal()
      setTimeout(() => { userStore.logout(); redirectTo('/pages/login') }, 800)
    }
  } catch { /* handled */ }
  finally { changing.value = false; hideLoading() }
}

function clearCache() {
  uni.showModal({
    title: '清除缓存', content: '确定要清除所有缓存数据吗？',
    confirmText: '确定', confirmColor: '#f56c6c',
    success: (res) => {
      if (res.confirm) {
        const token = uni.getStorageSync('token')
        uni.clearStorageSync()
        if (token) uni.setStorageSync('token', token)
        cacheSize.value = '0 B'
        showToast('缓存已清除')
      }
    }
  })
}

function showAbout() {
  uni.showModal({ title: '关于 ChargeSafe', content: '社区充电安全运维平台 v1.0.0\n\n致力于为社区居民提供安全、便捷的电动车充电服务。', showCancel: false, confirmText: '知道了' })
}

function handleLogout() {
  uni.showModal({
    title: '退出登录', content: '确定要退出当前账号吗？',
    confirmText: '退出', confirmColor: '#f56c6c',
    success: (res) => {
      if (res.confirm) {
        showLoading('退出中...')
        userStore.logout()
        hideLoading()
        showToast('已退出登录')
        setTimeout(() => redirectTo('/pages/login'), 500)
      }
    }
  })
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; padding-bottom: 60rpx; }
.section-label { font-size: 24rpx; color: #999; padding: 32rpx 30rpx 12rpx; }
.card { background: #fff; margin: 0 20rpx; border-radius: 16rpx; }
.item { display: flex; align-items: center; padding: 30rpx; border-bottom: 2rpx solid #f5f7fa; }
.item:last-child { border-bottom: none; }
.item-icon { font-size: 36rpx; margin-right: 20rpx; }
.item-text { flex: 1; font-size: 28rpx; color: #333; }
.arrow { font-size: 32rpx; color: #ccc; }
.right { display: flex; align-items: center; }
.hint { font-size: 24rpx; color: #999; margin-right: 8rpx; }

.logout-area { padding: 60rpx 40rpx; }
.btn-logout { background: #fff; color: #f56c6c; border: 2rpx solid #f56c6c; border-radius: 12rpx; font-size: 28rpx; }

/* ===== 弹窗 ===== */
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 999; padding: 60rpx; }
.modal { width: 100%; background: #fff; border-radius: 20rpx; overflow: hidden; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 36rpx 30rpx 10rpx; }
.modal-title { font-size: 34rpx; font-weight: bold; color: #333; }
.modal-close { font-size: 36rpx; color: #999; padding: 10rpx; }
.modal-body { padding: 20rpx 30rpx; }
.pwd-item { position: relative; margin-bottom: 24rpx; }
.pwd-label { font-size: 26rpx; color: #333; margin-bottom: 10rpx; display: block; }
.pwd-input { border: 1px solid #ddd; border-radius: 12rpx; padding: 24rpx 80rpx 24rpx 24rpx; font-size: 28rpx; background: #fafafa; }
.toggle { position: absolute; right: 20rpx; bottom: 18rpx; font-size: 34rpx; }
.pwd-hint { font-size: 22rpx; color: #999; }
.modal-footer { display: flex; border-top: 2rpx solid #f0f0f0; }
.btn-cancel { flex: 1; display: flex; align-items: center; justify-content: center; height: 100rpx; font-size: 30rpx; color: #999; border-right: 2rpx solid #f0f0f0; }
.btn-confirm { flex: 1; display: flex; align-items: center; justify-content: center; height: 100rpx; font-size: 30rpx; color: #409eff; font-weight: 500; }
.btn-confirm.disabled { opacity: .5; }
</style>
