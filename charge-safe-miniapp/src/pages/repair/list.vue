<template>
  <view class="page">
    <view class="card" v-for="item in repairs" :key="item.id">
      <view class="top"><text class="no">{{ item.ticketNo }}</text><text :class="['tag', item.status]">{{ map[item.status] || item.status }}</text></view>
      <text class="desc">{{ item.faultDesc }}</text>
      <view class="btm"><text class="type">{{ item.faultType }}</text><text class="time">{{ item.createdTime }}</text></view>
    </view>
    <view class="empty-block" v-if="repairs.length === 0"><text>暂无报修记录</text></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
const repairs = ref([
  { id: 1, ticketNo: 'TK202607040001', faultDesc: '1号端口无法充电', faultType: '无法充电', status: 'PROCESSING', createdTime: '2026-07-04 09:00' },
  { id: 2, ticketNo: 'TK202607030001', faultDesc: '2号端口接触不良', faultType: '端口损坏', status: 'FINISHED', createdTime: '2026-07-03 14:30' }
])
const map = { 'PENDING': '待受理', 'DISPATCHED': '已派单', 'PROCESSING': '处理中', 'FINISHED': '已完成', 'CLOSED': '已关闭' }
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 24rpx 28rpx; }
.card { background: #fff; border-radius: 22rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.no { font-size: 28rpx; font-weight: 700; color: #1e293b; }
.tag { font-size: 22rpx; padding: 6rpx 14rpx; border-radius: 8rpx; font-weight: 600; }
.tag.PENDING { background: #fef3e2; color: #e67e22; }
.tag.PROCESSING { background: #e8f0fe; color: #1e40af; }
.tag.FINISHED { background: #e8f5e9; color: #2e7d32; }
.desc { font-size: 28rpx; color: #475569; display: block; margin-bottom: 16rpx; }
.btm { display: flex; justify-content: space-between; border-top: 2rpx solid #f8fafc; padding-top: 14rpx; }
.type { font-size: 24rpx; color: #94a3b8; }
.time { font-size: 24rpx; color: #94a3b8; }
.empty-block { text-align: center; padding: 100rpx 0; color: #94a3b8; font-size: 28rpx; }
</style>
