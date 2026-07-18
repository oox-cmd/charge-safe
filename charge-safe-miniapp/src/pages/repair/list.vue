<template>
  <view class="page">
    <view class="card" v-for="item in repairs" :key="item.id">
      <view class="top">
        <text class="no">报修单 #{{ item.id }}</text>
        <text :class="['tag', item.status]">{{ statusText(item.status) }}</text>
      </view>
      <text class="device">{{ item.deviceName || '充电设备' }}</text>
      <text class="desc">{{ item.faultDesc }}</text>
      <view class="btm"><text class="type">{{ item.faultType }}</text><text class="time">{{ item.createdTime }}</text></view>
    </view>
    <view class="empty-block" v-if="!loading && repairs.length === 0"><text>暂无报修记录</text></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { pageMyRepairReports } from '@/api/repair'

const repairs = ref([])
const loading = ref(false)
const statusMap = {
  PENDING: '待受理',
  CONFIRMED: '已确认',
  DISPATCHED: '已派单',
  PROCESSING: '处理中',
  WAITING_ACCEPTANCE: '待验收',
  FINISHED: '已完成',
  REJECTED: '已驳回'
}

const statusText = (status) => statusMap[status] || status || '未知状态'

async function fetchReports() {
  loading.value = true
  try {
    const res = await pageMyRepairReports({ page: 1, size: 50 })
    if (res.code === 200 && Array.isArray(res.data?.records)) {
      repairs.value = res.data.records
    }
  } catch {
    // request.js 已统一展示错误提示
  } finally {
    loading.value = false
  }
}

onShow(fetchReports)
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 24rpx 28rpx; }
.card { background: #fff; border-radius: 22rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.no { font-size: 28rpx; font-weight: 700; color: #1e293b; }
.tag { font-size: 22rpx; padding: 6rpx 14rpx; border-radius: 8rpx; font-weight: 600; }
.tag.PENDING { background: #fef3e2; color: #e67e22; }
.tag.CONFIRMED, .tag.DISPATCHED, .tag.PROCESSING, .tag.WAITING_ACCEPTANCE { background: #e8f0fe; color: #1e40af; }
.tag.FINISHED { background: #ecfdf5; color: #047857; }
.tag.REJECTED { background: #fef2f2; color: #b91c1c; }
.device { display: block; font-size: 26rpx; color: #64748b; margin-bottom: 10rpx; }
.desc { font-size: 28rpx; color: #475569; display: block; margin-bottom: 16rpx; }
.btm { display: flex; justify-content: space-between; border-top: 2rpx solid #f8fafc; padding-top: 14rpx; }
.type, .time { font-size: 24rpx; color: #94a3b8; }
.empty-block { text-align: center; padding: 100rpx 0; color: #94a3b8; font-size: 28rpx; }
</style>
