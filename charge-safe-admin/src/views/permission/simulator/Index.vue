<template>
  <div class="simulator-container">
    <el-tabs type="border-card">
      <!-- 端口状态模拟 -->
      <el-tab-pane label="端口状态模拟">
        <el-form :inline="true" :model="filters" class="search-form">
          <el-form-item label="社区">
            <el-select v-model="filters.communityId" placeholder="选择社区" clearable @change="handleCommunityChange" style="width: 180px">
              <el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="车棚">
            <el-select v-model="filters.parkingAreaId" placeholder="选择车棚" clearable @change="handleParkingAreaChange" style="width: 180px">
              <el-option v-for="p in parkingAreas" :key="p.id" :label="p.name" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="设备">
            <el-select v-model="filters.deviceId" placeholder="选择设备" clearable @change="handleDeviceChange" style="width: 180px">
              <el-option v-for="d in devices" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="fetchPorts">查询端口</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="ports" stripe v-loading="loadingPorts" style="width: 100%; margin-top: 15px">
          <el-table-column prop="portNo" label="端口编号" width="100" />
          <el-table-column prop="portCode" label="端口识别码" width="150" />
          <el-table-column prop="deviceName" label="所属设备" />
          <el-table-column prop="status" label="当前状态" width="130">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="快捷状态模拟" width="400" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="success" :disabled="row.status === 'IDLE'" @click="handleStatusSimulate(row, 'IDLE')">
                设为空闲
              </el-button>
              <el-button size="small" type="warning" :disabled="row.status === 'CHARGING'" @click="handleStatusSimulate(row, 'CHARGING')">
                设为充电中
              </el-button>
              <el-button size="small" type="danger" :disabled="row.status === 'FAULT'" @click="handleFaultSimulate(row)">
                模拟故障
              </el-button>
              <el-button size="small" type="info" :disabled="row.status !== 'FAULT'" @click="handleRecoverSimulate(row)">
                恢复正常
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 充电订单模拟 -->
      <el-tab-pane label="充电订单模拟">
        <div class="tab-toolbar">
          <el-button type="primary" icon="Refresh" @click="fetchOrders">刷新充电中订单</el-button>
        </div>

        <el-table :data="orders" stripe v-loading="loadingOrders" style="width: 100%; margin-top: 15px">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="username" label="用户" width="120" />
          <el-table-column prop="portCode" label="端口识别码" width="150" />
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="chargingPrice" label="费率 (元/小时)" width="120" />
          <el-table-column prop="status" label="订单状态" width="120">
            <template #default="{ row }">
              <el-tag type="warning">充电中</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="danger" icon="VideoPlay" @click="handleChargingCompleteSimulate(row)">
                模拟充电完成
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommunityList } from '@/api/community'
import { getParkingAreaList } from '@/api/community'
import { getDeviceList, getPorts } from '@/api/device'
import { getOrders } from '@/api/charging'
import {
  simulatePortStatus,
  simulatePortFault,
  simulatePortRecover,
  simulateChargingComplete
} from '@/api/simulator'

const filters = ref({
  communityId: null,
  parkingAreaId: null,
  deviceId: null
})

const communities = ref([])
const parkingAreas = ref([])
const devices = ref([])
const ports = ref([])
const orders = ref([])

const loadingPorts = ref(false)
const loadingOrders = ref(false)

// 获取基本级联数据
const fetchCommunities = async () => {
  try {
    const res = await getCommunityList()
    if (res.code === 200) {
      communities.value = res.data
    }
  } catch (error) {
    console.error('获取社区列表失败:', error)
  }
}

const handleCommunityChange = async (val) => {
  filters.value.parkingAreaId = null
  filters.value.deviceId = null
  parkingAreas.value = []
  devices.value = []
  if (val) {
    try {
      const res = await getParkingAreaList(val)
      if (res.code === 200) {
        parkingAreas.value = res.data
      }
    } catch (error) {
      console.error('获取车棚列表失败:', error)
    }
  }
}

const handleParkingAreaChange = async (val) => {
  filters.value.deviceId = null
  devices.value = []
  if (val) {
    try {
      const res = await getDeviceList(val)
      if (res.code === 200) {
        devices.value = res.data
      }
    } catch (error) {
      console.error('获取设备列表失败:', error)
    }
  }
}

const handleDeviceChange = () => {
  fetchPorts()
}

// 获取端口列表
const fetchPorts = async () => {
  loadingPorts.value = true
  try {
    const params = {
      page: 1,
      size: 50,
      deviceId: filters.value.deviceId || undefined
    }
    const res = await getPorts(params)
    if (res.code === 200) {
      ports.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取端口列表失败:', error)
  } finally {
    loadingPorts.value = false
  }
}

// 获取充电中订单列表
const fetchOrders = async () => {
  loadingOrders.value = true
  try {
    const res = await getOrders({ status: 'CHARGING', page: 1, size: 50 })
    if (res.code === 200) {
      orders.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loadingOrders.value = false
  }
}

// 模拟操作处理器
const handleStatusSimulate = async (row, status) => {
  try {
    const res = await simulatePortStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success(`成功模拟将端口状态变更为【${getStatusLabel(status)}】`)
      fetchPorts()
    }
  } catch (error) {
    console.error('模拟端口状态变更失败:', error)
  }
}

const handleFaultSimulate = async (row) => {
  ElMessageBox.confirm(`确定要对端口 "${row.portCode}" 模拟故障告警吗？这会生成系统故障工单。`, '危险操作确认', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await simulatePortFault(row.id)
      if (res.code === 200) {
        ElMessage.success('成功模拟故障上报')
        fetchPorts()
      }
    } catch (error) {
      console.error('模拟故障失败:', error)
    }
  }).catch(() => {})
}

const handleRecoverSimulate = async (row) => {
  try {
    const res = await simulatePortRecover(row.id)
    if (res.code === 200) {
      ElMessage.success('成功模拟故障恢复')
      fetchPorts()
    }
  } catch (error) {
    console.error('模拟故障恢复失败:', error)
  }
}

const handleChargingCompleteSimulate = async (row) => {
  ElMessageBox.confirm(`确认要对订单 "${row.orderNo}" 模拟充电完成吗？这会结束充电并计算模拟费用。`, '二次确认', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await simulateChargingComplete(row.id)
      if (res.code === 200) {
        ElMessage.success('已模拟充电完成订单结算')
        fetchOrders()
      }
    } catch (error) {
      console.error('模拟充电完成失败:', error)
    }
  }).catch(() => {})
}

// 标签映射辅助
const getStatusLabel = (status) => {
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

const getStatusTagType = (status) => {
  const map = {
    'IDLE': 'success',
    'RESERVED': 'primary',
    'CHARGING': 'warning',
    'FAULT': 'danger',
    'MAINTENANCE': 'info',
    'DISABLED': 'info',
    'OFFLINE': 'info'
  }
  return map[status] || 'info'
}

onMounted(() => {
  fetchCommunities()
  fetchPorts()
  fetchOrders()
})
</script>

<style scoped>
.simulator-container {
  padding: 10px 0;
}
.tab-toolbar {
  margin-bottom: 10px;
}
</style>
