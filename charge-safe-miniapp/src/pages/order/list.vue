<template>
  <view class="page">
    <view class="tabs">
      <text v-for="t in tabs" :key="t.v" :class="['tab', { on: cur === t.v }]" @click="cur = t.v">{{ t.l }}</text>
    </view>
    <view class="card" v-for="o in filtered" :key="o.id" @click="goDetail(o)">
      <view class="top">
        <text class="no">{{ o.orderNo }}</text>
        <text :class="['tag', o.status]" :style="o.status === 'CHARGING' ? 'animation: pulse 1.5s ease-in-out infinite;' : ''">{{ map[o.status] || o.status }}</text>
      </view>
      <view class="mid">
        <text class="place">{{ o.parkingName || 'A栋充电车棚' }} · {{ o.portName || '1号端口' }}</text>
      </view>
      <view class="btm">
        <text class="time">{{ o.startTime }}</text>
        <text class="cost" v-if="o.amount">¥{{ o.amount }}</text>
        <text class="cost" v-else-if="o.status === 'CHARGING'">计费中</text>
      </view>
    </view>
    <view class="empty-block" v-if="filtered.length === 0"><text>暂无订单记录</text></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
const cur = ref('')
const tabs = [{ l: '全部', v: '' }, { l: '充电中', v: 'CHARGING' }, { l: '已完成', v: 'COMPLETED' }, { l: '已取消', v: 'CANCELLED' }, { l: '已结算', v: 'SETTLED' }]
const orders = ref([
  { id: 1, orderNo: 'ORD202607040001', status: 'CHARGING', parkingName: 'A栋充电车棚', portName: 'A1-1号口', durationMinutes: null, amount: null, startTime: '2026-07-04 08:00' },
  { id: 2, orderNo: 'ORD202607030001', status: 'COMPLETED', parkingName: 'A栋充电车棚', portName: 'A1-2号口', durationMinutes: 120, amount: '3.00', startTime: '2026-07-03 14:00' },
  { id: 3, orderNo: 'ORD202607020001', status: 'CANCELLED', parkingName: 'B栋充电车棚', portName: 'B1-1号口', durationMinutes: 0, amount: null, startTime: '2026-07-02 10:00' }
])
const filtered = computed(() => cur.value ? orders.value.filter(o => o.status === cur.value) : orders.value)
const map = { 'CHARGING': '充电中', 'COMPLETED': '已完成', 'SETTLED': '已结算', 'CANCELLED': '已取消' }
const goDetail = (o) => uni.navigateTo({ url: `/pages/order/detail?id=${o.id}` })
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; }
.tabs { display: flex; background: #fff; padding: 8rpx 20rpx; gap: 12rpx; border-bottom: 2rpx solid #f1f5f9; overflow-x: auto; white-space: nowrap; }
.tab { font-size: 26rpx; color: #64748b; padding: 16rpx 28rpx; border-radius: 12rpx; font-weight: 500; flex-shrink: 0; }
.tab.on { background: #eff6ff; color: #2563eb; font-weight: 600; }

.card { background: #fff; border-radius: 22rpx; padding: 28rpx; margin: 20rpx 28rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.no { font-size: 28rpx; font-weight: 700; color: #1e293b; }
.tag { font-size: 22rpx; padding: 6rpx 14rpx; border-radius: 8rpx; font-weight: 600; }
.tag.CHARGING { background: #fef3e2; color: #e67e22; }
.tag.COMPLETED { background: #e8f5e9; color: #2e7d32; }
.tag.CANCELLED { background: #f1f5f9; color: #94a3b8; }
.tag.SETTLED { background: #e8f0fe; color: #1e40af; }
.mid { margin-bottom: 14rpx; }
.place { font-size: 26rpx; color: #64748b; }
.btm { display: flex; justify-content: space-between; border-top: 2rpx solid #f8fafc; padding-top: 14rpx; }
.time { font-size: 24rpx; color: #94a3b8; }
.cost { font-size: 30rpx; font-weight: 700; color: #ef4444; }
.empty-block { text-align: center; padding: 100rpx 0; color: #94a3b8; font-size: 28rpx; }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
</style>
