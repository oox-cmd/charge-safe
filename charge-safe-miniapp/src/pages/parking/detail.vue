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

    <!-- 端口色块网格 -->
    <view class="port-card">
      <text class="section-title">端口状态</text>
      <view class="port-grid">
        <view v-for="port in ports" :key="port.id" :class="['port-block', port.status]" @click="handlePort(port)">
          <text class="port-no">{{ port.portName || port.portNumber }}</text>
          <text class="port-status-text">{{ statusMap[port.status] }}</text>
          <text class="port-power">{{ port.powerRating }}W</text>
        </view>
      </view>
    </view>

    <!-- 图例 -->
    <view class="legend">
      <view class="leg-item"><view class="leg-dot idle"></view><text>空闲</text></view>
      <view class="leg-item"><view class="leg-dot reserved"></view><text>已预约</text></view>
      <view class="leg-item"><view class="leg-dot charging"></view><text>充电中</text></view>
      <view class="leg-item"><view class="leg-dot fault"></view><text>故障</text></view>
      <view class="leg-item"><view class="leg-dot maint"></view><text>维修</text></view>
    </view>

    <!-- 统计色块条 -->
    <view class="color-bar">
      <view class="bar-seg idle" :style="{ flex: idleCount }" v-if="idleCount > 0"></view>
      <view class="bar-seg reserved" :style="{ flex: reservedCount }" v-if="reservedCount > 0"></view>
      <view class="bar-seg charging" :style="{ flex: chargingCount }" v-if="chargingCount > 0"></view>
      <view class="bar-seg fault" :style="{ flex: faultCount }" v-if="faultCount > 0"></view>
    </view>

    <!-- 预约弹窗 -->
    <view class="modal-mask" v-if="showModal" @click="showModal = false">
      <view class="modal-box" @click.stop>
        <text class="modal-title">确认预约</text>
        <view class="modal-row"><text class="mlbl">端口</text><text class="mval">{{ selectedPort.portName }}</text></view>
        <view class="modal-row"><text class="mlbl">状态</text><text :class="['mval', selectedPort.status]">{{ statusMap[selectedPort.status] }}</text></view>
        <view class="modal-row" v-if="selectedPort.status === 'IDLE'"><text class="mlbl">预估费用</text><text class="mval cost">¥1.50/小时</text></view>
        <view class="modal-btns">
          <button v-if="selectedPort.status === 'IDLE'" class="btn-reserve" @click="handleReserve(selectedPort)">预约充电</button>
          <button v-if="selectedPort.status === 'FAULT'" class="btn-fault" @click="handleFault(selectedPort)">报修此端口</button>
          <button class="btn-cancel" @click="showModal = false" v-if="selectedPort.status !== 'IDLE' && selectedPort.status !== 'FAULT'">知道了</button>
          <button class="btn-cancel" @click="showModal = false" v-else>取消</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
const area = ref({}); const ports = ref([])
const showModal = ref(false); const selectedPort = ref({})

const idleCount = computed(() => ports.value.filter(p => p.status === 'IDLE').length)
const reservedCount = computed(() => ports.value.filter(p => p.status === 'RESERVED').length)
const chargingCount = computed(() => ports.value.filter(p => p.status === 'CHARGING').length)
const faultCount = computed(() => ports.value.filter(p => ['FAULT','MAINTENANCE','DISABLED','OFFLINE'].includes(p.status)).length)

const statusMap = { 'IDLE': '空闲', 'RESERVED': '已预约', 'CHARGING': '充电中', 'FAULT': '故障', 'MAINTENANCE': '维修中', 'DISABLED': '停用', 'OFFLINE': '离线' }

function handlePort(port) { selectedPort.value = port; showModal.value = true }
function handleReserve(port) { showModal.value = false; uni.navigateTo({ url: `/pages/reservation/confirm?portId=${port.id}&portName=${port.portName}` }) }
function handleFault(port) { showModal.value = false; uni.navigateTo({ url: `/pages/repair/submit?device=${port.portName}` }) }

onMounted(() => {
  area.value = { id: 1, name: 'A栋充电车棚', location: '小区东门入口处' }
  ports.value = [
    { id: 1, portName: 'A1-1号口', portNumber: '01', powerRating: 500, status: 'IDLE' },
    { id: 2, portName: 'A1-2号口', portNumber: '02', powerRating: 500, status: 'CHARGING' },
    { id: 3, portName: 'A1-3号口', portNumber: '03', powerRating: 500, status: 'IDLE' },
    { id: 4, portName: 'A1-4号口', portNumber: '04', powerRating: 500, status: 'FAULT' },
    { id: 5, portName: 'A2-1号口', portNumber: '05', powerRating: 800, status: 'RESERVED' },
    { id: 6, portName: 'A2-2号口', portNumber: '06', powerRating: 800, status: 'IDLE' },
    { id: 7, portName: 'A2-3号口', portNumber: '07', powerRating: 800, status: 'MAINTENANCE' },
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

/* 端口色块 */
.port-card { background: #fff; border-radius: 22rpx; padding: 28rpx; box-shadow: 0 2rpx 12rpx rgba(30,58,138,.04); margin-bottom: 20rpx; }
.section-title { font-size: 30rpx; font-weight: 700; color: #1e293b; margin-bottom: 20rpx; display: block; }
.port-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.port-block { width: calc(25% - 12rpx); border-radius: 18rpx; padding: 24rpx 12rpx; text-align: center; }
.port-block.IDLE { background: #f0fdf4; border: 2rpx solid #bbf7d0; }
.port-block.CHARGING { background: #fef9f0; border: 2rpx solid #fde68a; }
.port-block.FAULT, .port-block.MAINTENANCE, .port-block.DISABLED, .port-block.OFFLINE { background: #fef2f2; border: 2rpx solid #fecaca; }
.port-block.RESERVED { background: #eff6ff; border: 2rpx solid #bfdbfe; }
.port-no { font-size: 36rpx; font-weight: 800; display: block; color: #1e293b; margin-bottom: 6rpx; }
.port-block.IDLE .port-no { color: #166534; }
.port-block.CHARGING .port-no { color: #92400e; }
.port-block.RESERVED .port-no { color: #1e40af; }
.port-block.FAULT .port-no, .port-block.MAINTENANCE .port-no { color: #991b1b; }
.port-status-text { font-size: 22rpx; font-weight: 600; display: block; color: inherit; margin-bottom: 4rpx; }
.port-block.IDLE .port-status-text { color: #22c55e; }
.port-block.CHARGING .port-status-text { color: #f59e0b; }
.port-block.RESERVED .port-status-text { color: #3b82f6; }
.port-block.FAULT .port-status-text { color: #ef4444; }
.port-block.MAINTENANCE .port-status-text, .port-block.DISABLED .port-status-text, .port-block.OFFLINE .port-status-text { color: #94a3b8; }
.port-power { font-size: 20rpx; color: #94a3b8; display: block; margin-top: 4rpx; }

/* 图例 */
.legend { display: flex; justify-content: center; gap: 28rpx; padding: 10rpx 0 20rpx; }
.leg-item { display: flex; align-items: center; gap: 8rpx; font-size: 22rpx; color: #64748b; }
.leg-dot { width: 20rpx; height: 20rpx; border-radius: 6rpx; }
.leg-dot.idle { background: #22c55e; }
.leg-dot.reserved { background: #3b82f6; }
.leg-dot.charging { background: #f59e0b; }
.leg-dot.fault { background: #ef4444; }
.leg-dot.maint { background: #94a3b8; }

/* 色块条 */
.color-bar { display: flex; height: 12rpx; border-radius: 6rpx; overflow: hidden; margin: 0 4rpx 30rpx; }
.bar-seg.idle { background: #22c55e; }
.bar-seg.reserved { background: #3b82f6; }
.bar-seg.charging { background: #f59e0b; }
.bar-seg.fault { background: #ef4444; }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 999; display: flex; align-items: center; justify-content: center; padding: 60rpx; }
.modal-box { width: 100%; max-width: 560rpx; background: #fff; border-radius: 24rpx; padding: 40rpx 32rpx 24rpx; }
.modal-title { font-size: 34rpx; font-weight: 800; color: #1e3a8a; display: block; margin-bottom: 32rpx; }
.modal-row { display: flex; justify-content: space-between; padding: 18rpx 0; border-bottom: 2rpx solid #f8fafc; }
.mlbl { font-size: 28rpx; color: #64748b; }
.mval { font-size: 28rpx; font-weight: 600; color: #1e293b; }
.mval.IDLE { color: #22c55e; }
.mval.cost { color: #2563eb; }
.modal-btns { display: flex; flex-direction: column; gap: 16rpx; margin-top: 32rpx; }
.btn-reserve { width: 100%; height: 80rpx; background: linear-gradient(135deg, #1e3a8a, #1e40af); color: #fff; border: none; border-radius: 16rpx; font-size: 30rpx; font-weight: 600; }
.btn-fault { width: 100%; height: 80rpx; background: #fff; color: #ef4444; border: 2rpx solid #fecaca; border-radius: 16rpx; font-size: 30rpx; font-weight: 600; }
.btn-cancel { width: 100%; height: 80rpx; background: #f8fafc; color: #64748b; border: none; border-radius: 16rpx; font-size: 28rpx; }
</style>
