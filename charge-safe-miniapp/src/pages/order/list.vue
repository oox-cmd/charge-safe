<template>
  <view class="page">
    <view class="tabs">
      <text v-for="t in tabs" :key="t.v" :class="['tab', { on: cur === t.v }]" @click="cur = t.v">{{ t.l }}</text>
    </view>
    <view class="card" v-for="o in filtered" :key="o.id" @click="goDetail(o)">
      <view class="top"><text class="no">{{ o.orderNo }}</text><text :class="['tag', o.status]">{{ map[o.status] || o.status }}</text></view>
      <view class="mid"><text class="mid-lbl">充电时长</text><text class="mid-val">{{ o.durationMinutes || '-' }} 分钟</text></view>
      <view class="btm"><text class="time">{{ o.startTime }}</text><text class="cost" v-if="o.amount">¥{{ o.amount }}</text></view>
    </view>
    <view class="empty-block" v-if="filtered.length === 0"><text>暂无订单记录</text></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
const cur = ref('')
const tabs = [{ l: '全部', v: '' }, { l: '充电中', v: 'CHARGING' }, { l: '已完成', v: 'COMPLETED' }, { l: '已结算', v: 'SETTLED' }]
const orders = ref([
  { id: 1, orderNo: 'ORD202607040001', status: 'CHARGING', durationMinutes: null, amount: null, startTime: '2026-07-04 08:00' },
  { id: 2, orderNo: 'ORD202607030001', status: 'COMPLETED', durationMinutes: 120, amount: '3.00', startTime: '2026-07-03 14:00' }
])
const filtered = computed(() => cur.value ? orders.value.filter(o => o.status === cur.value) : orders.value)
const map = { 'CHARGING': '充电中', 'COMPLETED': '已完成', 'SETTLED': '已结算' }
const goDetail = (o) => uni.navigateTo({ url: `/pages/order/detail?id=${o.id}` })
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; }
.tabs { display: flex; background: #fff; padding: 8rpx 20rpx; gap: 12rpx; border-bottom: 2rpx solid #f1f5f9; }
.tab { font-size: 26rpx; color: #64748b; padding: 16rpx 28rpx; border-radius: 12rpx; font-weight: 500; }
.tab.on { background: #eff6ff; color: #2563eb; font-weight: 600; }

.card { background: #fff; border-radius: 22rpx; padding: 28rpx; margin: 20rpx 28rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.no { font-size: 28rpx; font-weight: 700; color: #1e293b; }
.tag { font-size: 22rpx; padding: 6rpx 14rpx; border-radius: 8rpx; font-weight: 600; }
.tag.CHARGING { background: #fef3e2; color: #e67e22; }
.tag.COMPLETED { background: #e8f5e9; color: #2e7d32; }
.tag.SETTLED { background: #e8f0fe; color: #1e40af; }
.mid { display: flex; justify-content: space-between; padding: 16rpx 0; border-top: 2rpx solid #f8fafc; margin-bottom: 12rpx; }
.mid-lbl { font-size: 24rpx; color: #94a3b8; }
.mid-val { font-size: 24rpx; color: #475569; }
.btm { display: flex; justify-content: space-between; }
.time { font-size: 24rpx; color: #94a3b8; }
.cost { font-size: 30rpx; font-weight: 700; color: #ef4444; }
.empty-block { text-align: center; padding: 100rpx 0; color: #94a3b8; font-size: 28rpx; }
</style>
