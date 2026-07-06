<template>
  <view class="index-page">
    <!-- 顶部信息 -->
    <view class="header">
      <view class="user-info">
        <text class="greeting">您好，{{ userStore.nickname || '居民' }}</text>
        <view class="community-row" @click="showPicker = true">
          <text class="loc-icon">📍</text>
          <text class="community-name">{{ userStore.communityName || '点击选择小区' }}</text>
          <text class="switch-arrow">›</text>
        </view>
      </view>
      <view class="notification" @click="goNotification">
        <text class="badge" v-if="unreadCount > 0">{{ unreadCount }}</text>
        <text class="icon">🔔</text>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="features">
      <view class="feature-item" @click="goParking">
        <text class="feature-icon">⚡</text>
        <text class="feature-text">充电车棚</text>
      </view>
      <view class="feature-item" @click="goReservation">
        <text class="feature-icon">📋</text>
        <text class="feature-text">我的预约</text>
      </view>
      <view class="feature-item" @click="goOrder">
        <text class="feature-icon">📄</text>
        <text class="feature-text">充电订单</text>
      </view>
      <view class="feature-item" @click="goRepair">
        <text class="feature-icon">🔧</text>
        <text class="feature-text">设备报修</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-card" @click="goParking">
        <view class="action-info">
          <text class="action-title">查看空闲端口</text>
          <text class="action-desc">查看附近车棚可用充电端口</text>
        </view>
        <text class="action-arrow">→</text>
      </view>
      <view class="action-card" @click="goScan">
        <view class="action-info">
          <text class="action-title">扫码充电</text>
          <text class="action-desc">扫描端口二维码开始充电</text>
        </view>
        <text class="action-arrow">→</text>
      </view>
    </view>

    <!-- 最近订单 -->
    <view class="recent-orders">
      <view class="section-header">
        <text class="section-title">最近订单</text>
        <text class="section-more" @click="goOrder">查看更多</text>
      </view>
      <view class="order-list">
        <view class="order-item" v-for="order in recentOrders" :key="order.id">
          <view class="order-info">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="order-time">{{ order.startTime }}</text>
          </view>
          <view class="order-status">
            <text :class="['status-tag', order.status]">{{ getStatusText(order.status) }}</text>
            <text class="order-amount" v-if="order.amount">¥{{ order.amount }}</text>
          </view>
        </view>
        <view class="empty-tip" v-if="recentOrders.length === 0">
          <text>暂无订单记录</text>
        </view>
      </view>
    </view>

    <!-- ========== 小区选择弹窗 ========== -->
    <view class="picker-mask" v-if="showPicker" @click="showPicker = false">
      <view class="picker-sheet" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择小区</text>
          <text class="picker-close" @click="showPicker = false">✕</text>
        </view>
        <!-- 搜索栏 -->
        <view class="search-bar">
          <text class="search-icon">🔍</text>
          <input
            v-model="searchText"
            class="search-input"
            placeholder="搜索小区名称"
            placeholder-style="color:#ccc"
            @input="filterCommunities"
          />
          <text class="locate-btn" @click="autoLocate">📍 定位</text>
        </view>
        <!-- 列表 -->
        <scroll-view class="picker-list" scroll-y>
          <view
            v-for="item in filteredList"
            :key="item.id"
            :class="['picker-item', { selected: userStore.communityId === item.id }]"
            @click="selectCommunity(item)"
          >
            <view class="item-info">
              <text class="item-name">{{ item.name }}</text>
              <text class="item-addr">{{ item.address }}</text>
            </view>
            <view class="item-extra">
              <text class="item-ports">🟢 {{ item.idlePorts }} 空闲</text>
              <text class="item-check" v-if="userStore.communityId === item.id">✓</text>
            </view>
          </view>
          <view class="picker-empty" v-if="filteredList.length === 0">
            <text>未找到匹配小区</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAllCommunities, getNearestCommunity } from '@/api/community'
import { getUnreadCount } from '@/api/user'

const userStore = useUserStore()

// ============ 状态 ============
const unreadCount = ref(3)
const showPicker = ref(false)
const searchText = ref('')
const communityList = ref([])

const recentOrders = ref([
  { id: 1, orderNo: 'ORD202601010001', startTime: '2026-01-01 08:00', status: 'COMPLETED', amount: '3.75' },
  { id: 2, orderNo: 'ORD202601020001', startTime: '2026-01-02 09:30', status: 'CHARGING', amount: null }
])

// ============ 过滤列表 ============
const filteredList = computed(() => {
  if (!searchText.value) return communityList.value
  const kw = searchText.value.toLowerCase()
  return communityList.value.filter(c =>
    c.name.toLowerCase().includes(kw) || c.address.toLowerCase().includes(kw)
  )
})

// ============ 初始化 ============
onMounted(async () => {
  // 加载小区列表
  try {
    const res = await getAllCommunities()
    if (res.code === 200 && res.data) {
      communityList.value = res.data
    }
  } catch { /* mock 兜底 */ }

  // 如果没有选过小区，尝试 GPS 自动定位
  if (!userStore.communityId) {
    await autoLocate()
  }

  // 获取未读消息
  try {
    const res = await getUnreadCount()
    if (res.code === 200 && res.data) unreadCount.value = res.data.count
  } catch { /* ignore */ }
})

// ============ 定位 ============
async function autoLocate() {
  try {
    const pos = await new Promise((resolve, reject) => {
      uni.getLocation({
        type: 'gcj02',
        success: resolve,
        fail: reject
      })
    })
    // 匹配最近小区
    const res = await getNearestCommunity(pos.latitude, pos.longitude)
    if (res.code === 200 && res.data) {
      userStore.setCommunity(res.data.id, res.data.name)
    }
  } catch {
    // GPS 失败 → 用第一个默认
    if (!userStore.communityId && communityList.value.length > 0) {
      const first = communityList.value[0]
      userStore.setCommunity(first.id, first.name)
    }
  }
}

// ============ 选择小区 ============
function selectCommunity(item) {
  userStore.setCommunity(item.id, item.name)
  showPicker.value = false
  searchText.value = ''
}

// ============ 状态映射 ============
const getStatusText = (status) => {
  const map = { 'CHARGING': '充电中', 'COMPLETED': '已完成', 'SETTLED': '已结算' }
  return map[status] || status
}

// ============ 导航 ============
const goParking = () => uni.navigateTo({ url: '/pages/parking/index' })
const goReservation = () => uni.navigateTo({ url: '/pages/reservation/index' })
const goOrder = () => uni.navigateTo({ url: '/pages/order/list' })
const goRepair = () => uni.navigateTo({ url: '/pages/repair/list' })
const goNotification = () => uni.navigateTo({ url: '/pages/notification/list' })
const goScan = () => {
  uni.scanCode({
    success: (res) => uni.showToast({ title: '扫码成功: ' + res.result, icon: 'none' })
  })
}
</script>

<style scoped>
.index-page { padding: 20rpx; background: #f5f7fa; min-height: 100vh; }

/* ===== 头部 ===== */
.header {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 30rpx; background: #409eff; border-radius: 16rpx; color: #fff; margin-bottom: 30rpx;
}
.user-info { flex: 1; }
.greeting { font-size: 36rpx; font-weight: bold; display: block; }
.community-row { display: flex; align-items: center; margin-top: 14rpx; }
.loc-icon { font-size: 28rpx; margin-right: 8rpx; }
.community-name { font-size: 26rpx; opacity: 0.9; }
.switch-arrow { font-size: 28rpx; opacity: 0.7; margin-left: 6rpx; }
.notification { position: relative; margin-top: 4rpx; }
.icon { font-size: 44rpx; }
.badge {
  position: absolute; top: -8rpx; right: -8rpx; background: #f56c6c; color: #fff;
  font-size: 20rpx; padding: 4rpx 10rpx; border-radius: 20rpx; min-width: 32rpx; text-align: center;
}

/* ===== 功能入口 ===== */
.features { display: flex; justify-content: space-around; background: #fff; border-radius: 16rpx; padding: 30rpx 0; margin-bottom: 30rpx; }
.feature-item { text-align: center; }
.feature-icon { font-size: 48rpx; display: block; }
.feature-text { font-size: 24rpx; color: #666; margin-top: 10rpx; display: block; }

/* ===== 快捷操作 ===== */
.quick-actions { margin-bottom: 30rpx; }
.action-card {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border-radius: 16rpx; padding: 30rpx; margin-bottom: 20rpx;
}
.action-title { font-size: 30rpx; font-weight: bold; display: block; }
.action-desc { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }
.action-arrow { font-size: 32rpx; color: #999; }

/* ===== 最近订单 ===== */
.recent-orders { background: #fff; border-radius: 16rpx; padding: 30rpx; }
.section-header { display: flex; justify-content: space-between; margin-bottom: 20rpx; }
.section-title { font-size: 30rpx; font-weight: bold; }
.section-more { font-size: 24rpx; color: #409eff; }
.order-item { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; border-bottom: 1px solid #f0f0f0; }
.order-no { font-size: 28rpx; display: block; }
.order-time { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }
.status-tag { font-size: 24rpx; padding: 4rpx 12rpx; border-radius: 8rpx; display: block; }
.status-tag.CHARGING { background: #fdf6ec; color: #e6a23c; }
.status-tag.COMPLETED { background: #f0f9eb; color: #67c23a; }
.order-amount { font-size: 28rpx; color: #f56c6c; margin-top: 8rpx; display: block; }
.empty-tip { text-align: center; padding: 40rpx; color: #999; }

/* ===== 小区选择弹窗 ===== */
.picker-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4); z-index: 999;
  display: flex; align-items: flex-end;
}
.picker-sheet {
  width: 100%; max-height: 70vh; background: #fff;
  border-radius: 24rpx 24rpx 0 0; display: flex; flex-direction: column;
}
.picker-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 30rpx 30rpx 0;
}
.picker-title { font-size: 34rpx; font-weight: bold; color: #333; }
.picker-close { font-size: 36rpx; color: #999; padding: 10rpx; }

/* 搜索栏 */
.search-bar {
  display: flex; align-items: center; margin: 20rpx 30rpx;
  background: #f5f7fa; border-radius: 12rpx; padding: 0 20rpx; height: 72rpx;
}
.search-icon { font-size: 30rpx; margin-right: 12rpx; }
.search-input { flex: 1; font-size: 28rpx; color: #333; }
.locate-btn {
  font-size: 24rpx; color: #409eff; padding: 10rpx 20rpx;
  background: #ecf5ff; border-radius: 8rpx; flex-shrink: 0; margin-left: 12rpx;
}

/* 列表 */
.picker-list { flex: 1; overflow-y: auto; padding: 0 30rpx 40rpx; }
.picker-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 28rpx 0; border-bottom: 2rpx solid #f5f7fa;
}
.picker-item:active { background: #f8f9fa; margin: 0 -30rpx; padding: 28rpx 30rpx; }
.picker-item.selected { background: #ecf5ff; margin: 0 -30rpx; padding: 28rpx 30rpx; border-radius: 8rpx; border-bottom: none; }
.item-info { flex: 1; }
.item-name { font-size: 30rpx; font-weight: 500; color: #333; display: block; }
.item-addr { font-size: 24rpx; color: #999; margin-top: 6rpx; display: block; }
.item-extra { display: flex; align-items: center; gap: 16rpx; }
.item-ports { font-size: 24rpx; color: #67c23a; }
.item-check { font-size: 32rpx; color: #409eff; font-weight: bold; }
.picker-empty { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
</style>
