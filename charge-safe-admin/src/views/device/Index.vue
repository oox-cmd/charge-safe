<template>
  <div class="device-page">
    <!-- 页面标题 -->
    <CsPageHeader title="充电设备管理" description="管理接入平台的充电桩设备，维护设备编码和所属车棚。" />

    <!-- 搜索面板 -->
    <CsSearchPanel :model="queryParams" @search="fetchData" @reset="resetQuery">
      <el-form-item label="所属车棚">
        <el-select v-model="queryParams.parkingAreaId" placeholder="选择车棚" clearable style="width: 220px">
          <el-option v-for="item in parkingAreas" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
    </CsSearchPanel>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="fetchData">
        <template #left>
          <el-button v-permission="'device:list:view'" type="primary" icon="Plus" @click="handleAdd">新增设备</el-button>
        </template>
      </CsTableToolbar>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="deviceCode" label="设备编码" width="150" />
        <el-table-column prop="deviceName" label="设备名称" />
        <el-table-column prop="parkingAreaName" label="所属车棚" />
        <el-table-column prop="portCount" label="端口数" width="100" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'">
              {{ row.status === 1 ? '启用' : row.status === 2 ? '故障' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'device:list:view'" type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link icon="Connection" @click="handleViewPorts(row)">查看端口</el-button>
            <CsConfirmButton
              v-permission="'device:list:view'"
              :title="`确定要${row.status === 1 ? '停用' : '启用'}设备 【${row.deviceName}】 吗？`"
              :buttonType="row.status === 1 ? 'warning' : 'success'"
              :type="row.status === 1 ? 'warning' : 'success'"
              @confirm="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </CsConfirmButton>
          </template>
        </el-table-column>
      </el-table>

      <CsPagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        @change="fetchData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="车棚" prop="parkingAreaId">
          <el-select v-model="form.parkingAreaId" placeholder="选择车棚" style="width: 100%">
            <el-option v-for="item in parkingAreas" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="编码" prop="deviceCode">
          <el-input v-model="form.deviceCode" placeholder="请输入设备编码" />
        </el-form-item>
        <el-form-item label="名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="类型" prop="deviceType">
          <el-input v-model="form.deviceType" placeholder="请输入设备类型，例如：交流慢充桩、直流快充桩" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import CsPagination from '@/components/CsPagination.vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDevices, createDevice, updateDevice, updateDeviceStatus } from '@/api/device'
import { getParkingAreaList } from '@/api/community'
import CsPageHeader from '@/components/CsPageHeader.vue'
import CsSearchPanel from '@/components/CsSearchPanel.vue'
import CsTableToolbar from '@/components/CsTableToolbar.vue'
import CsConfirmButton from '@/components/CsConfirmButton.vue'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const parkingAreas = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  parkingAreaId: null
})

const form = reactive({
  id: null,
  parkingAreaId: null,
  deviceCode: '',
  deviceName: '',
  deviceType: ''
})

const rules = {
  parkingAreaId: [{ required: true, message: '请选择车棚', trigger: 'change' }],
  deviceCode: [{ required: true, message: '请输入设备编码', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

const fetchParkingAreas = async () => {
  try {
    const res = await getParkingAreaList()
    if (res.code === 200) {
      parkingAreas.value = res.data
    }
  } catch (error) {
    console.error('获取车棚列表失败:', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDevices(queryParams)
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

const resetQuery = () => {
  queryParams.parkingAreaId = null
  queryParams.page = 1
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增设备'
  form.id = null
  form.parkingAreaId = null
  form.deviceCode = ''
  form.deviceName = ''
  form.deviceType = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑设备'
  form.id = row.id
  form.parkingAreaId = row.parkingAreaId
  form.deviceCode = row.deviceCode
  form.deviceName = row.deviceName
  form.deviceType = row.deviceType
  dialogVisible.value = true
}

const handleViewPorts = (row) => {
  router.push({ path: '/ops/port', query: { deviceId: row.id } })
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    if (form.id) {
      await updateDevice(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createDevice(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '停用'

  try {
    await updateDeviceStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => {
  fetchParkingAreas()
  fetchData()
})
</script>

<style scoped>
</style>
