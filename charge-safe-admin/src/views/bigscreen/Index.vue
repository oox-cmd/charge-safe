<template>
  <div class="bigscreen" ref="screenRef">
    <!-- 顶部标题区 -->
    <header class="bigscreen-header">
      <div class="header-left">
        <span class="system-name">社区充电安全态势大屏</span>
      </div>
      <div class="header-center">
        <span class="community-name">{{ currentCommunity }}</span>
      </div>
      <div class="header-right">
        <span class="current-time">{{ currentTime }}</span>
        <span class="role-info">{{ roleInfo }}</span>
      </div>
    </header>

    <!-- 主体区域 -->
    <div class="bigscreen-body">
      <!-- 左侧统计区 -->
      <aside class="bigscreen-left">
        <!-- 端口状态环形图 -->
        <div class="panel">
          <div class="panel-title">端口状态分布</div>
          <div ref="portStatusChartRef" class="chart-container"></div>
        </div>
        <!-- 车棚利用率 -->
        <div class="panel">
          <div class="panel-title">车棚利用率</div>
          <div ref="parkingUsageChartRef" class="chart-container"></div>
        </div>
        <!-- 核心指标卡片 -->
        <div class="panel">
          <div class="panel-title">核心指标</div>
          <div class="metric-grid">
            <div class="metric-item" v-for="metric in coreMetrics" :key="metric.label">
              <div class="metric-value" :style="{ color: metric.color }">{{ metric.value }}</div>
              <div class="metric-label">{{ metric.label }}</div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 中间主视觉区 -->
      <main class="bigscreen-center">
        <div class="panel">
          <div class="panel-title">端口状态矩阵</div>
          <div class="port-matrix">
            <div
              v-for="area in parkingAreas"
              :key="area.id"
              class="parking-area"
            >
              <div class="area-name">{{ area.name }}</div>
              <div class="port-grid">
                <div
                  v-for="port in area.ports"
                  :key="port.id"
                  class="port-block"
                  :style="{ backgroundColor: getPortColor(port.status) }"
                  :title="`${port.portName} - ${getPortStatusName(port.status)}`"
                >
                  <span class="port-number">{{ port.portNumber }}</span>
                </div>
              </div>
            </div>
            <div v-if="parkingAreas.length === 0" class="empty-tip">
              暂无端口数据
            </div>
          </div>
        </div>
      </main>

      <!-- 右侧告警/工单/隐患列表 -->
      <aside class="bigscreen-right">
        <!-- 实时告警列表 -->
        <div class="panel">
          <div class="panel-title">
            <span>实时告警</span>
            <span class="badge danger">{{ alerts.length }}</span>
          </div>
          <div class="scroll-list alert-list">
            <div
              v-for="alert in alerts"
              :key="alert.id"
              class="scroll-item"
              :class="{ 'item-danger': alert.severity === 'URGENT' || alert.severity === 'SERIOUS' }"
            >
              <div class="item-title">{{ alert.description }}</div>
              <div class="item-time">{{ alert.createdTime }}</div>
            </div>
            <div v-if="alerts.length === 0" class="empty-tip">暂无告警</div>
          </div>
        </div>

        <!-- 待处理工单 -->
        <div class="panel">
          <div class="panel-title">
            <span>待处理工单</span>
            <span class="badge warning">{{ pendingTickets.length }}</span>
          </div>
          <div class="scroll-list ticket-list">
            <div
              v-for="ticket in pendingTickets"
              :key="ticket.id"
              class="scroll-item"
            >
              <div class="item-title">{{ ticket.faultDesc }}</div>
              <div class="item-time">{{ ticket.createdTime }}</div>
            </div>
            <div v-if="pendingTickets.length === 0" class="empty-tip">暂无待处理工单</div>
          </div>
        </div>

        <!-- 待整改隐患 -->
        <div class="panel">
          <div class="panel-title">
            <span>待整改隐患</span>
            <span class="badge warning">{{ pendingHazards.length }}</span>
          </div>
          <div class="scroll-list hazard-list">
            <div
              v-for="hazard in pendingHazards"
              :key="hazard.id"
              class="scroll-item"
              :class="{ 'item-danger': hazard.severity === 'URGENT' || hazard.severity === 'SERIOUS' }"
            >
              <div class="item-title">{{ hazard.description }}</div>
              <div class="item-time">{{ hazard.createdTime }}</div>
            </div>
            <div v-if="pendingHazards.length === 0" class="empty-tip">暂无待整改隐患</div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 底部趋势图区 -->
    <footer class="bigscreen-footer">
      <div class="panel">
        <div class="panel-title">订单趋势</div>
        <div ref="orderTrendChartRef" class="chart-container"></div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import {
  getDashboard, getPortStatus, getOrderTrend,
  getParkingAreaUsage
} from '@/api/statistics'

const userStore = useUserStore()

// 图表引用
const portStatusChartRef = ref(null)
const parkingUsageChartRef = ref(null)
const orderTrendChartRef = ref(null)

// 图表实例
let portStatusChart = null
let parkingUsageChart = null
let orderTrendChart = null

// 大屏数据
const currentTime = ref('')
const currentCommunity = ref('全部社区')
const roleInfo = ref(userStore.roleName || '')
const dashboardData = ref({})
const parkingAreas = ref([])
const alerts = ref([])
const pendingTickets = ref([])
const pendingHazards = ref([])

// 核心指标
const coreMetrics = ref([
  { label: '端口总数', value: 0, color: '#409eff' },
  { label: '空闲端口', value: 0, color: '#67c23a' },
  { label: '充电中', value: 0, color: '#e6a23c' },
  { label: '故障', value: 0, color: '#f56c6c' },
  { label: '今日订单', value: 0, color: '#409eff' },
  { label: '今日预约', value: 0, color: '#67c23a' }
])

// 端口颜色映射
const portColorMap = {
  IDLE: '#22c55e',
  RESERVED: '#3b82f6',
  CHARGING: '#f59e0b',
  FAULT: '#ef4444',
  MAINTENANCE: '#94a3b8',
  DISABLED: '#c0c4cc',
  OFFLINE: '#c0c4cc'
}

// 端口状态名称
const portStatusNameMap = {
  IDLE: '空闲',
  RESERVED: '已预约',
  CHARGING: '充电中',
  FAULT: '故障',
  MAINTENANCE: '维修中',
  DISABLED: '停用',
  OFFLINE: '离线'
}

const getPortColor = (status) => portColorMap[status] || '#c0c4cc'
const getPortStatusName = (status) => portStatusNameMap[status] || '未知'

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取仪表盘数据
const fetchDashboard = async () => {
  try {
    const res = await getDashboard()
    if (res.code === 200) {
      dashboardData.value = res.data
      coreMetrics.value = [
        { label: '端口总数', value: res.data.totalPorts || 0, color: '#409eff' },
        { label: '空闲端口', value: res.data.idlePorts || 0, color: '#67c23a' },
        { label: '充电中', value: res.data.chargingPorts || 0, color: '#e6a23c' },
        { label: '故障', value: res.data.faultPorts || 0, color: '#f56c6c' },
        { label: '今日订单', value: res.data.todayOrders || 0, color: '#409eff' },
        { label: '今日预约', value: res.data.todayReservations || 0, color: '#67c23a' }
      ]
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

// 获取端口状态（用于矩阵）
const fetchPortStatusMatrix = async () => {
  try {
    const res = await getParkingAreaUsage()
    if (res.code === 200 && res.data?.areas) {
      // TODO: 需要后端提供更详细的端口列表数据
      // 目前使用模拟数据展示矩阵
      parkingAreas.value = res.data.areas.map(area => ({
        id: area.id,
        name: area.name,
        ports: generateMockPorts(area.totalPorts, area)
      }))
    }
  } catch (error) {
    console.error('获取端口矩阵失败:', error)
  }
}

// 生成模拟端口数据（实际应从后端获取）
const generateMockPorts = (total, area) => {
  const ports = []
  for (let i = 1; i <= Math.min(total || 0, 20); i++) {
    let status = 'IDLE'
    // 根据统计数据分配状态
    if (i <= (area.chargingPorts || 0)) {
      status = 'CHARGING'
    } else if (i <= (area.chargingPorts || 0) + (area.faultPorts || 0)) {
      status = 'FAULT'
    }
    ports.push({
      id: i,
      portNumber: i,
      portName: `${i}号端口`,
      status
    })
  }
  return ports
}

// 初始化端口状态环形图
const initPortStatusChart = async () => {
  try {
    const res = await getPortStatus()
    if (res.code === 200 && res.data?.statusList) {
      const chartData = res.data.statusList
        .filter(item => item.count > 0)
        .map(item => ({
          name: item.statusName,
          value: item.count
        }))

      portStatusChart = echarts.init(portStatusChartRef.value)
      portStatusChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { show: false },
        series: [{
          type: 'pie',
          radius: ['45%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 5, borderColor: '#0a1a3a', borderWidth: 2 },
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
          data: chartData
        }]
      })
    }
  } catch (error) {
    console.error('获取端口状态失败:', error)
  }
}

// 初始化车棚利用率柱状图
const initParkingUsageChart = async () => {
  try {
    const res = await getParkingAreaUsage()
    if (res.code === 200 && res.data?.areas) {
      const areas = res.data.areas
      parkingUsageChart = echarts.init(parkingUsageChartRef.value)
      parkingUsageChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: areas.map(a => a.name),
          axisLabel: { color: '#a0aec0', fontSize: 10 }
        },
        yAxis: {
          type: 'value',
          max: 100,
          axisLabel: { color: '#a0aec0', formatter: '{value}%' }
        },
        series: [{
          type: 'bar',
          data: areas.map(a => a.usageRate),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: '#2563eb' }
            ])
          }
        }]
      })
    }
  } catch (error) {
    console.error('获取车棚利用率失败:', error)
  }
}

// 初始化订单趋势折线图
const initOrderTrendChart = async () => {
  try {
    const res = await getOrderTrend()
    if (res.code === 200 && res.data) {
      orderTrendChart = echarts.init(orderTrendChartRef.value)
      orderTrendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['订单数', '完成数'], textStyle: { color: '#a0aec0' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: res.data.dates,
          axisLabel: { color: '#a0aec0' }
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: '#a0aec0' }
        },
        series: [
          {
            name: '订单数',
            type: 'line',
            smooth: true,
            data: res.data.orderCounts,
            lineStyle: { color: '#409eff' },
            itemStyle: { color: '#409eff' }
          },
          {
            name: '完成数',
            type: 'line',
            smooth: true,
            data: res.data.completedCounts,
            lineStyle: { color: '#67c23a' },
            itemStyle: { color: '#67c23a' }
          }
        ]
      })
    }
  } catch (error) {
    console.error('获取订单趋势失败:', error)
  }
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  portStatusChart?.resize()
  parkingUsageChart?.resize()
  orderTrendChart?.resize()
}

// 定时刷新数据
let refreshTimer = null

onMounted(async () => {
  updateTime()
  setInterval(updateTime, 1000)

  await fetchDashboard()
  await Promise.all([
    fetchPortStatusMatrix(),
    initPortStatusChart(),
    initParkingUsageChart(),
    initOrderTrendChart()
  ])

  window.addEventListener('resize', handleResize)

  // 每60秒刷新一次数据
  refreshTimer = setInterval(() => {
    fetchDashboard()
    fetchPortStatusMatrix()
  }, 60000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  portStatusChart?.dispose()
  parkingUsageChart?.dispose()
  orderTrendChart?.dispose()
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.bigscreen {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #0a1a3a 0%, #0d2137 50%, #0a1a3a 100%);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部标题区 */
.bigscreen-header {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.1) 0%, transparent 100%);
  border-bottom: 1px solid rgba(64, 158, 255, 0.3);
}

.system-name {
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(90deg, #409eff, #67c23a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 4px;
}

.community-name {
  font-size: 18px;
  color: #a0aec0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.current-time {
  font-size: 16px;
  color: #a0aec0;
}

.role-info {
  font-size: 14px;
  color: #409eff;
  padding: 4px 12px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 4px;
}

/* 主体区域 */
.bigscreen-body {
  flex: 1;
  display: flex;
  padding: 16px;
  gap: 16px;
  overflow: hidden;
}

/* 左侧 */
.bigscreen-left {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 中间 */
.bigscreen-center {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 右侧 */
.bigscreen-right {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 面板 */
.panel {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.panel-title {
  font-size: 14px;
  font-weight: bold;
  color: #a0aec0;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  flex: 1;
  min-height: 150px;
}

/* 核心指标 */
.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.metric-item {
  text-align: center;
  padding: 8px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 4px;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
}

.metric-label {
  font-size: 12px;
  color: #718096;
  margin-top: 4px;
}

/* 端口矩阵 */
.port-matrix {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.parking-area {
  margin-bottom: 16px;
}

.area-name {
  font-size: 12px;
  color: #718096;
  margin-bottom: 8px;
}

.port-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
  gap: 8px;
}

.port-block {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: white;
  cursor: pointer;
  transition: transform 0.2s;
}

.port-block:hover {
  transform: scale(1.1);
}

.port-number {
  font-weight: bold;
}

/* 滚动列表 */
.scroll-list {
  flex: 1;
  overflow-y: auto;
  max-height: 200px;
}

.scroll-item {
  padding: 8px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.scroll-item.item-danger {
  border-left-color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.item-title {
  font-size: 13px;
  color: #e2e8f0;
  margin-bottom: 4px;
}

.item-time {
  font-size: 11px;
  color: #718096;
}

.badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  color: white;
}

.badge.danger {
  background: #f56c6c;
}

.badge.warning {
  background: #e6a23c;
}

.empty-tip {
  text-align: center;
  color: #718096;
  padding: 20px;
  font-size: 13px;
}

/* 底部趋势图 */
.bigscreen-footer {
  height: 200px;
  padding: 0 16px 16px;
}

.bigscreen-footer .panel {
  height: 100%;
}

.bigscreen-footer .chart-container {
  min-height: 120px;
}
</style>
