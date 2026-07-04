<template>
  <view class="reservation-list-page">
    <view class="reservation-item" v-for="item in reservations" :key="item.id">
      <view class="reserv-header">
        <text class="reserv-port">{{ item.portName }}</text>
        <text :class="['status-tag', item.status]">{{ getStatusText(item.status) }}</text>
      </view>
      <view class="reserv-body">
        <view class="info-row">
          <text class="label">预约时间</text>
          <text class="value">{{ item.reserveTime }}</text>
        </view>
        <view class="info-row">
          <text class="label">过期时间</text>
          <text class="value">{{ item.expireTime }}</text>
        </view>
      </view>
      <view class="reserv-footer" v-if="item.status === 'PENDING'">
        <button class="btn-cancel" size="mini" @click="handleCancel(item)">取消预约</button>
      </view>
    </view>
    <view class="empty-tip" v-if="reservations.length === 0">
      <text>暂无预约记录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const reservations = ref([
  { id: 1, portName: '1号端口', status: 'PENDING', reserveTime: '2026-07-04 08:00', expireTime: '2026-07-04 08:30' },
  { id: 2, portName: '2号端口', status: 'CONVERTED', reserveTime: '2026-07-03 14:00', expireTime: '2026-07-03 14:30' }
])

const getStatusText = (status) => {
  const map = { 'PENDING': '待生效', 'CONVERTED': '已转订单', 'CANCELLED': '已取消', 'EXPIRED': '已过期' }
  return map[status] || status
}

const handleCancel = (item) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该预约吗？',
    success: (res) => {
      if (res.confirm) {
        item.status = 'CANCELLED'
        uni.showToast({ title: '已取消', icon: 'success' })
      }
    }
  })
}
</script>

<style scoped>
.reservation-list-page {
  padding: 20rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.reservation-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}
.reserv-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.reserv-port {
  font-size: 30rpx;
  font-weight: bold;
}
.status-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.status-tag.PENDING { background: #fdf6ec; color: #e6a23c; }
.status-tag.CONVERTED { background: #f0f9eb; color: #67c23a; }
.status-tag.CANCELLED { background: #f4f4f5; color: #999; }
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
.reserv-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 16rpx;
  margin-top: 12rpx;
  text-align: right;
}
.btn-cancel {
  background: #fff;
  color: #f56c6c;
  border: 1px solid #f56c6c;
  font-size: 24rpx;
}
.empty-tip {
  text-align: center;
  padding: 80rpx;
  color: #999;
}
</style>
