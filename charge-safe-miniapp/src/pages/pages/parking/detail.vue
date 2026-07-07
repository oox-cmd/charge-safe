<template>
  <view class="page">
    <!-- 概览 -->
    <view class="hero-card">
      <text class="hero-name">{{ area.name }}</text>
      <text class="hero-loc">{{ area.location }}</text>
      <view class="hero-stats">
        <view class="hs-item"><text class="hs-num">{{ ports.length }}</text><text class="hs-lbl">总端口</text></view>
        <view class="hs-item accent-green"><text class="hs-num">{{ idleCount }}</text><text class="hs-lbl">空闲</text></view>
        <view class="hs-item accent-orange"><text class="hs-num">{{ chargingCount }}</text><text class="hs-lbl">充电中</text></view>
        <view class="hs-item accent-red"><text class="hs-num">{{ faultCount }}</text><text class="hs-lbl">故障</text></view>
      </view>
    </view>

    <!-- 端口列表 -->
    <view class="port-card">
      <text class="section-title">端口列表</text>
      <view class="port-item" v-for="port in ports" :key="port.id">
        <view class="port-info">
          <text class="port-name">{{ port.portName }}</text>
          <text class="port-power">{{ port.powerRating }}W</text>
        </view>
        <view :class="['port-status', port.status]"><text>{{ statusMap[port.status] || port.status }}</text></view>
        <button v-if="port.status === 'IDLE'" class="btn-reserve" size="mini" @click="handleReserve(port)">预约</button>
        <button v-else class="btn-off" size="mini" disabled>{{ port.status === 'CHARGING' ? '充电中' : '不可用' }}</button>
      </view>
      <view class="empty-block" v-if="ports.length === 0"><text>暂无端口数据</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
const area = ref({}); const ports = ref([])
const idleCount = computed(() => ports.value.filter(p => p.status === 'IDLE').length)
const chargingCount = computed(() => ports.value.filter(p => p.status === 'CHARGING').length)
const faultCount = computed(() => ports.value.filter(p => ['FAULT','MAINTENANCE','DISABLED','OFFLINE'].includes(p.status)).length)
const statusMap = { 'IDLE': '空闲', 'RESERVED': '已预约', 'CHARGING': '充电中', 'FAULT': '故障', 'MAINTENANCE': '维修中', 'DISABLED': '停用', 'OFFLINE': '离线' }
const handleReserve = (port) => uni.navigateTo({ url: `/pages/reservation/confirm?portId=${port.id}&portName=${port.portName}` })
onMounted(() => {
  area.value = { id: 1, name: 'A栋充电车棚', location: '小区东门入口处' }
  ports.value = [
    { id: 1, portName: 'A1-1号口', powerRating: 500, status: 'IDLE' },
    { id: 2, portName: 'A1-2号口', powerRating: 500, status: 'CHARGING' },
    { id: 3, portName: 'A1-3号口', powerRating: 500, status: 'IDLE' },
    { id: 4, portName: 'A1-4号口', powerRating: 500, status: 'FAULT' }
  ]
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f4f8; padding: 24rpx 28rpx; }

.hero-card { background: linear-gradient(150deg, #1e3a8a 0%, #1e40af 40%, #2563eb 100%); border-radius: 24rpx; padding: 36rpx 32rpx; margin-bottom: 24rpx; }
.hero-name { font-size: 38rpx; font-weight: 800; color: #fff; display: block; }
.hero-loc { font-size: 26rpx; color: rgba(255,255,255,.7); margin-top: 8rpx; display: block; }
.hero-stats { display: flex; justify-content: space-around; margin-top: 32rpx; padding-top: 28rpx; border-top: 2rpx solid rgba(255,255,255,.15); }
.hs-item { text-align: center; }
.hs-num { font-size: 52rpx; font-weight: 800; color: #fff; display: block; line-height: 1; }
.hs-lbl { font-size: 24rpx; color: rgba(255,255,255,.7); margin-top: 8rpx; display: block; }
.accent-green .hs-num { color: #4ade80; }
.accent-orange .hs-num { color: #fbbf24; }
.accent-red .hs-num { color: #f87171; }

.port-card { background: #fff; border-radius: 22rpx; padding: 28rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); }
.section-title { font-size: 30rpx; font-weight: 700; color: #1e293b; margin-bottom: 20rpx; display: block; }
.port-item { display: flex; align-items: center; padding: 22rpx 0; border-bottom: 2rpx solid #f8fafc; }
.port-item:last-child { border-bottom: none; }
.port-info { flex: 1; }
.port-name { font-size: 28rpx; font-weight: 600; color: #1e293b; display: block; }
.port-power { font-size: 24rpx; color: #94a3b8; margin-top: 4rpx; display: block; }
.port-status { padding: 8rpx 18rpx; border-radius: 10rpx; margin: 0 16rpx; font-size: 24rpx; font-weight: 600; }
.port-status.IDLE { background: #e8f5e9; color: #2e7d32; }
.port-status.CHARGING { background: #fef3e2; color: #e67e22; }
.port-status.FAULT, .port-status.MAINTENANCE, .port-status.DISABLED, .port-status.OFFLINE { background: #fce4ec; color: #c62828; }
.port-status.RESERVED { background: #e8f0fe; color: #1e40af; }
.btn-reserve { background: #2563eb; color: #fff; font-size: 24rpx; border-radius: 12rpx; border: none; padding: 8rpx 24rpx; }
.btn-off { background: #f1f5f9; color: #94a3b8; font-size: 24rpx; border-radius: 12rpx; border: none; padding: 8rpx 24rpx; }
.empty-block { text-align: center; padding: 60rpx 0; color: #94a3b8; font-size: 26rpx; }
</style>
