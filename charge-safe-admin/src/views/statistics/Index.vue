<template>
  <div class="statistics-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="3" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
          <el-icon :size="40" class="stat-icon" :style="{ color: card.color }">
            <component :is="card.icon" />
          </el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <!-- 端口状态环形图 -->
      <el-col :span="8">
        <el-card header="端口状态分布">
          <div ref="portStatusChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>

      <!-- 订单趋势折线图 -->
      <el-col :span="16">
        <el-card header="订单趋势（近7天）">
          <div ref="orderTrendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <!-- 车棚利用率柱状图 -->
      <el-col :span="12">
        <el-card header="车棚利用率">
          <div ref="parkingUsageChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>

      <!-- 工单状态柱状图 -->
      <el-col :span="6">
        <el-card header="工单状态">
          <div ref="repairChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>

      <!-- 隐患状态饼图 -->
      <el-col :span="6">
        <el-card header="安全隐患状态">
          <div ref="hazardChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
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

// 统计数据
const dashboardData = ref({})

// 顶部卡片数据
const statCards = computed(() => [
  { label: '端口总数', value: dashboardData.value.totalPorts || 0, icon: 'Connection', color: '#409eff' },
  { label: '空闲端口', value: dashboardData.value.idlePorts || 0, icon: 'CircleCheck', color: '#67c23a' },
  { label: '充电中', value: dashboardData.value.chargingPorts || 0, icon: 'Lightning', color: '#e6a23c' },
  { label: '故障端口', value: dashboardData.value.faultPorts || 0, icon: 'Warning', color: '#f56c6c' },
  { label: '今日预约', value: dashboardData.value.todayReservations || 0, icon: 'Clock', color: '#409eff' },
  { label: '今日订单', value: dashboardData.value.todayOrders || 0, icon: 'Document', color: '#67c23a' },
  { label: '待处理工单', value: dashboardData.value.pendingTickets || 0, icon: 'Tickets', color: '#e6a23c' },
  { label: '待整改隐患', value: dashboardData.value.pendingHazards || 0, icon: 'WarnTriangleFilled', color: '#f56c6c' }
])

// 获取仪表盘数据
const fetchDashboard = async () => {
  try {
    const res = await getDashboard()
    if (res.code === 200) {
      dashboardData.value = res.data
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
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
        legend: { orient: 'vertical', right: '5%', top: 'center' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: 18, fontWeight: 'bold' }
          },
          labelLine: { show: false },
          data: chartData
        }]
      })
    }
  } catch (error) {
    console.error('获取端口状态失败:', error)
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
        legend: { data: ['订单数', '完成数', '金额(元)'] },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: res.data.dates
        },
        yAxis: [
          { type: 'value', name: '数量' },
          { type: 'value', name: '金额(元)' }
        ],
        series: [
          {
            name: '订单数',
            type: 'line',
            smooth: true,
            data: res.data.orderCounts
          },
          {
            name: '完成数',
            type: 'line',
            smooth: true,
            data: res.data.completedCounts
          },
          {
            name: '金额(元)',
            type: 'line',
            smooth: true,
            yAxisIndex: 1,
            data: res.data.amounts
          }
        ]
      })
    }
  } catch (error) {
    console.error('获取订单趋势失败:', error)
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
        legend: { data: ['空闲', '充电中', '故障', '使用率%'] },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: areas.map(a => a.name)
        },
        yAxis: [
          { type: 'value', name: '端口数' },
          { type: 'value', name: '使用率%', max: 100 }
        ],
        series: [
          { name: '空闲', type: 'bar', stack: 'total', data: areas.map(a => a.idlePorts) },
          { name: '充电中', type: 'bar', stack: 'total', data: areas.map(a => a.chargingPorts) },
          { name: '故障', type: 'bar', stack: 'total', data: areas.map(a => a.faultPorts) },
          {
            name: '使用率%',
            type: 'line',
            yAxisIndex: 1,
            data: areas.map(a => a.usageRate)
          }
        ]
      })
    }
  } catch (error) {
    console.error('获取车棚利用率失败:', error)
  }
}

// 初始化工单状态柱状图
const initRepairChart = async () => {
  try {
    const res = await getRepairSummary()
    if (res.code === 200 && res.data?.statusList) {
      const statusList = res.data.statusList
      repairChart = echarts.init(repairChartRef.value)
      repairChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: statusList.map(s => s.statusName),
          axisLabel: { rotate: 30 }
        },
        yAxis: { type: 'value', name: '数量' },
        series: [{
          type: 'bar',
          data: statusList.map(s => s.count),
          itemStyle: { color: '#409eff' }
        }]
      })
    }
  } catch (error) {
    console.error('获取工单状态失败:', error)
  }
}

// 初始化隐患状态饼图
const initHazardChart = async () => {
  try {
    const res = await getHazardSummary()
    if (res.code === 200 && res.data?.statusList) {
      const chartData = res.data.statusList
        .filter(item => item.count > 0)
        .map(item => ({
          name: item.statusName,
          value: item.count
        }))

      hazardChart = echarts.init(hazardChartRef.value)
      hazardChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', right: '5%', top: 'center' },
        series: [{
          type: 'pie',
          radius: '70%',
          data: chartData,
          emphasis: {
            itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
          }
        }]
      })
    }
  } catch (error) {
    console.error('获取隐患状态失败:', error)
  }
}

// 窗口大小变化时重绘图表
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
  padding: 0;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  height: 100px;
}

.stat-card-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.2;
}
</style>
