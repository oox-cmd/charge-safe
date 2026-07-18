<template>
  <div class="bigscreen" ref="screenRef">
    <!-- 顶部标题区 -->
    <header class="bigscreen-header">
      <div class="header-left">
        <div ref="hudLottieRef" class="hud-lottie"></div>
        <span class="system-name">社区充电态势大屏</span>
      </div>
      
      <!-- 导航 Tab -->
      <div class="header-nav">
        <div class="nav-item" :class="{ active: activeTab === 'overview' }" @click="switchTab('overview')">总览态势</div>
        <div class="nav-item" :class="{ active: activeTab === 'device' }" @click="switchTab('device')">设备监控</div>
        <div class="nav-item" :class="{ active: activeTab === 'safety' }" @click="switchTab('safety')">安全防护</div>
      </div>

      <div class="header-right">
        <span class="community-name">{{ currentCommunity }}</span>
        <span class="current-time">{{ currentTime }}</span>
        
        <!-- 用户菜单 -->
        <div class="user-menu" @click="toggleUserMenu" ref="userMenuRef">
          <div class="user-avatar">
            <span>{{ (userStore.nickname || userStore.username || '游客').charAt(0).toUpperCase() }}</span>
          </div>
          <span class="user-name">{{ userStore.nickname || userStore.username || '游客' }}</span>
          <span class="user-role-badge">{{ userStore.roleName || roleInfo }}</span>
          <span class="menu-arrow" :class="{ open: showUserMenu }">▾</span>
          
          <!-- 下拉菜单 -->
          <div class="user-dropdown" v-show="showUserMenu">
            <div class="dropdown-header">{{ userStore.username || '未登录' }}</div>
            <!-- 只有管理员/物业/运维/巡检账号才显示返回后台按钮 -->
            <div
              v-if="canAccessAdmin"
              class="dropdown-item"
              @click.stop="goToAdmin"
            >
              <span>⬅</span> 返回管理后台
            </div>
            <div v-if="userStore.token" class="dropdown-item logout" @click.stop="handleLogout">
              <span>⏻</span> 退出登录
            </div>
            <div v-if="!userStore.token" class="dropdown-item" @click.stop="goToLogin">
              <span>→</span> 登录账号
            </div>
          </div>
        </div>
      </div>
    </header>

    <p v-if="managementBigscreenData" class="simulation-disclaimer">
      {{ managementBigscreenData.dataDisclaimer || '演示环境：数据来自业务数据库与设备模拟器，不代表真实硬件实时采集。' }}
    </p>
    <p v-if="dataError" class="data-error">{{ dataError }}</p>

    <!-- 主体区域：总览态势 -->
    <div class="bigscreen-body" v-show="activeTab === 'overview'">
      <!-- 左侧统计区 -->
      <aside class="bigscreen-left">
        <div class="panel">
          <div class="panel-title">端口状态分布</div>
          <div ref="portStatusChartRef" class="chart-container"></div>
        </div>
        <div class="panel">
          <div class="panel-title">核心指标</div>
          <div class="metric-grid">
            <div class="metric-item" v-for="metric in coreMetrics" :key="metric.label" :class="getMetricClass(metric.label)">
              <div class="metric-value" :style="{ color: metric.color }">{{ metric.value }}</div>
              <div class="metric-label">{{ metric.label }}</div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 中间核心区 -->
      <main class="bigscreen-center">
        <div class="panel station-panel">
          <div class="panel-title">中心充电站全息监控</div>
          <div class="station-hologram">
            <div ref="stationLottieRef" class="station-lottie"></div>
            <div class="station-status">
              <div class="status-item"><span class="label">运行状态</span><span class="value demo-val">演示状态<span class="demo-mark">（非硬件实时）</span></span></div>
              <div class="status-item"><span class="label">当前负载</span><span class="value demo-val">42%<span class="demo-mark">（演示）</span></span></div>
              <div class="status-item"><span class="label">今日能耗</span><span class="value demo-val">124 kW·h<span class="demo-mark">（演示）</span></span></div>
            </div>
          </div>
        </div>
        <div class="panel">
          <div class="panel-title">端口状态矩阵</div>
          <div class="port-matrix">
            <div v-for="area in parkingAreas" :key="area.id" class="parking-area">
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
            <div v-if="parkingAreas.length === 0" class="empty-tip">暂无端口数据</div>
          </div>
        </div>
      </main>

      <!-- 右侧订单趋势 -->
      <aside class="bigscreen-right">
        <div class="panel">
          <div class="panel-title">车棚利用率</div>
          <div ref="parkingUsageChartRef" class="chart-container"></div>
        </div>
        <div class="panel" style="flex:1;">
          <div class="panel-title">订单趋势</div>
          <div ref="orderTrendChartRef" class="chart-container"></div>
        </div>
      </aside>
    </div>

    <!-- 主体区域：设备监控 -->
    <div class="bigscreen-body device-view" v-show="activeTab === 'device'">
      <aside class="bigscreen-left">
        <div class="panel list-panel">
          <div class="panel-title">车棚列表</div>
          <div class="area-list">
            <div 
              class="area-item" 
              v-for="area in parkingAreas" 
              :key="area.id"
              :class="{ active: selectedAreaId === area.id }"
              @click="selectedAreaId = area.id"
            >
              {{ area.name }}
              <span class="area-badge">{{ area.ports?.length || 0 }}</span>
            </div>
          </div>
        </div>
      </aside>

      <main class="bigscreen-center">
        <div class="panel">
          <div class="panel-title">
            {{ selectedArea?.name || '请选择车棚' }} - 端口详情
          </div>
          <div class="port-matrix detail-matrix">
            <div class="port-grid large">
              <div
                v-for="port in selectedArea?.ports || []"
                :key="port.id"
                class="port-block large-block"
                :class="{ active: selectedPortId === port.id }"
                :style="{ backgroundColor: getPortColor(port.status) }"
                @click="selectedPortId = port.id"
              >
                <span class="port-number">{{ port.portNumber }}</span>
                <span class="port-status-text">{{ getPortStatusName(port.status) }}</span>
              </div>
            </div>
            <div v-if="!selectedArea" class="empty-tip">请先在左侧选择车棚</div>
          </div>
        </div>
      </main>

      <aside class="bigscreen-right">
        <div class="panel detail-panel" v-if="selectedPort">
          <div class="panel-title">端口 {{ selectedPort.portNumber }} 实时监控</div>
          
          <!-- 动态 Lottie 展示区 -->
          <div class="device-lottie-container">
            <div v-show="selectedPort.status === 'CHARGING'" ref="chargingLottieRef" class="scooter-lottie"></div>
            <div v-show="selectedPort.status === 'FAULT'" ref="sparkLottieRef" class="spark-lottie"></div>
            
            <div v-show="!['CHARGING', 'FAULT'].includes(selectedPort.status)" class="idle-placeholder">
              端口处于: {{ getPortStatusName(selectedPort.status) }}
            </div>
          </div>
          
          <div class="port-info-list">
            <div class="info-row"><span class="label">端口状态</span> <span class="value" :style="{color: getPortColor(selectedPort.status)}">{{ getPortStatusName(selectedPort.status) }}</span></div>
            <div class="info-row"><span class="label">当前电压</span> <span class="value demo-val">220V<span class="demo-mark">（演示）</span></span></div>
            <div class="info-row"><span class="label">当前电流</span> <span class="value demo-val">{{ selectedPort.status === 'CHARGING' ? '2.1A' : '0A' }}<span class="demo-mark">（演示）</span></span></div>
            <div class="info-row"><span class="label">已充时长</span> <span class="value demo-val">{{ selectedPort.status === 'CHARGING' ? '1h 23m' : '--' }}<span class="demo-mark">（演示）</span></span></div>
          </div>
        </div>
        <div class="panel detail-panel" v-else>
          <div class="panel-title">{{ selectedArea?.name || '当前车棚' }} - 综合面板</div>
          
          <div class="area-summary-cards">
            <div class="summary-card">
              <div class="s-label">总端口</div>
              <div class="s-value" style="color: #409eff">{{ selectedArea?.ports?.length || 0 }}</div>
            </div>
            <div class="summary-card">
              <div class="s-label">充电中</div>
              <div class="s-value" style="color: #e6a23c">{{ selectedArea?.ports?.filter(p => p.status === 'CHARGING').length || 0 }}</div>
            </div>
            <div class="summary-card">
              <div class="s-label">空闲/可用</div>
              <div class="s-value" style="color: #67c23a">{{ selectedArea?.ports?.filter(p => p.status === 'IDLE').length || 0 }}</div>
            </div>
          </div>
          
          <div class="empty-tip" style="margin-top: auto; opacity: 0.6">
            想要查看单个端口的电压和电流？<br/>请在中间矩阵点击对应端口号
          </div>
        </div>
      </aside>
    </div>

    <!-- 主体区域：安全防护 -->
    <div class="bigscreen-body safety-view" v-show="activeTab === 'safety'">
      <aside class="bigscreen-left">
         <div class="panel">
          <div class="panel-title"><span>告警分布</span></div>
          <div class="metric-grid">
            <div class="metric-item danger">
              <div class="metric-value">{{ dashboardData?.faultPorts || 0 }}</div>
              <div class="metric-label">实时告警</div>
            </div>
            <div class="metric-item warning">
              <div class="metric-value">{{ dashboardData?.pendingHazards || 0 }}</div>
              <div class="metric-label">待整改隐患</div>
            </div>
          </div>
        </div>
        <div class="panel" style="flex:1;">
          <div class="panel-title">操作反馈</div>
          <div class="success-lottie-container">
             <div ref="successLottieRef" class="success-lottie" v-show="showSuccessAnim"></div>
             <div class="empty-tip" v-show="!showSuccessAnim">系统安全运行中</div>
          </div>
        </div>
      </aside>

      <main class="bigscreen-center">
        <div class="panel">
          <div class="panel-title">
            <span>实时告警日志</span>
          </div>
          <div class="scroll-list alert-list">
            <div
              v-for="alert in alerts"
              :key="alert.id"
              class="scroll-item"
              :class="alert.alarmLevel === 'ERROR' ? 'item-danger' : 'item-warning'"
            >
              <div class="item-header">
                <span class="item-title">{{ alert.alarmContent || alert.description }}</span>
                <span class="item-time">{{ new Date(alert.createdTime).toLocaleString() }}</span>
              </div>
            </div>
            <div class="empty-tip demo-tip" v-if="alerts.length === 0">暂无告警记录</div>
          </div>
        </div>
      </main>

      <aside class="bigscreen-right">
        <div class="panel">
          <div class="panel-title">
            <span>待处理工单</span>
          </div>
          <div class="scroll-list ticket-list">
            <div v-for="ticket in pendingTickets" :key="ticket.id" class="scroll-item">
              <div class="item-title">{{ ticket.faultDesc }}</div>
              <div class="item-time">{{ new Date(ticket.createdTime).toLocaleString() }}</div>
            </div>
            <div class="empty-tip demo-tip" v-if="pendingTickets.length === 0">暂无待处理工单记录</div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
// The light player intentionally excludes Lottie's expression evaluator, so
// these local display-only animations do not ship a direct-eval capability.
import lottie from 'lottie-web/build/player/esm/lottie_light.min.js'
import stationAnimationData from '@/assets/lottie/station.json'
import hudAnimationData from '@/assets/lottie/loading.json'
import scooterAnimationData from '@/assets/lottie/scooter.json'
import successAnimationData from '@/assets/lottie/success.json'
import sparkAnimationData from '@/assets/lottie/spark.json'

import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { getManagementBigscreen } from '@/api/statistics'

const userStore = useUserStore()
const router = useRouter()

// 状态管理
const activeTab = ref('overview') // 'overview', 'device', 'safety'
const selectedAreaId = ref(null)
const selectedPortId = ref(null)

// Lottie Refs
const hudLottieRef = ref(null)
const stationLottieRef = ref(null)
const chargingLottieRef = ref(null)
const successLottieRef = ref(null)
const sparkLottieRef = ref(null)

let hudLottieInstance = null
let stationLottieInstance = null
let chargingLottieInstance = null
let successLottieInstance = null
let sparkLottieInstance = null

const showSuccessAnim = ref(false)

// 图表 Refs
const portStatusChartRef = ref(null)
const parkingUsageChartRef = ref(null)
const orderTrendChartRef = ref(null)

let portStatusChart = null
let parkingUsageChart = null
let orderTrendChart = null

// 数据
const currentTime = ref('')
const currentCommunity = ref('全部社区')
const roleInfo = ref(userStore.roleName || '管理员')

// 用户菜单
const showUserMenu = ref(false)
const userMenuRef = ref(null)
const canAccessAdmin = computed(() => {
  const code = userStore.roleCode
  return ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'MAINTAINER', 'INSPECTOR'].includes(code)
})

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

// 点击外部关闭菜单
const handleOutsideClick = (e) => {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target)) {
    showUserMenu.value = false
  }
}

const goToAdmin = () => {
  showUserMenu.value = false
  const code = userStore.roleCode
  if (code === 'PLATFORM_ADMIN') router.push('/permission/dashboard')
  else if (code === 'PROPERTY_ADMIN') router.push('/ops/dashboard')
  else if (code === 'MAINTAINER') router.push('/ops/repair-ticket')
  else if (code === 'INSPECTOR') router.push('/ops/inspection-task')
}

const goToLogin = () => {
  showUserMenu.value = false
  router.push('/login')
}

const handleLogout = () => {
  showUserMenu.value = false
  userStore.logout()
  router.push('/login')
}
const dashboardData = ref({})
const managementBigscreenData = ref(null)
const dataError = ref('')
const parkingAreas = ref([])
const alerts = ref([])
const pendingTickets = ref([])
const pendingHazards = ref([])

const coreMetrics = ref([
  { label: '端口总数', value: 0, color: '#409eff' },
  { label: '空闲端口', value: 0, color: '#67c23a' },
  { label: '充电中', value: 0, color: '#e6a23c' },
  { label: '故障', value: 0, color: '#f56c6c' },
  { label: '今日订单', value: 0, color: '#409eff' },
  { label: '今日预约', value: 0, color: '#67c23a' }
])

const getMetricClass = (label) => {
  if (label === '故障') return 'danger'
  if (label === '充电中') return 'warning'
  return ''
}

const portColorMap = {
  IDLE: '#22c55e', RESERVED: '#3b82f6', CHARGING: '#f59e0b',
  FAULT: '#ef4444', MAINTENANCE: '#94a3b8', DISABLED: '#c0c4cc', OFFLINE: '#c0c4cc'
}
const portStatusNameMap = {
  IDLE: '空闲', RESERVED: '预约', CHARGING: '充电',
  FAULT: '故障', MAINTENANCE: '维修', DISABLED: '停用', OFFLINE: '离线'
}

const getPortColor = (status) => portColorMap[status] || '#c0c4cc'
const getPortStatusName = (status) => portStatusNameMap[status] || '未知'

// 计算属性
const selectedArea = computed(() => parkingAreas.value.find(a => a.id === selectedAreaId.value))
const selectedPort = computed(() => selectedArea.value?.ports?.find(p => p.id === selectedPortId.value))

// 时间更新
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

// ========== Lottie 管理 ==========
const initHudLottie = () => {
  if (hudLottieRef.value && !hudLottieInstance) {
    hudLottieInstance = lottie.loadAnimation({
      container: hudLottieRef.value, renderer: 'svg', loop: true, autoplay: true, animationData: hudAnimationData
    })
  }
}

const initStationLottie = () => {
  if (stationLottieRef.value && !stationLottieInstance) {
    stationLottieInstance = lottie.loadAnimation({
      container: stationLottieRef.value, renderer: 'svg', loop: true, autoplay: true, animationData: stationAnimationData
    })
  }
}

const initChargingLottie = () => {
  if (chargingLottieRef.value) {
    if (chargingLottieInstance) chargingLottieInstance.destroy()
    chargingLottieInstance = lottie.loadAnimation({
      container: chargingLottieRef.value, renderer: 'svg', loop: true, autoplay: true, animationData: scooterAnimationData
    })
  }
}

const initSparkLottie = () => {
  if (sparkLottieRef.value) {
    if (sparkLottieInstance) sparkLottieInstance.destroy()
    sparkLottieInstance = lottie.loadAnimation({
      container: sparkLottieRef.value, renderer: 'svg', loop: true, autoplay: true, animationData: sparkAnimationData
    })
  }
}

const initSuccessLottie = () => {
  if (successLottieRef.value) {
    if (successLottieInstance) successLottieInstance.destroy()
    successLottieInstance = lottie.loadAnimation({
      container: successLottieRef.value, renderer: 'svg', loop: false, autoplay: true, animationData: successAnimationData
    })
    successLottieInstance.addEventListener('complete', () => {
      showSuccessAnim.value = false
    })
  }
}


// ========== 数据拉取 ==========
const applyDashboardData = (data) => {
  if (data) {
      dashboardData.value = data
      coreMetrics.value = [
        { label: '端口总数', value: data.totalPorts || 0, color: '#409eff' },
        { label: '空闲端口', value: data.idlePorts || 0, color: '#67c23a' },
        { label: '充电中', value: data.chargingPorts || 0, color: '#e6a23c' },
        { label: '故障', value: data.faultPorts || 0, color: '#f56c6c' },
        { label: '今日订单', value: data.todayOrders || 0, color: '#409eff' },
        { label: '今日预约', value: data.todayReservations || 0, color: '#67c23a' }
      ]
  }
}

const applyParkingAreaData = (data) => {
    if (data?.areas) {
      parkingAreas.value = data.areas.map(area => {
        // 直接使用后端返回的真实端口明细，不再按 totalPorts 伪造端口。
        // 后端以 portNumber 升序返回；portName 经 ChargingPort#getPortCode 序列化为 portCode。
        const ports = (area.ports || []).map(port => ({
          id: port.id,
          portNumber: port.portNo ?? port.portNumber,
          portName: port.portCode ?? port.portName ?? `${port.portNo ?? port.portNumber}号端口`,
          status: port.status
        }))
        return { id: area.id, name: area.name, ports }
      })
      if (!selectedAreaId.value && parkingAreas.value.length > 0) {
        selectedAreaId.value = parkingAreas.value[0].id
      }
    }
}

const fetchManagementData = async () => {
  try {
    const res = await getManagementBigscreen()
    if (res.code === 200) {
      dataError.value = ''
      managementBigscreenData.value = res.data
      applyDashboardData(res.data?.dashboard)
      applyParkingAreaData(res.data?.parkingAreaUsage)

      alerts.value = res.data?.alarms || []

      // 待处理工单和待整改隐患暂时没有明细接口返回，先通过列表接口获取
      try {
        const { getRepairTickets } = await import('@/api/repair')
        const ticketRes = await getRepairTickets({ status: 'PENDING', current: 1, size: 20 })
        if (ticketRes.code === 200) pendingTickets.value = ticketRes.data.records
      } catch(e) {}

      try {
        const { getHazards } = await import('@/api/inspection')
        const hazardRes = await getHazards({ status: 'PENDING', current: 1, size: 20 })
        if (hazardRes.code === 200) pendingHazards.value = hazardRes.data.records
      } catch(e) {}
    }
  } catch (error) {
    dataError.value = '大屏数据加载失败，请检查网络、登录状态和接口权限。'
  }
}

const initCharts = () => {
  try {
    const data = managementBigscreenData.value
    const portStatus = data?.portStatus
    if (portStatus?.statusList && portStatusChartRef.value) {
      if (portStatusChart) portStatusChart.dispose()
      portStatusChart = echarts.init(portStatusChartRef.value)
      portStatusChart.setOption({
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie', radius: ['45%', '70%'],
          itemStyle: { borderRadius: 5, borderColor: '#0a1a3a', borderWidth: 2 },
          label: { show: false },
          data: portStatus.statusList.map(item => ({ name: item.statusName, value: item.count }))
        }]
      })
    }
    const orderTrend = data?.orderTrend || []
    if (orderTrend.length && orderTrendChartRef.value) {
      if (orderTrendChart) orderTrendChart.dispose()
      orderTrendChart = echarts.init(orderTrendChartRef.value)
      orderTrendChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: orderTrend.map(item => item.date), axisLabel: { color: '#a0aec0' } },
        yAxis: { type: 'value', axisLabel: { color: '#a0aec0' } },
        series: [
          { name: '订单数', type: 'line', smooth: true, data: orderTrend.map(item => item.count), itemStyle: { color: '#409eff' } }
        ]
      })
    }
    const parkingAreaUsage = data?.parkingAreaUsage
    if (parkingAreaUsage?.areas && parkingUsageChartRef.value) {
      if (parkingUsageChart) parkingUsageChart.dispose()
      parkingUsageChart = echarts.init(parkingUsageChartRef.value)
      parkingUsageChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: parkingAreaUsage.areas.map(a => a.name), axisLabel: { color: '#a0aec0', fontSize: 10 } },
        yAxis: { type: 'value', max: 100, axisLabel: { color: '#a0aec0' } },
        series: [{ type: 'bar', data: parkingAreaUsage.areas.map(a => a.usageRate), itemStyle: { color: '#409eff' } }]
      })
    }
  } catch (error) {}
}

const handleResize = () => {
  portStatusChart?.resize()
  parkingUsageChart?.resize()
  orderTrendChart?.resize()
}

// ========== Watchers & Lifecycle ==========
const switchTab = (tab) => {
  activeTab.value = tab
  nextTick(() => {
    // 总览态势
    if (tab === 'overview') {
      initStationLottie()
      initCharts()
    } else {
      stationLottieInstance?.destroy(); stationLottieInstance = null;
    }
    
    // 设备监控
    if (tab === 'device') {
      if (selectedPort.value?.status === 'CHARGING') initChargingLottie()
      else if (selectedPort.value?.status === 'FAULT') initSparkLottie()
    } else {
      chargingLottieInstance?.destroy(); chargingLottieInstance = null;
      sparkLottieInstance?.destroy(); sparkLottieInstance = null;
    }
  })
}

watch(selectedPort, (newPort) => {
  if (activeTab.value !== 'device') return
  
  // 清理所有设备页的 lottie
  chargingLottieInstance?.destroy(); chargingLottieInstance = null;
  sparkLottieInstance?.destroy(); sparkLottieInstance = null;

  nextTick(() => {
    if (newPort?.status === 'CHARGING') initChargingLottie()
    else if (newPort?.status === 'FAULT') initSparkLottie()
  })
})

let clockTimer = null
let refreshTimer = null

onMounted(async () => {
  updateTime()
  clockTimer = setInterval(updateTime, 1000)
  
  await fetchManagementData()

  initHudLottie()
  switchTab('overview')
  
  window.addEventListener('resize', handleResize)
  document.addEventListener('click', handleOutsideClick)
  
  refreshTimer = setInterval(() => {
    fetchManagementData().then(() => {
      if (activeTab.value === 'overview') nextTick(initCharts)
    })
  }, 60000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('click', handleOutsideClick)
  hudLottieInstance?.destroy()
  stationLottieInstance?.destroy()
  chargingLottieInstance?.destroy()
  successLottieInstance?.destroy()
  sparkLottieInstance?.destroy()
  
  portStatusChart?.dispose()
  parkingUsageChart?.dispose()
  orderTrendChart?.dispose()
  clearInterval(clockTimer)
  clearInterval(refreshTimer)
})
</script>

<style scoped>
.bigscreen {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #0B101E 0%, #111827 50%, #0B101E 100%);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* 顶部标题区 */
.bigscreen-header {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: linear-gradient(180deg, rgba(0, 242, 254, 0.08) 0%, transparent 100%);
  border-bottom: 1px solid rgba(0, 242, 254, 0.2);
  z-index: 10;
}

.simulation-disclaimer, .data-error {
  position: absolute;
  z-index: 20;
  top: 76px;
  left: 50%;
  transform: translateX(-50%);
  margin: 0;
  max-width: min(760px, 86vw);
  padding: 7px 12px;
  border-radius: 4px;
  font-size: 12px;
  text-align: center;
}

.simulation-disclaimer { color: #fde68a; background: rgba(120, 53, 15, .82); border: 1px solid rgba(245, 158, 11, .5); }
.data-error { color: #fecaca; background: rgba(127, 29, 29, .88); border: 1px solid rgba(248, 113, 113, .6); }

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 450px;
}

.hud-lottie {
  width: 40px;
  height: 40px;
  opacity: 0.9;
}

.system-name {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(90deg, #00F2FE, #4FACFE);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
  white-space: nowrap;
}

.header-nav {
  display: flex;
  gap: 30px;
  align-items: center;
}

.nav-item {
  font-size: 18px;
  color: #718096;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 4px;
  transition: all 0.3s ease;
  position: relative;
  white-space: nowrap;
}

.nav-item:hover {
  color: #00F2FE;
}

.nav-item.active {
  color: #fff;
  font-weight: bold;
  background: rgba(0, 242, 254, 0.1);
  border: 1px solid rgba(0, 242, 254, 0.3);
  box-shadow: 0 0 15px rgba(0, 242, 254, 0.2) inset;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 450px;
  justify-content: flex-end;
}

.community-name, .current-time {
  font-size: 15px;
  color: #a0aec0;
  white-space: nowrap;
}

.role-info {
  font-size: 13px;
  color: #409eff;
  padding: 4px 10px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 4px;
  background: rgba(64, 158, 255, 0.1);
}

/* 用户菜单 */
.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 8px;
  border: 1px solid rgba(79, 172, 254, 0.3);
  background: rgba(79, 172, 254, 0.08);
  transition: all 0.2s;
  user-select: none;
}
.user-menu:hover {
  background: rgba(79, 172, 254, 0.18);
  border-color: rgba(79, 172, 254, 0.6);
}
.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #0b101e;
  flex-shrink: 0;
}
.user-name {
  font-size: 13px;
  color: #e2e8f0;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-role-badge {
  font-size: 11px;
  color: #4facfe;
  padding: 1px 6px;
  border: 1px solid rgba(79, 172, 254, 0.4);
  border-radius: 10px;
  background: rgba(79, 172, 254, 0.1);
}
.menu-arrow {
  font-size: 10px;
  color: #a0aec0;
  transition: transform 0.2s;
}
.menu-arrow.open {
  transform: rotate(180deg);
}
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(79, 172, 254, 0.25);
  border-radius: 10px;
  z-index: 9999;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}
.dropdown-header {
  padding: 10px 14px;
  font-size: 12px;
  color: #718096;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  font-weight: 500;
}
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  font-size: 13px;
  color: #cbd5e0;
  cursor: pointer;
  transition: background 0.15s;
}
.dropdown-item:hover {
  background: rgba(79, 172, 254, 0.12);
  color: #4facfe;
}
.dropdown-item.logout {
  color: #fc8181;
  border-top: 1px solid rgba(255,255,255,0.06);
}
.dropdown-item.logout:hover {
  background: rgba(252, 129, 129, 0.1);
  color: #f56c6c;
}

/* 主体区域 */
.bigscreen-body {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
  overflow: hidden;
}

/* 侧边与中央结构 - 添加滚动条防止溢出覆盖 */
.bigscreen-left { width: 320px; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; }
.bigscreen-right { width: 350px; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; }
.bigscreen-center { flex: 1; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; }

/* 滚动条美化 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: rgba(0, 242, 254, 0.3); border-radius: 3px; }
::-webkit-scrollbar-track { background: transparent; }

/* 玻璃面板 */
.panel {
  background: rgba(16, 24, 40, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(0, 242, 254, 0.15);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.panel-title {
  font-size: 16px;
  font-weight: bold;
  color: #e2e8f0;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  padding-left: 12px;
}
.panel-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 2px;
  bottom: 2px;
  width: 4px;
  background: #00F2FE;
  border-radius: 2px;
}

.chart-container { flex: 1; min-height: 200px; }

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.metric-item {
  text-align: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  transition: all 0.3s;
}
.metric-item:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateY(-2px);
}
.metric-item.danger { border-color: rgba(245, 108, 108, 0.3); background: rgba(245, 108, 108, 0.05); }
.metric-item.danger .metric-value { color: #f56c6c; }
.metric-item.warning { border-color: rgba(230, 162, 60, 0.3); background: rgba(230, 162, 60, 0.05); }
.metric-item.warning .metric-value { color: #e6a23c; }

.metric-value { font-size: 28px; font-weight: bold; }
.metric-label { font-size: 13px; color: #a0aec0; margin-top: 8px; }

/* 充电站全息监控卡片 */
.station-panel { flex: unset; height: 260px; overflow: hidden; }
.station-hologram { display: flex; align-items: center; height: 100%; gap: 30px; overflow: hidden; }
.station-lottie { flex: 1; height: 100%; max-width: 300px; display: flex; align-items: center; justify-content: center; }
.station-lottie svg { max-height: 100%; max-width: 100%; }
.station-status { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.status-item { display: flex; justify-content: space-between; padding: 12px; background: rgba(0,0,0,0.2); border-radius: 6px; }
.status-item .label { color: #a0aec0; }
.status-item .value { font-weight: bold; color: #fff; }
.status-item .value.success { color: #67c23a; }

/* 端口矩阵 */
.port-matrix { flex: 1; overflow-y: auto; }
.parking-area { margin-bottom: 20px; }
.area-name { font-size: 14px; color: #cbd5e1; margin-bottom: 12px; }
.port-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(45px, 1fr)); gap: 10px; }
.port-block {
  height: 45px; border-radius: 6px; display: flex; align-items: center; justify-content: center;
  font-size: 14px; color: white; cursor: pointer; transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.port-block:hover { transform: scale(1.1); box-shadow: 0 4px 12px rgba(0,0,0,0.3); }

/* 设备监控页特定样式 */
.list-panel { flex: 1; }
.area-list { display: flex; flex-direction: column; gap: 10px; }
.area-item {
  padding: 16px; background: rgba(255,255,255,0.03); border-radius: 8px;
  cursor: pointer; display: flex; justify-content: space-between; align-items: center;
  transition: all 0.3s;
}
.area-item:hover { background: rgba(255,255,255,0.08); }
.area-item.active { background: rgba(0, 242, 254, 0.15); border-left: 4px solid #00F2FE; }
.area-badge { background: #409eff; padding: 2px 8px; border-radius: 12px; font-size: 12px; }

.detail-matrix .port-grid.large { grid-template-columns: repeat(auto-fill, minmax(80px, 1fr)); gap: 16px; }
.large-block { height: 80px; flex-direction: column; gap: 8px; }
.large-block.active { border: 2px solid #fff; transform: scale(1.05); }
.port-status-text { font-size: 12px; opacity: 0.9; }

.detail-panel { flex: 1; }
.device-lottie-container { height: 250px; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.2); border-radius: 8px; margin-bottom: 20px; overflow: hidden; }
.scooter-lottie, .spark-lottie { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
.idle-placeholder { font-size: 18px; color: #a0aec0; }

.port-info-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 30px; }
.info-row { display: flex; justify-content: space-between; padding: 12px; background: rgba(255,255,255,0.02); border-radius: 6px; }
.port-actions { display: flex; gap: 16px; }
.btn-action { flex: 1; padding: 12px; border: none; border-radius: 6px; color: white; cursor: pointer; font-size: 15px; font-weight: bold; transition: all 0.2s; }
.btn-action.small { padding: 6px 12px; font-size: 12px; flex: unset; }
.btn-action.danger { background: #f56c6c; }
.btn-action.danger:hover { background: #e64242; }
.btn-action.warning { background: #e6a23c; }
.btn-action.warning:hover { background: #cf9236; }
.btn-action.primary { background: rgba(0, 242, 254, 0.15); border: 1px solid rgba(0, 242, 254, 0.4); color: #00F2FE; }
.btn-action.primary:hover { background: rgba(0, 242, 254, 0.3); }

.area-summary-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-top: 20px; }
.area-summary-cards .summary-card:first-child { grid-column: span 2; }
.summary-card { padding: 16px; background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.05); border-radius: 8px; display: flex; flex-direction: column; gap: 8px; }
.s-label { font-size: 13px; color: #a0aec0; }
.s-value { font-size: 24px; font-weight: bold; }
.area-actions { display: flex; flex-direction: column; }

/* 安全防护特定样式 */
.success-lottie-container { height: 100%; display: flex; align-items: center; justify-content: center; }
.success-lottie { width: 200px; height: 200px; }
.scroll-list { flex: 1; overflow-y: auto; }
.scroll-item { padding: 16px; margin-bottom: 12px; background: rgba(255, 255, 255, 0.02); border-radius: 8px; border-left: 4px solid #409eff; display: flex; flex-direction: column; gap: 12px; }
.scroll-item.item-danger { border-left-color: #f56c6c; background: rgba(245, 108, 108, 0.05); }
.item-header { display: flex; justify-content: space-between; align-items: flex-start; }
.item-title { font-size: 14px; color: #e2e8f0; line-height: 1.5; }
.item-time { font-size: 12px; color: #718096; white-space: nowrap; margin-left: 12px; }

.empty-tip { text-align: center; color: #718096; padding: 40px 20px; font-size: 14px; }

/* 演示数据隔离标记 */
.badge.demo { background: rgba(148, 163, 184, 0.25); color: #94a3b8; border: 1px solid rgba(148, 163, 184, 0.4); font-size: 11px; padding: 2px 8px; border-radius: 10px; font-weight: normal; letter-spacing: 1px; }
.demo-mark { font-size: 11px; color: #94a3b8; font-weight: normal; margin-left: 4px; white-space: nowrap; }
.demo-val { color: #94a3b8; }
.demo-tip { font-size: 12px; color: #94a3b8; }
</style>
