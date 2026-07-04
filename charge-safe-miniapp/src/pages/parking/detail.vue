<template>
  <view class="parking-detail-page">
    <!-- 车棚信息 -->
    <view class="area-info">
      <text class="area-name">{{ area.name }}</text>
      <text class="area-location">{{ area.location }}</text>
      <view class="area-stats">
        <view class="stat-item">
          <text class="stat-value">{{ ports.length }}</text>
          <text class="stat-label">总端口</text>
        </view>
        <view class="stat-item">
          <text class="stat-value idle">{{ idleCount }}</text>
          <text class="stat-label">空闲</text>
        </view>
        <view class="stat-item">
          <text class="stat-value charging">{{ chargingCount }}</text>
          <text class="stat-label">充电中</text>
        </view>
        <view class="stat-item">
          <text class="stat-value fault">{{ faultCount }}</text>
          <text class="stat-label">故障</text>
        </view>
      </view>
    </view>

    <!-- 端口列表 -->
    <view class="port-list">
      <view class="section-header">
        <text class="section-title">端口列表</text>
      </view>
      <view class="port-item" v-for="port in ports" :key="port.id">
        <view class="port-info">
          <text class="port-name">{{ port.portName }}</text>
          <text class="port-power">{{ port.powerRating }}W</text>
        </view>
        <view class="port-status">
          <text :class="['status-tag', port.status]">{{ getStatusText(port.status) }}</text>
        </view>
        <view class="port-action">
          <button
            v-if="port.status === 'IDLE'"
            class="btn-reserve"
            size="mini"
            @click="handleReserve(port)"
          >预约</button>
          <button
            v-else-if="port.status === 'CHARGING'"
            class="btn-charging"
            size="mini"
            disabled
          >充电中</button>
          <button
            v-else
            class="btn-disabled"
            size="mini"
            disabled
          >不可用</button>
        </view>
      </view>
      <view class="empty-tip" v-if="ports.length === 0">
        <text>暂无端口数据</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const area = ref({})
const ports = ref([])

const idleCount = computed(() => ports.value.filter(p => p.status === 'IDLE').length)
const chargingCount = computed(() => ports.value.filter(p => p.status === 'CHARGING').length)
const faultCount = computed(() => ports.value.filter(p => p.status === 'FAULT').length)

const getStatusText = (status) => {
  const map = {
    'IDLE': '空闲',
    'RESERVED': '已预约',
    'CHARGING': '充电中',
    'FAULT': '故障',
    'MAINTENANCE': '维修中',
    'DISABLED': '停用',
    'OFFLINE': '离线'
  }
  return map[status] || status
}

const handleReserve = (port) => {
  uni.navigateTo({
    url: `/pages/reservation/confirm?portId=${port.id}&portName=${port.portName}`
  })
}

onMounted(() => {
  // 模拟数据
  const id = 1
  area.value = { id: 1, name: '1号车棚', location: '小区东门' }
  ports.value = [
    { id: 1, portName: '1号端口', powerRating: 500, status: 'IDLE' },
    { id: 2, portName: '2号端口', powerRating: 500, status: 'CHARGING' },
    { id: 3, portName: '3号端口', powerRating: 500, status: 'IDLE' },
    { id: 4, portName: '4号端口', powerRating: 500, status: 'FAULT' }
  ]
})
</script>

<style scoped>
.parking-detail-page {
  padding: 20rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.area-info {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}
.area-name {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
}
.area-location {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}
.area-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 30rpx;
}
.stat-item {
  text-align: center;
}
.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  display: block;
}
.stat-value.idle { color: #67c23a; }
.stat-value.charging { color: #e6a23c; }
.stat-value.fault { color: #f56c6c; }
.stat-label {
  font-size: 22rpx;
  color: #999;
  display: block;
}
.port-list {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}
.section-header {
  margin-bottom: 20rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: bold;
}
.port-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1px solid #f0f0f0;
}
.port-info {
  flex: 1;
}
.port-name {
  font-size: 28rpx;
  display: block;
}
.port-power {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}
.port-status {
  margin: 0 20rpx;
}
.status-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.status-tag.IDLE { background: #f0f9eb; color: #67c23a; }
.status-tag.CHARGING { background: #fdf6ec; color: #e6a23c; }
.status-tag.FAULT { background: #fef0f0; color: #f56c6c; }
.status-tag.RESERVED { background: #ecf5ff; color: #409eff; }
.btn-reserve {
  background: #409eff;
  color: #fff;
  font-size: 24rpx;
}
.btn-charging {
  background: #e6a23c;
  color: #fff;
  font-size: 24rpx;
}
.btn-disabled {
  background: #dcdfe6;
  color: #999;
  font-size: 24rpx;
}
.empty-tip {
  text-align: center;
  padding: 40rpx;
  color: #999;
}
</style>
