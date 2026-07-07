<template>
  <view class="page">
    <view class="bg-pattern"></view>
    <view class="content">
      <!-- 账号安全 -->
      <view class="section-label">账号安全</view>
      <view class="menu-card">
        <view class="menu-item" @click="showPwdModal = true">
          <view class="item-left"><text class="item-icon">🔑</text><text class="item-text">修改密码</text></view>
          <text class="item-arrow">›</text>
        </view>
      </view>

      <!-- 通用 -->
      <view class="section-label">通用</view>
      <view class="menu-card">
        <view class="menu-item" @click="clearCache">
          <view class="item-left"><text class="item-icon">🧹</text><text class="item-text">清除缓存</text></view>
          <view class="item-right"><text class="item-hint">{{ cacheSize }}</text><text class="item-arrow">›</text></view>
        </view>
        <view class="menu-item" @click="showAbout">
          <view class="item-left"><text class="item-icon">ℹ️</text><text class="item-text">关于我们</text></view>
          <view class="item-right"><text class="item-hint">v1.0.0</text><text class="item-arrow">›</text></view>
        </view>
      </view>

      <!-- 退出 -->
      <view class="logout-box">
        <button class="btn-logout" @click="handleLogout">退出登录</button>
      </view>
    </view>

    <!-- 修改密码弹窗 -->
    <view class="modal-mask" v-if="showPwdModal" @click="closePwdModal">
      <view class="modal-card" @click.stop>
        <view class="modal-head">
          <text class="modal-title">修改密码</text>
          <text class="modal-close" @click="closePwdModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="pwd-field">
            <text class="pwd-label">旧密码</text>
            <view class="field"><input v-model="pwd.oldPassword" :type="s1 ? 'text' : 'password'" placeholder="请输入旧密码" placeholder-style="color:#c0c4cc" class="field-input field-flex" /><view class="field-suffix" @click="s1 = !s1"><text>{{ s1 ? '🙈' : '👁' }}</text></view></view>
          </view>
          <view class="pwd-field">
            <text class="pwd-label">新密码</text>
            <view class="field"><input v-model="pwd.newPassword" :type="s2 ? 'text' : 'password'" placeholder="6-20位新密码" placeholder-style="color:#c0c4cc" class="field-input field-flex" /><view class="field-suffix" @click="s2 = !s2"><text>{{ s2 ? '🙈' : '👁' }}</text></view></view>
          </view>
          <view class="pwd-field">
            <text class="pwd-label">确认新密码</text>
            <view class="field"><input v-model="pwd.confirmPassword" :type="s3 ? 'text' : 'password'" placeholder="请再次输入" placeholder-style="color:#c0c4cc" class="field-input field-flex" /><view class="field-suffix" @click="s3 = !s3"><text>{{ s3 ? '🙈' : '👁' }}</text></view></view>
          </view>
          <text class="pwd-hint">Mock 模式旧密码为：123456</text>
        </view>
        <view class="modal-foot">
          <view class="btn-cancel" @click="closePwdModal">取消</view>
          <view :class="['btn-confirm', { off: changing }]" @click="handleChangePwd">{{ changing ? '修改中...' : '确认修改' }}</view>
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
const cacheSize = ref('2.3 MB'); const showPwdModal = ref(false)
const s1 = ref(false); const s2 = ref(false); const s3 = ref(false); const changing = ref(false)
const pwd = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

function closePwdModal() { showPwdModal.value = false; Object.assign(pwd, { oldPassword: '', newPassword: '', confirmPassword: '' }); s1.value = s2.value = s3.value = false }
async function handleChangePwd() {
  if (!pwd.oldPassword) return showToast('请输入旧密码')
  if (!pwd.newPassword || pwd.newPassword.length < 6) return showToast('新密码至少6位')
  if (pwd.newPassword !== pwd.confirmPassword) return showToast('两次密码输入不一致')
  changing.value = true; showLoading('修改中...')
  try { const ok = await userStore.changePassword(pwd); if (ok) { showToast('密码修改成功，请重新登录'); closePwdModal(); setTimeout(() => { userStore.logout(); redirectTo('/pages/login') }, 800) } } catch { /* handled */ }
  finally { changing.value = false; hideLoading() }
}
function clearCache() {
  uni.showModal({ title: '清除缓存', content: '确定要清除所有缓存数据吗？', confirmText: '确定', confirmColor: '#ef4444', success: (res) => { if (res.confirm) { const token = uni.getStorageSync('token'); uni.clearStorageSync(); if (token) uni.setStorageSync('token', token); cacheSize.value = '0 B'; showToast('缓存已清除') } } })
}
function showAbout() { uni.showModal({ title: '关于 ChargeSafe', content: '社区充电安全运维平台 v1.0.0\n\n致力于为社区居民提供安全、便捷的电动车充电服务。', showCancel: false, confirmText: '知道了' }) }
function handleLogout() {
  uni.showModal({ title: '退出登录', content: '确定要退出当前账号吗？', confirmText: '退出', confirmColor: '#ef4444', success: (res) => { if (res.confirm) { showLoading('退出中...'); userStore.logout(); hideLoading(); showToast('已退出登录'); setTimeout(() => redirectTo('/pages/login'), 500) } } })
}
</script>

<style scoped>
.page { min-height: 100vh; background: radial-gradient(ellipse at 20% 0%, rgba(59,130,246,.08) 0%, transparent 60%), radial-gradient(ellipse at 80% 100%, rgba(99,102,241,.06) 0%, transparent 55%), linear-gradient(175deg, #eef6ff 0%, #f0f4f8 100%); position: relative; overflow: hidden; }
.bg-pattern { position: absolute; inset: 0; opacity: .04; background-image: repeating-linear-gradient(90deg, transparent, transparent 96rpx, #1e3a8a 96rpx, #1e3a8a 97rpx), repeating-linear-gradient(0deg, transparent, transparent 96rpx, #1e3a8a 96rpx, #1e3a8a 97rpx); background-size: 96rpx 96rpx, 96rpx 96rpx; pointer-events: none; }
.content { position: relative; z-index: 1; padding: 40rpx 30rpx; }

.section-label { font-size: 24rpx; color: #94a3b8; padding: 28rpx 10rpx 10rpx; font-weight: 500; letter-spacing: 2rpx; }
.menu-card { background: #fff; border-radius: 24rpx; box-shadow: 0 2rpx 16rpx rgba(30,58,138,.04); overflow: hidden; }
.menu-item { display: flex; align-items: center; justify-content: space-between; padding: 32rpx 28rpx; border-bottom: 2rpx solid #f8fafc; }
.menu-item:last-child { border-bottom: none; }
.menu-item:active { background: #f8fafc; }
.item-left { display: flex; align-items: center; }
.item-icon { font-size: 36rpx; margin-right: 20rpx; width: 44rpx; text-align: center; }
.item-text { font-size: 30rpx; color: #1e293b; }
.item-right { display: flex; align-items: center; }
.item-hint { font-size: 24rpx; color: #94a3b8; margin-right: 8rpx; }
.item-arrow { font-size: 32rpx; color: #cbd5e1; }

.logout-box { padding: 60rpx 10rpx; }
.btn-logout { width: 100%; height: 88rpx; background: #fff; color: #ef4444; border: 2rpx solid #fecaca; border-radius: 18rpx; font-size: 30rpx; }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,.4); display: flex; align-items: center; justify-content: center; z-index: 999; padding: 60rpx; }
.modal-card { width: 100%; max-width: 600rpx; background: #fff; border-radius: 28rpx; overflow: hidden; }
.modal-head { display: flex; justify-content: space-between; align-items: center; padding: 36rpx 30rpx 0; }
.modal-title { font-size: 34rpx; font-weight: 700; color: #1e3a8a; }
.modal-close { font-size: 34rpx; color: #94a3b8; padding: 8rpx; }
.modal-body { padding: 24rpx 30rpx 10rpx; }
.pwd-field { margin-bottom: 20rpx; }
.pwd-label { font-size: 26rpx; color: #475569; margin-bottom: 10rpx; display: block; font-weight: 500; }
.field { display: flex; align-items: center; height: 80rpx; background: #f8fafc; border: 2rpx solid #e8ecf0; border-radius: 14rpx; padding: 0 18rpx; }
.field-input { flex: 1; height: 100%; font-size: 28rpx; color: #1e293b; }
.field-flex { padding-right: 16rpx; }
.field-suffix { width: 52rpx; height: 52rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #94a3b8; flex-shrink: 0; }
.pwd-hint { font-size: 22rpx; color: #94a3b8; }
.modal-foot { display: flex; border-top: 2rpx solid #f1f5f9; margin-top: 8rpx; }
.btn-cancel { flex: 1; height: 96rpx; display: flex; align-items: center; justify-content: center; font-size: 30rpx; color: #94a3b8; border-right: 2rpx solid #f1f5f9; }
.btn-confirm { flex: 1; height: 96rpx; display: flex; align-items: center; justify-content: center; font-size: 30rpx; color: #2563eb; font-weight: 600; }
.btn-confirm.off { opacity: .4; }
</style>
