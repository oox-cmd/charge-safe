<template>
  <view class="page">
    <view class="card" v-for="item in reservations" :key="item.id">
      <view class="top"><text class="title">{{ item.portName }}</text><text :class="['tag', item.status]">{{ statusMap[item.status] || item.status }}</text></view>
      <view class="mid">
        <view class="row"><text class="lbl">预约时间</text><text class="val">{{ formatTime(item.reserveTime) }}</text></view>
        <view class="row"><text class="lbl">过期时间</text><text class="val">{{ formatTime(item.expireTime) }}</text></view>
      </view>
      <view class="btm" v-if="item.status === 'PENDING'">
        <button class="btn-start" size="mini" @click="handleStart(item)">开始充电</button>
        <button class="btn-cancel" size="mini" @click="handleCancel(item)">取消预约</button>
      </view>
    </view>
    <view class="empty-block" v-if="reservations.length === 0"><text>暂无预约记录</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pageMyReservations, cancelReservation, startCharging } from '@/api/charging'

const reservations = ref([])
const statusMap = { 'PENDING': '待生效', 'CONVERTED': '已转订单', 'CANCELLED': '已取消', 'EXPIRED': '已过期' }

function formatTime(iso) {
  if (!iso) return '--'
  try { return iso.replace('T', ' ').substring(0, 16) } catch { return iso }
}

async function handleCancel(item) {
  uni.showModal({
    title: '确认取消', content: '确定要取消该预约吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const r = await cancelReservation(item.id)
          if (r.code === 200) {
            uni.showToast({ title: '已取消', icon: 'success' })
            item.status = 'CANCELLED'
          }
        } catch { /* 错误由 request.js 统一 toast */ }
      }
    }
  })
}

async function handleStart(item) {
  try {
    const res = await startCharging(item.id)
    if (res.code === 200 && res.data?.id) {
      uni.redirectTo({ url: `/pages/charging/charging?orderId=${res.data.id}&orderNo=${res.data.orderNo || ''}` })
    }
  } catch { /* 错误由 request.js 统一 toast */ }
}

onMounted(async () => {
  try {
    const res = await pageMyReservations()
    if (res.code === 200 && res.data?.records) {
      reservations.value = res.data.records
    }
  } catch { /* 错误由 request.js 统一 toast */ }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 24rpx 28rpx; }
.card { background: #fff; border-radius: 22rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18rpx; }
.title { font-size: 30rpx; font-weight: 700; color: #1e293b; }
.tag { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 8rpx; font-weight: 600; }
.tag.PENDING { background: #fef3e2; color: #e67e22; }
.tag.CONVERTED { background: #e8f5e9; color: #2e7d32; }
.tag.CANCELLED { background: #f1f5f9; color: #94a3b8; }
.tag.EXPIRED { background: #f1f5f9; color: #94a3b8; }
.mid { border-top: 2rpx solid #f8fafc; padding-top: 18rpx; }
.row { display: flex; justify-content: space-between; margin-bottom: 10rpx; }
.lbl { font-size: 26rpx; color: #94a3b8; }
.val { font-size: 26rpx; color: #475569; font-weight: 500; }
.btm { border-top: 2rpx solid #f8fafc; padding-top: 18rpx; margin-top: 8rpx; display: flex; gap: 16rpx; justify-content: flex-end; }
.btn-start { background: #2563eb; color: #fff; border: none; font-size: 24rpx; border-radius: 10rpx; padding: 10rpx 24rpx; }
.btn-cancel { background: #fff; color: #ef4444; border: 2rpx solid #fecaca; font-size: 24rpx; border-radius: 10rpx; padding: 10rpx 24rpx; }
.empty-block { text-align: center; padding: 100rpx 0; color: #94a3b8; font-size: 28rpx; }
</style>
