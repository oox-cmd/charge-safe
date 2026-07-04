<template>
  <view class="repair-list-page">
    <view class="repair-item" v-for="item in repairs" :key="item.id">
      <view class="repair-header">
        <text class="repair-no">{{ item.ticketNo }}</text>
        <text :class="['status-tag', item.status]">{{ getStatusText(item.status) }}</text>
      </view>
      <view class="repair-body">
        <text class="fault-desc">{{ item.faultDesc }}</text>
        <text class="fault-type">类型: {{ item.faultType }}</text>
      </view>
      <view class="repair-footer">
        <text class="time">{{ item.createdTime }}</text>
      </view>
    </view>
    <view class="empty-tip" v-if="repairs.length === 0">
      <text>暂无报修记录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const repairs = ref([
  { id: 1, ticketNo: 'TK202607040001', faultDesc: '1号端口无法充电', faultType: '无法充电', status: 'PROCESSING', createdTime: '2026-07-04 09:00' },
  { id: 2, ticketNo: 'TK202607030001', faultDesc: '2号端口接触不良', faultType: '端口损坏', status: 'FINISHED', createdTime: '2026-07-03 14:30' }
])

const getStatusText = (status) => {
  const map = { 'PENDING': '待受理', 'DISPATCHED': '已派单', 'PROCESSING': '处理中', 'FINISHED': '已完成', 'CLOSED': '已关闭' }
  return map[status] || status
}
</script>

<style scoped>
.repair-list-page {
  padding: 20rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.repair-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}
.repair-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}
.repair-no {
  font-size: 28rpx;
  font-weight: bold;
}
.status-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.status-tag.PENDING { background: #fdf6ec; color: #e6a23c; }
.status-tag.PROCESSING { background: #ecf5ff; color: #409eff; }
.status-tag.FINISHED { background: #f0f9eb; color: #67c23a; }
.fault-desc {
  font-size: 26rpx;
  display: block;
  margin-bottom: 8rpx;
}
.fault-type {
  font-size: 22rpx;
  color: #999;
  display: block;
}
.repair-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 12rpx;
  margin-top: 12rpx;
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
