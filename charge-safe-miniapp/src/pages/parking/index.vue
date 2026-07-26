<template>
  <view class="page">
    <view class="card" v-for="item in parkingAreas" :key="item.id" @click="goDetail(item)">
      <view class="card-left">
        <text class="card-name">{{ item.name }}</text>
        <text class="card-loc">{{ item.location }}</text>
      </view>
      <view class="card-right">
        <view class="stat-block"><text class="stat-num green">{{ item.idlePorts }}</text><text class="stat-label">空闲</text></view>
        <view class="stat-divider"></view>
        <view class="stat-block"><text class="stat-num">{{ item.totalPorts }}</text><text class="stat-label">端口</text></view>
      </view>
    </view>
    <view class="empty-block" v-if="parkingAreas.length === 0"><text>暂无可用车棚</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getResidentDashboard } from '@/api/parking'

const parkingAreas = ref([])

onMounted(async () => {
  try {
    const res = await getResidentDashboard()
    if (res.code === 200 && res.data?.parkingAreas) {
      parkingAreas.value = res.data.parkingAreas.map(a => ({
        id: a.parkingAreaId,
        name: a.parkingAreaName,
        location: a.locationDescription || '',
        idlePorts: a.idleCount ?? 0,
        totalPorts: a.totalPorts ?? 0
      }))
    }
  } catch { /* 错误由 request.js 统一 toast */ }
})

const goDetail = (item) => uni.navigateTo({
  url: `/pages/parking/detail?id=${item.id}&name=${encodeURIComponent(item.name)}&location=${encodeURIComponent(item.location)}`
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 24rpx 28rpx; }
.card { display: flex; justify-content: space-between; align-items: center; background: #fff; border-radius: 22rpx; padding: 32rpx 28rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.card-left { flex: 1; }
.card-name { font-size: 32rpx; font-weight: 700; color: #1e293b; display: block; }
.card-loc { font-size: 26rpx; color: #94a3b8; margin-top: 8rpx; display: block; }
.card-right { display: flex; align-items: center; flex-shrink: 0; }
.stat-block { text-align: center; padding: 0 24rpx; }
.stat-num { font-size: 48rpx; font-weight: 800; color: #1e293b; display: block; line-height: 1; }
.stat-num.green { color: #2e7d32; }
.stat-label { font-size: 22rpx; color: #94a3b8; margin-top: 4rpx; display: block; }
.stat-divider { width: 2rpx; height: 50rpx; background: #f1f5f9; }
.empty-block { text-align: center; padding: 100rpx 0; color: #94a3b8; font-size: 28rpx; }
</style>
