<template>
  <div class="ops-dashboard">
    <!-- 今日概况 & 核心指标 (针对管理员和通用角色) -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #3b82f6">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.todayReservations || 0 }}</div>
              <div class="stat-label">今日预约</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #10b981">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.todayOrders || 0 }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #f59e0b">
              <el-icon :size="28"><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.pendingTickets || 0 }}</div>
              <div class="stat-label">待处理工单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #ef4444">
              <el-icon :size="28"><WarnTriangleFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.pendingHazards || 0 }}</div>
              <div class="stat-label">待整改隐患</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主体内容 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 针对不同角色的工作看板 -->
      <el-col :span="14">
        <!-- 1. 运维人员 (MAINTAINER) 工作看板 -->
        <el-card v-if="userStore.roleCode === 'MAINTAINER' || userStore.roleCode === 'PLATFORM_ADMIN'" class="box-card" style="margin-bottom: 20px">
          <template #header>
            <div class="card-header">
              <span>我的待办维修工单</span>
              <el-button link type="primary" @click="router.push('/ops/repair-ticket')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="myTickets" stripe style="width: 100%" v-loading="loadingTickets">
            <el-table-column prop="ticketNo" label="工单编号" width="150" />
            <el-table-column prop="portCode" label="端口识别码" width="130" />
            <el-table-column prop="description" label="故障描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getTicketStatusType(row.status)">
                  {{ getTicketStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="router.push('/ops/repair-ticket')">去处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 2. 巡检人员 (INSPECTOR) 工作看板 -->
        <el-card v-if="userStore.roleCode === 'INSPECTOR' || userStore.roleCode === 'PLATFORM_ADMIN'" class="box-card" style="margin-bottom: 20px">
          <template #header>
            <div class="card-header">
              <span>我的待办巡检任务</span>
              <el-button link type="primary" @click="router.push('/ops/inspection-task')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="myTasks" stripe style="width: 100%" v-loading="loadingTasks">
            <el-table-column prop="taskNo" label="任务编号" width="150" />
            <el-table-column prop="communityName" label="社区" />
            <el-table-column prop="planStartTime" label="计划开始时间" width="160" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getTaskStatusType(row.status)">
                  {{ getTaskStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="router.push('/ops/inspection-task')">去巡检</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 3. 物业管理员 (PROPERTY_ADMIN) 视角：安全隐患监控 -->
        <el-card v-if="userStore.roleCode === 'PROPERTY_ADMIN' || userStore.roleCode === 'PLATFORM_ADMIN'" class="box-card">
          <template #header>
            <div class="card-header">
              <span>社区安全隐患监控</span>
              <el-button link type="primary" @click="router.push('/ops/hazard')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="hazards" stripe style="width: 100%" v-loading="loadingHazards">
            <el-table-column prop="hazardNo" label="隐患编号" width="150" />
            <el-table-column prop="title" label="隐患描述" show-overflow-tooltip />
            <el-table-column prop="level" label="严重程度" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getHazardLevelType(row.level)">
                  {{ getHazardLevelLabel(row.level) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="整改状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getHazardStatusType(row.status)">
                  {{ getHazardStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：数据图表 & 端口状态 -->
      <el-col :span="10">
        <!-- 端口状态环形图 -->
        <el-card class="box-card" style="margin-bottom: 20px">
          <template #header>
            <div class="card-header">
              <span>端口状态监测</span>
            </div>
          </template>
          <div ref="portStatusChartRef" style="height: 250px"></div>
        </el-card>

        <!-- 快捷操作区 -->
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>常用功能</span>
            </div>
          </template>
          <div class="quick-nav">
            <el-button type="primary" icon="OfficeBuilding" @click="router.push('/ops/community')">社区资源</el-button>
            <el-button type="success" icon="Monitor" @click="router.push('/ops/device')">充电设备</el-button>
            <el-button type="warning" icon="Clock" @click="router.push('/ops/reservation')">预约记录</el-button>
            <el-button type="danger" icon="Document" @click="router.push('/ops/order')">订单监控</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import { getDashboard, getPortStatus } from '@/api/statistics'
import { getRepairTickets } from '@/api/repair'
import { getMyInspectionTasks, getInspectionTasks } from '@/api/inspection'
import { getHazards } from '@/api/inspection'

const router = useRouter()
const userStore = useUserStore()

const dashboard = ref({})
const myTickets = ref([])
const myTasks = ref([])
const hazards = ref([])

const loadingTickets = ref(false)
const loadingTasks = ref(false)
const loadingHazards = ref(false)

const portStatusChartRef = ref(null)
let portChart = null

const fetchDashboard = async () => {
  try {
    const res = await getDashboard(userStore.communityId)
    if (res.code === 200) {
      dashboard.value = res.data
    }
  } catch (error) {
    console.error('获取运维看板统计失败:', error)
  }
}

const fetchMyTickets = async () => {
  loadingTickets.value = true
  try {
    const res = await getRepairTickets({ status: 'DISPATCHED', page: 1, size: 5 })
    if (res.code === 200) {
      myTickets.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取待办工单失败:', error)
  } finally {
    loadingTickets.value = false
  }
}

const fetchMyTasks = async () => {
  loadingTasks.value = true
  try {
    const api = userStore.roleCode === 'PLATFORM_ADMIN' ? getInspectionTasks : getMyInspectionTasks
    const res = await api({ page: 1, size: 5 })
    if (res.code === 200) {
      myTasks.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取待办巡检任务失败:', error)
  } finally {
    loadingTasks.value = false
  }
}

const fetchHazards = async () => {
  loadingHazards.value = true
  try {
    const res = await getHazards({ page: 1, size: 5 })
    if (res.code === 200) {
      hazards.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取隐患监测数据失败:', error)
  } finally {
    loadingHazards.value = false
  }
}

const fetchChartData = async () => {
  try {
    const res = await getPortStatus(userStore.communityId)
    if (res.code === 200) {
      renderPortChart(res.data)
    }
  } catch (error) {
    console.error('获取端口统计失败:', error)
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
      top: '5%',
      left: 'center'
    },
    series: [
      {
        name: '端口状态',
        type: 'pie',
        radius: ['50%', '80%'],
        top: '15%',
        avoidLabelOverlap: false,
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
        data: chartData
      }
    ]
  }

  portChart.setOption(option)
}

// 辅助标签函数
const getTicketStatusLabel = (status) => {
  const map = {
    'DISPATCHED': '待处理',
    'PROCESSING': '进行中',
    'RESOLVED': '已完成',
    'CLOSED': '已关闭'
  }
  return map[status] || status
}

const getTicketStatusType = (status) => {
  const map = {
    'DISPATCHED': 'warning',
    'PROCESSING': 'primary',
    'RESOLVED': 'success',
    'CLOSED': 'info'
  }
  return map[status] || 'info'
}

const getTaskStatusLabel = (status) => {
  const map = {
    'TODO': '待处理',
    'DOING': '巡检中',
    'DONE': '已完成'
  }
  return map[status] || status
}

const getTaskStatusType = (status) => {
  const map = {
    'TODO': 'warning',
    'DOING': 'primary',
    'DONE': 'success'
  }
  return map[status] || 'info'
}

const getHazardLevelLabel = (level) => {
  const map = {
    'LOW': '一般',
    'MEDIUM': '较大',
    'HIGH': '严重'
  }
  return map[level] || level
}

const getHazardLevelType = (level) => {
  const map = {
    'LOW': 'info',
    'MEDIUM': 'warning',
    'HIGH': 'danger'
  }
  return map[level] || 'info'
}

const getHazardStatusLabel = (status) => {
  const map = {
    'PENDING': '待处理',
    'RECTIFYING': '整改中',
    'REVIEWING': '待复查',
    'CLOSED': '已完成'
  }
  return map[status] || status
}

const getHazardStatusType = (status) => {
  const map = {
    'PENDING': 'danger',
    'RECTIFYING': 'warning',
    'REVIEWING': 'primary',
    'CLOSED': 'success'
  }
  return map[status] || 'info'
}

const handleResize = () => {
  portChart?.resize()
}

onMounted(() => {
  fetchDashboard()
  fetchChartData()

  if (userStore.roleCode === 'MAINTAINER' || userStore.roleCode === 'PLATFORM_ADMIN') {
    fetchMyTickets()
  }
  if (userStore.roleCode === 'INSPECTOR' || userStore.roleCode === 'PLATFORM_ADMIN') {
    fetchMyTasks()
  }
  if (userStore.roleCode === 'PROPERTY_ADMIN' || userStore.roleCode === 'PLATFORM_ADMIN') {
    fetchHazards()
  }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  portChart?.dispose()
})
</script>

<style scoped>
.ops-dashboard {
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

.quick-nav {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-nav .el-button {
  height: 50px;
  margin: 0;
  font-size: 14px;
}
</style>
