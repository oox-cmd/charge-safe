<template>
  <div class="permission-dashboard">
    <!-- 系统总览统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ userCount }}</div>
              <div class="stat-label">系统用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon :size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ roleCount }}</div>
              <div class="stat-label">权限角色数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon :size="28"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ communityCount }}</div>
              <div class="stat-label">接入社区数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #b37feb">
              <el-icon :size="28"><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceCount }}</div>
              <div class="stat-label">设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中部：统计图表 & 系统配置入口 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 端口状态分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>端口状态分布</span>
            </div>
          </template>
          <div ref="portStatusChartRef" style="height: 280px"></div>
        </el-card>
      </el-col>

      <!-- 系统配置入口 -->
      <el-col :span="12">
        <el-card style="height: 343px">
          <template #header>
            <div class="card-header">
              <span>系统配置快捷入口</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" plain @click="router.push('/permission/system/user')">
              <el-icon><User /></el-icon>用户管理
            </el-button>
            <el-button type="success" plain @click="router.push('/permission/system/role')">
              <el-icon><UserFilled /></el-icon>角色管理
            </el-button>
            <el-button type="warning" plain @click="router.push('/permission/system/menu')">
              <el-icon><Menu /></el-icon>菜单管理
            </el-button>
            <el-button type="danger" plain @click="router.push('/permission/system/dept')">
              <el-icon><OfficeBuilding /></el-icon>部门管理
            </el-button>
            <el-button type="info" plain @click="router.push('/permission/device-category')">
              <el-icon><Memo /></el-icon>设备分类
            </el-button>
            <el-button type="primary" plain @click="router.push('/permission/simulator')">
              <el-icon><Cpu /></el-icon>设备模拟器
            </el-button>
            <el-button type="success" plain @click="router.push('/permission/log')">
              <el-icon><Notebook /></el-icon>操作日志
            </el-button>
            <el-button type="warning" plain @click="router.push('/bigscreen')">
              <el-icon><FullScreen /></el-icon>安全大屏
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 下部：近7天充电订单趋势 & 最近操作日志 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 近7天订单趋势 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>近 7 天充电订单趋势</span>
            </div>
          </template>
          <div ref="orderTrendChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 最近操作日志 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近系统操作日志</span>
              <el-button link type="primary" @click="router.push('/permission/log')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentLogs" stripe style="width: 100%; height: 300px">
            <el-table-column prop="operator" label="操作人" width="100" show-overflow-tooltip />
            <el-table-column prop="module" label="模块" width="100" />
            <el-table-column prop="operation" label="操作内容" show-overflow-tooltip />
            <el-table-column prop="createTime" label="操作时间" width="160" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getUsers, getRoleList } from '@/api/system'
import { getCommunities } from '@/api/community'
import { getDevices } from '@/api/device'
import { getLogs } from '@/api/log'
import { getPortStatus, getOrderTrend } from '@/api/statistics'

const router = useRouter()

const userCount = ref(0)
const roleCount = ref(0)
const communityCount = ref(0)
const deviceCount = ref(0)
const recentLogs = ref([])

const portStatusChartRef = ref(null)
const orderTrendChartRef = ref(null)
let portChart = null
let trendChart = null

const fetchCounts = async () => {
  try {
    const userRes = await getUsers({ page: 1, size: 1 })
    if (userRes.code === 200) userCount.value = userRes.data.total || 0

    const roleRes = await getRoleList()
    if (roleRes.code === 200) roleCount.value = roleRes.data.length || 0

    const communityRes = await getCommunities({ page: 1, size: 1 })
    if (communityRes.code === 200) communityCount.value = communityRes.data.total || 0

    const deviceRes = await getDevices({ page: 1, size: 1 })
    if (deviceRes.code === 200) deviceCount.value = deviceRes.data.total || 0
  } catch (error) {
    console.error('获取总览统计失败:', error)
  }
}

const fetchLogs = async () => {
  try {
    const res = await getLogs({ page: 1, size: 6 })
    if (res.code === 200) {
      recentLogs.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取最近日志失败:', error)
  }
}

const fetchChartsData = async () => {
  try {
    // 1. 端口状态图表
    const portRes = await getPortStatus()
    if (portRes.code === 200) {
      renderPortChart(portRes.data)
    }

    // 2. 订单趋势图表
    const trendRes = await getOrderTrend()
    if (trendRes.code === 200) {
      renderTrendChart(trendRes.data)
    }
  } catch (error) {
    console.error('获取图表数据失败:', error)
  }
}

const renderPortChart = (data) => {
  if (!portStatusChartRef.value) return
  if (portChart) portChart.dispose()

  portChart = echarts.init(portStatusChartRef.value)

  const statusMap = {
    'IDLE': '空闲',
    'RESERVED': '已预约',
    'CHARGING': '充电中',
    'FAULT': '故障',
    'MAINTENANCE': '维修中',
    'DISABLED': '停用',
    'OFFLINE': '离线'
  }

  const colorMap = {
    'IDLE': '#22c55e',
    'RESERVED': '#3b82f6',
    'CHARGING': '#f59e0b',
    'FAULT': '#ef4444',
    'MAINTENANCE': '#94a3b8',
    'DISABLED': '#cbd5e1',
    'OFFLINE': '#cbd5e1'
  }

  const chartData = Object.entries(data).map(([key, value]) => ({
    name: statusMap[key] || key,
    value: value,
    itemStyle: { color: colorMap[key] || '#999' }
  })).filter(item => item.value > 0)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}个 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: chartData.map(d => d.name)
    },
    series: [
      {
        name: '端口状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: chartData
      }
    ]
  }

  portChart.setOption(option)
}

const renderTrendChart = (data) => {
  if (!orderTrendChartRef.value) return
  if (trendChart) trendChart.dispose()

  trendChart = echarts.init(orderTrendChartRef.value)

  const dates = data.map(item => item.date)
  const counts = data.map(item => item.count)
  const amounts = data.map(item => item.amount)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['订单量', '充电金额']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: dates,
        axisPointer: { type: 'shadow' }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '订单量 (笔)',
        minInterval: 1
      },
      {
        type: 'value',
        name: '充电金额 (元)',
        axisLabel: { formatter: '{value} 元' }
      }
    ],
    series: [
      {
        name: '订单量',
        type: 'bar',
        barWidth: '40%',
        data: counts,
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '充电金额',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: amounts,
        itemStyle: { color: '#f59e0b' },
        lineStyle: { width: 3 }
      }
    ]
  }

  trendChart.setOption(option)
}

const handleResize = () => {
  portChart?.resize()
  trendChart?.resize()
}

onMounted(() => {
  fetchCounts()
  fetchLogs()
  fetchChartsData()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  portChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.permission-dashboard {
  padding: 10px 0;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #8c92a4;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 10px 0;
}

.quick-actions .el-button {
  height: 70px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  margin: 0;
}

:deep(.quick-actions .el-button span) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
</style>
