<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon :size="28"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.totalPorts || 0 }}</div>
              <div class="stat-label">端口总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.idlePorts || 0 }}</div>
              <div class="stat-label">空闲端口</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon :size="28"><Lightning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.chargingPorts || 0 }}</div>
              <div class="stat-label">充电中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon :size="28"><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.faultPorts || 0 }}</div>
              <div class="stat-label">故障端口</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon" style="background: #909399">
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
            <div class="stat-icon" style="background: #b37feb">
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
            <div class="stat-icon" style="background: #ff85c0">
              <el-icon :size="28"><Tickets /></el-icon>
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
            <div class="stat-icon" style="background: #ffc53d">
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

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>端口状态分布</span>
          </template>
          <div ref="portStatusChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="router.push('/community')">
              <el-icon><OfficeBuilding /></el-icon>社区管理
            </el-button>
            <el-button type="success" @click="router.push('/port')">
              <el-icon><Connection /></el-icon>端口管理
            </el-button>
            <el-button type="warning" @click="router.push('/repair-ticket')">
              <el-icon><Tickets /></el-icon>维修工单
            </el-button>
            <el-button type="danger" @click="router.push('/hazard')">
              <el-icon><WarnTriangleFilled /></el-icon>安全隐患
            </el-button>
            <el-button @click="router.push('/notification')">
              <el-icon><Bell /></el-icon>消息通知
              <el-badge v-if="dashboard.unreadNotifications > 0" :value="dashboard.unreadNotifications" />
            </el-button>
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
import { getDashboard } from '@/api/statistics'
import { getPortStatistics } from '@/api/device'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const dashboard = ref({})
const portStatusChart = ref(null)
let chart = null

const fetchDashboard = async () => {
  try {
    const communityId = userStore.communityId
    const res = await getDashboard(communityId)
    if (res.code === 200) {
      dashboard.value = res.data
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

const fetchPortStatistics = async () => {
  try {
    const communityId = userStore.communityId
    const res = await getPortStatistics(communityId)
    if (res.code === 200) {
      renderChart(res.data)
    }
  } catch (error) {
    console.error('获取端口统计失败:', error)
  }
}

const renderChart = (data) => {
  if (!portStatusChart.value) return

  if (chart) {
    chart.dispose()
  }

  chart = echarts.init(portStatusChart.value)

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
    'IDLE': '#67c23a',
    'RESERVED': '#409eff',
    'CHARGING': '#e6a23c',
    'FAULT': '#f56c6c',
    'MAINTENANCE': '#909399',
    'DISABLED': '#c0c4cc',
    'OFFLINE': '#c0c4cc'
  }

  const chartData = Object.entries(data).map(([key, value]) => ({
    name: statusMap[key] || key,
    value: value,
    itemStyle: { color: colorMap[key] || '#999' }
  })).filter(item => item.value > 0)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '端口状态',
        type: 'pie',
        radius: '50%',
        data: chartData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  chart.setOption(option)
}

onMounted(() => {
  fetchDashboard()
  fetchPortStatistics()

  window.addEventListener('resize', () => {
    chart?.resize()
  })
})

onUnmounted(() => {
  chart?.dispose()
})
</script>

<style scoped>
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
  color: #999;
  margin-top: 4px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 20px 0;
}
</style>
