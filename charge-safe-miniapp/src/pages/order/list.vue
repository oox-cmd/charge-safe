<template>
  <view class="order-list-page">
    <!-- 状态筛选 -->
    <view class="filter-bar">
      <text
        v-for="tab in statusTabs"
        :key="tab.value"
        :class="['filter-item', { active: currentStatus === tab.value }]"
        @click="currentStatus = tab.value"
      >{{ tab.label }}</text>
    </view>

    <!-- 订单列表 -->
    <view class="order-list">
      <view class="order-item" v-for="order in filteredOrders" :key="order.id" @click="goDetail(order)">
        <view class="order-header">
          <text class="order-no">{{ order.orderNo }}</text>
          <text :class="['status-tag', order.status]">{{ getStatusText(order.status) }}</text>
        </view>
        <view class="order-body">
          <view class="info-row">
            <text class="label">充电时长</text>
            <text class="value">{{ order.durationMinutes || '-' }}分钟</text>
          </view>
          <view class="info-row">
            <text class="label">费用</text>
            <text class="value price">¥{{ order.amount || '0.00' }}</text>
          </view>
        </view>
        <view class="order-footer">
          <text class="time">{{ order.startTime }}</text>
        </view>
      </view>
      <view class="empty-tip" v-if="filteredOrders.length === 0">
        <text>暂无订单记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentStatus = ref('')

const statusTabs = [
  { label: '全部', value: '' },
  { label: '充电中', value: 'CHARGING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已结算', value: 'SETTLED' }
]

const orders = ref([
  { id: 1, orderNo: 'ORD202607040001', status: 'CHARGING', durationMinutes: null, amount: null, startTime: '2026-07-04 08:00' },
  { id: 2, orderNo: 'ORD202607030001', status: 'COMPLETED', durationMinutes: 120, amount: '3.00', startTime: '2026-07-03 14:00' },
  { id: 3, orderNo: 'ORD202607020001', status: 'COMPLETED', durationMinutes: 90, amount: '2.25', startTime: '2026-07-02 10:30' }
])

const filteredOrders = computed(() => {
  if (!currentStatus.value) return orders.value
  return orders.value.filter(o => o.status === currentStatus.value)
})

const getStatusText = (status) => {
  const map = { 'CHARGING': '充电中', 'COMPLETED': '已完成', 'SETTLED': '已结算', 'CANCELLED': '已取消' }
  return map[status] || status
}

const goDetail = (order) => {
  uni.navigateTo({ url: `/pages/order/detail?id=${order.id}` })
}
</script>

<style scoped>
.order-list-page {
  background: #f5f7fa;
  min-height: 100vh;
}
.filter-bar {
  display: flex;
  background: #fff;
  padding: 20rpx;
  gap: 20rpx;
}
.filter-item {
  font-size: 26rpx;
  color: #666;
  padding: 10rpx 24rpx;
  border-radius: 20rpx;
}
.filter-item.active {
  background: #409eff;
  color: #fff;
}
.order-list {
  padding: 20rpx;
}
.order-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.order-no {
  font-size: 28rpx;
  font-weight: bold;
}
.status-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.status-tag.CHARGING { background: #fdf6ec; color: #e6a23c; }
.status-tag.COMPLETED { background: #f0f9eb; color: #67c23a; }
.status-tag.SETTLED { background: #ecf5ff; color: #409eff; }
.order-body {
  border-top: 1px solid #f0f0f0;
  padding-top: 16rpx;
}
.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
}
.label {
  font-size: 24rpx;
  color: #999;
}
.value {
  font-size: 24rpx;
}
.value.price {
  color: #f56c6c;
  font-weight: bold;
}
.order-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 16rpx;
}
.time {
  font-size: 22rpx;
  color: #999;
}
.empty-tip {
  text-align: center;
  padding: 80rpx;
  color: #999;
}
</style>
