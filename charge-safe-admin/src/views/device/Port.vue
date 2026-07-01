<template>
  <div class="port-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>端口管理</span>
          <el-button type="primary" @click="handleBatchAdd">
            <el-icon><Plus /></el-icon>批量创建端口
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" style="margin-bottom: 20px">
        <el-form-item label="设备">
          <el-select v-model="queryParams.deviceId" placeholder="选择设备" clearable style="width: 200px">
            <el-option v-for="item in devices" :key="item.id" :label="item.deviceName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable style="width: 150px">
            <el-option v-for="item in portStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="portName" label="端口名称" />
        <el-table-column prop="deviceName" label="所属设备" />
        <el-table-column prop="portNumber" label="端口号" width="80" />
        <el-table-column prop="powerRating" label="额定功率(W)" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getPortStatusType(row.status)">
              {{ getPortStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-dropdown @command="(cmd) => handleStatusChange(row, cmd)">
              <el-button type="primary" link>
                修改状态 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="IDLE">空闲</el-dropdown-item>
                  <el-dropdown-item command="DISABLED">停用</el-dropdown-item>
                  <el-dropdown-item command="OFFLINE">离线</el-dropdown-item>
                  <el-dropdown-item command="FAULT">故障</el-dropdown-item>
                  <el-dropdown-item command="MAINTENANCE">维修中</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 批量创建对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量创建端口" width="400px">
      <el-form ref="batchFormRef" :model="batchForm" :rules="batchRules" label-width="80px">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="batchForm.deviceId" placeholder="选择设备" style="width: 100%">
            <el-option v-for="item in devices" :key="item.id" :label="item.deviceName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="count">
          <el-input-number v-model="batchForm.count" :min="1" :max="20" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleBatchSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPorts, updatePortStatus, batchCreatePorts } from '@/api/device'
import { getDeviceList } from '@/api/device'

const route = useRoute()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const batchDialogVisible = ref(false)
const batchFormRef = ref(null)
const devices = ref([])

const portStatusOptions = [
  { value: 'IDLE', label: '空闲' },
  { value: 'RESERVED', label: '已预约' },
  { value: 'CHARGING', label: '充电中' },
  { value: 'FAULT', label: '故障' },
  { value: 'MAINTENANCE', label: '维修中' },
  { value: 'DISABLED', label: '停用' },
  { value: 'OFFLINE', label: '离线' }
]

const queryParams = reactive({
  page: 1,
  size: 20,
  deviceId: route.query.deviceId ? Number(route.query.deviceId) : null,
  status: null
})

const batchForm = reactive({
  deviceId: null,
  count: 4
})

const batchRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  count: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const getPortStatusType = (status) => {
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

const getPortStatusText = (status) => {
  const map = {
    'IDLE': '空闲',
    'RESERVED': '已预约',
    'CHARGING': '充电中',
    'COMPLETED_WAITING_PICKUP': '待取车',
    'OVERTIME_OCCUPIED': '超时占用',
    'FAULT': '故障',
    'MAINTENANCE': '维修中',
    'DISABLED': '停用',
    'OFFLINE': '离线'
  }
  return map[status] || status
}

const fetchDevices = async () => {
  try {
    const res = await getDeviceList()
    if (res.code === 200) {
      devices.value = res.data
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPorts(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (row, status) => {
  const statusText = getPortStatusText(status)
  try {
    await ElMessageBox.confirm(`确定要将端口状态修改为"${statusText}"吗？`, '提示', { type: 'warning' })
    await updatePortStatus(row.id, status)
    ElMessage.success('状态修改成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改失败:', error)
    }
  }
}

const handleBatchAdd = () => {
  batchForm.deviceId = null
  batchForm.count = 4
  batchDialogVisible.value = true
}

const handleBatchSubmit = async () => {
  try {
    await batchFormRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    await batchCreatePorts(batchForm.deviceId, batchForm.count)
    ElMessage.success('批量创建成功')
    batchDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('创建失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchDevices()
  fetchData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
