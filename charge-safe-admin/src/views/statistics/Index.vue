<template>
  <div class="statistics-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="card in statCards.slice(0, 4)" :key="card.label">
        <el-card class="saas-stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">{{ card.label }}</div>
              <div class="stat-value">{{ card.value }}</div>
            </div>
            <div class="stat-icon-wrapper" :style="{ background: card.bgColor }">
              <el-icon :size="24" :style="{ color: card.color }">
                <component :is="card.icon" />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="stat-cards" style="margin-top: 20px;">
      <el-col :span="6" v-for="card in statCards.slice(4, 8)" :key="card.label">
        <el-card class="saas-stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">{{ card.label }}</div>
              <div class="stat-value">{{ card.value }}</div>
            </div>
            <div class="stat-icon-wrapper" :style="{ background: card.bgColor }">
              <el-icon :size="24" :style="{ color: card.color }">
                <component :is="card.icon" />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 端口状态环形图 -->
      <el-col :span="8">
        <el-card class="saas-chart-card" shadow="never">
          <template #header><div class="card-title">端口状态分布</div></template>
          <div v-if="dashboardData.totalPorts > 0" ref="portStatusChartRef" style="height: 350px"></div>
          <el-empty v-else description="暂无端口数据" :image-size="120" style="height: 350px" />
        </el-card>
      </el-col>

      <!-- 订单趋势折线图 -->
      <el-col :span="16">
        <el-card class="saas-chart-card" shadow="never">
          <template #header><div class="card-title">订单趋势（近7天）</div></template>
          <div v-if="hasOrderData" ref="orderTrendChartRef" style="height: 350px"></div>
          <el-empty v-else description="暂无订单趋势数据" :image-size="120" style="height: 350px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px; padding-bottom: 20px;">
      <!-- 车棚利用率柱状图 -->
      <el-col :span="10">
        <el-card class="saas-chart-card" shadow="never">
          <template #header><div class="card-title">车棚利用率</div></template>
          <div v-if="hasParkingData" ref="parkingUsageChartRef" style="height: 350px"></div>
          <el-empty v-else description="暂无车棚数据" :image-size="120" style="height: 350px" />
        </el-card>
      </el-col>

      <!-- 工单状态柱状图 -->
      <el-col :span="7">
        <el-card class="saas-chart-card" shadow="never">
          <template #header><div class="card-title">工单状态</div></template>
          <div v-if="hasRepairData" ref="repairChartRef" style="height: 350px"></div>
          <el-empty v-else description="暂无工单记录" :image-size="120" style="height: 350px" />
        </el-card>
      </el-col>

      <!-- 隐患状态饼图 -->
      <el-col :span="7">
        <el-card class="saas-chart-card" shadow="never">
          <template #header><div class="card-title">安全隐患状态</div></template>
          <div v-if="hasHazardData" ref="hazardChartRef" style="height: 350px"></div>
          <el-empty v-else description="暂无隐患记录" :image-size="120" style="height: 350px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getDashboard, getPortStatus, getOrderTrend,
  getRepairSummary, getHazardSummary, getParkingAreaUsage
} from '@/api/statistics'

// 图表引用
const portStatusChartRef = ref(null)
const orderTrendChartRef = ref(null)
const parkingUsageChartRef = ref(null)
const repairChartRef = ref(null)
const hazardChartRef = ref(null)

// 图表实例
let portStatusChart = null
let orderTrendChart = null
let parkingUsageChart = null
let repairChart = null
let hazardChart = null

// 数据状态
const dashboardData = ref({})
const hasOrderData = ref(false)
const hasParkingData = ref(false)
const hasRepairData = ref(false)
const hasHazardData = ref(false)

// 现代调色盘
const colorPalette = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#64748b']

// 顶部卡片数据
const statCards = computed(() => [
  { label: '端口总数', value: dashboardData.value.totalPorts || 0, icon: 'Connection', color: '#3b82f6', bgColor: 'rgba(59, 130, 246, 0.1)' },
  { label: '空闲可用', value: dashboardData.value.idlePorts || 0, icon: 'CircleCheck', color: '#10b981', bgColor: 'rgba(16, 185, 129, 0.1)' },
  { label: '正在充电', value: dashboardData.value.chargingPorts || 0, icon: 'Lightning', color: '#f59e0b', bgColor: 'rgba(245, 158, 11, 0.1)' },
  { label: '设备故障', value: dashboardData.value.faultPorts || 0, icon: 'Warning', color: '#ef4444', bgColor: 'rgba(239, 68, 68, 0.1)' },
  { label: '今日预约', value: dashboardData.value.todayReservations || 0, icon: 'Clock', color: '#8b5cf6', bgColor: 'rgba(139, 92, 246, 0.1)' },
  { label: '今日订单', value: dashboardData.value.todayOrders || 0, icon: 'Document', color: '#06b6d4', bgColor: 'rgba(6, 182, 212, 0.1)' },
  { label: '待处理工单', value: dashboardData.value.pendingTickets || 0, icon: 'Tickets', color: '#f97316', bgColor: 'rgba(249, 115, 22, 0.1)' },
  { label: '待整改隐患', value: dashboardData.value.pendingHazards || 0, icon: 'WarnTriangleFilled', color: '#ef4444', bgColor: 'rgba(239, 68, 68, 0.1)' }
])

const fetchDashboard = async () => {
  try {
    const res = await getDashboard()
    if (res.code === 200) dashboardData.value = res.data
  } catch (error) {}
}

const initPortStatusChart = async () => {
  try {
    const res = await getPortStatus()
    if (res.code === 200 && res.data?.statusList) {
      const chartData = res.data.statusList.filter(item => item.count > 0).map(item => ({ name: item.statusName, value: item.count }))
      if (chartData.length === 0) return // Fallback to v-if if dashboard total is also 0
      
      await nextTick()
      if (!portStatusChartRef.value) return
      portStatusChart = echarts.init(portStatusChartRef.value)
      portStatusChart.setOption({
        color: colorPalette,
        tooltip: { trigger: 'item', backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#e2e8f0' },
        legend: { bottom: '5%', icon: 'circle', textStyle: { color: '#64748b' } },
        series: [{
          type: 'pie', radius: ['50%', '75%'], center: ['50%', '42%'],
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          data: chartData
        }]
      })
    }
  } catch (error) {}
}

const initOrderTrendChart = async () => {
  try {
    const res = await getOrderTrend()
    if (res.code === 200 && res.data && res.data.orderCounts.length > 0 && res.data.orderCounts.some(v => v > 0)) {
      hasOrderData.value = true
      await nextTick()
      orderTrendChart = echarts.init(orderTrendChartRef.value)
      orderTrendChart.setOption({
        color: ['#3b82f6', '#10b981', '#f59e0b'],
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#e2e8f0' },
        legend: { top: '0%', textStyle: { color: '#64748b' } },
        grid: { left: '3%', right: '4%', bottom: '5%', top: '15%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: res.data.dates, axisLine: { lineStyle: { color: '#cbd5e1' } }, axisLabel: { color: '#64748b' } },
        yAxis: [
          { type: 'value', name: '数量', splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }, axisLabel: { color: '#64748b' } },
          { type: 'value', name: '金额(元)', position: 'right', splitLine: { show: false }, axisLabel: { color: '#64748b' } }
        ],
        series: [
          { name: '订单数', type: 'line', smooth: true, showSymbol: false,
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: 'rgba(59, 130, 246, 0.4)'}, {offset: 1, color: 'rgba(59, 130, 246, 0.05)'}]) },
            lineStyle: { width: 3 }, data: res.data.orderCounts
          },
          { name: '完成数', type: 'line', smooth: true, showSymbol: false,
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: 'rgba(16, 185, 129, 0.4)'}, {offset: 1, color: 'rgba(16, 185, 129, 0.05)'}]) },
            lineStyle: { width: 3 }, data: res.data.completedCounts
          },
          { name: '金额(元)', type: 'line', smooth: true, yAxisIndex: 1, showSymbol: false, lineStyle: { width: 3, type: 'dashed' }, data: res.data.amounts }
        ]
      })
    } else {
      hasOrderData.value = false
    }
  } catch (error) {}
}

const initParkingUsageChart = async () => {
  try {
    const res = await getParkingAreaUsage()
    if (res.code === 200 && res.data?.areas && res.data.areas.length > 0) {
      hasParkingData.value = true
      await nextTick()
      const areas = res.data.areas
      parkingUsageChart = echarts.init(parkingUsageChartRef.value)
      parkingUsageChart.setOption({
        color: ['#e2e8f0', '#3b82f6', '#ef4444'], // 空闲用灰色，充电用蓝，故障用红
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#e2e8f0', axisPointer: { type: 'shadow' } },
        legend: { top: '0%', textStyle: { color: '#64748b' } },
        grid: { left: '3%', right: '4%', bottom: '5%', top: '15%', containLabel: true },
        xAxis: { type: 'category', data: areas.map(a => a.name), axisLine: { lineStyle: { color: '#cbd5e1' } }, axisLabel: { color: '#64748b' } },
        yAxis: [
          { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }, axisLabel: { color: '#64748b' } },
          { type: 'value', name: '使用率%', max: 100, splitLine: { show: false }, axisLabel: { color: '#64748b' } }
        ],
        series: [
          { name: '空闲', type: 'bar', stack: 'total', barWidth: '40%', data: areas.map(a => a.idlePorts) },
          { name: '充电中', type: 'bar', stack: 'total', data: areas.map(a => a.chargingPorts) },
          { name: '故障', type: 'bar', stack: 'total', itemStyle: { borderRadius: [4, 4, 0, 0] }, data: areas.map(a => a.faultPorts) },
          { name: '使用率%', type: 'line', yAxisIndex: 1, smooth: true, itemStyle: { color: '#8b5cf6' }, lineStyle: { width: 3, shadowColor: 'rgba(139,92,246,0.3)', shadowBlur: 10 }, data: areas.map(a => a.usageRate) }
        ]
      })
    } else {
      hasParkingData.value = false
    }
  } catch (error) {}
}

const initRepairChart = async () => {
  try {
    const res = await getRepairSummary()
    if (res.code === 200 && res.data?.statusList && res.data.statusList.length > 0 && res.data.statusList.some(v => v.count > 0)) {
      hasRepairData.value = true
      await nextTick()
      const statusList = res.data.statusList
      repairChart = echarts.init(repairChartRef.value)
      repairChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '5%', top: '10%', containLabel: true },
        xAxis: { type: 'category', data: statusList.map(s => s.statusName), axisLabel: { color: '#64748b' }, axisLine: { lineStyle: { color: '#cbd5e1' } } },
        yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }, axisLabel: { color: '#64748b' } },
        series: [{
          type: 'bar', barWidth: '40%', data: statusList.map(s => s.count),
          itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: '#38bdf8'}, {offset: 1, color: '#0ea5e9'}]), borderRadius: [4, 4, 0, 0] }
        }]
      })
    } else {
      hasRepairData.value = false
    }
  } catch (error) {}
}

const initHazardChart = async () => {
  try {
    const res = await getHazardSummary()
    if (res.code === 200 && res.data?.statusList && res.data.statusList.length > 0 && res.data.statusList.some(v => v.count > 0)) {
      hasHazardData.value = true
      await nextTick()
      const chartData = res.data.statusList.filter(item => item.count > 0).map(item => ({ name: item.statusName, value: item.count }))
      hazardChart = echarts.init(hazardChartRef.value)
      hazardChart.setOption({
        color: ['#f59e0b', '#ef4444', '#10b981'],
        tooltip: { trigger: 'item' },
        legend: { bottom: '5%', icon: 'circle', textStyle: { color: '#64748b' } },
        series: [{
          type: 'pie', radius: ['45%', '70%'], center: ['50%', '42%'],
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          data: chartData
        }]
      })
    } else {
      hasHazardData.value = false
    }
  } catch (error) {}
}

const handleResize = () => {
  portStatusChart?.resize()
  orderTrendChart?.resize()
  parkingUsageChart?.resize()
  repairChart?.resize()
  hazardChart?.resize()
}

onMounted(async () => {
  await fetchDashboard()
  await Promise.all([
    initPortStatusChart(),
    initOrderTrendChart(),
    initParkingUsageChart(),
    initRepairChart(),
    initHazardChart()
  ])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  portStatusChart?.dispose()
  orderTrendChart?.dispose()
  parkingUsageChart?.dispose()
  repairChart?.dispose()
  hazardChart?.dispose()
})
</script>

<style scoped>
.statistics-page {
  padding: 10px;
  background-color: #f8fafc;
  min-height: calc(100vh - 84px);
}

.saas-stat-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.03) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  background: #fff;
}
.saas-stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px 0 rgba(0, 0, 0, 0.08) !important;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 20px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  font-family: 'Inter', -apple-system, sans-serif;
  line-height: 1.2;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.saas-chart-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.03) !important;
}

.saas-chart-card :deep(.el-card__header) {
  border-bottom: 1px solid #f1f5f9;
  padding: 20px 24px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  position: relative;
  padding-left: 12px;
}
.card-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: #3b82f6;
  border-radius: 2px;
}
</style>
