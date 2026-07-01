<template>
  <div class="report-page">
    <el-card>
      <template #header>
        <span>报修记录</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="reporterName" label="报修人" />
        <el-table-column prop="deviceName" label="设备" />
        <el-table-column prop="faultType" label="故障类型" width="100" />
        <el-table-column prop="faultDesc" label="故障描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="报修时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" link @click="handleDispatch(row)">派单</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 派单对话框 -->
    <el-dialog v-model="dispatchDialogVisible" title="派发工单" width="500px">
      <el-form ref="dispatchFormRef" :model="dispatchForm" :rules="dispatchRules" label-width="80px">
        <el-form-item label="维修人员" prop="assigneeId">
          <el-select v-model="dispatchForm.assigneeId" placeholder="选择维修人员" style="width: 100%">
            <el-option v-for="item in maintainers" :key="item.id" :label="item.nickname" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修要求" prop="repairRequirement">
          <el-input v-model="dispatchForm.repairRequirement" type="textarea" placeholder="请输入维修要求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleDispatchSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRepairReports } from '@/api/repair'
import { dispatchTicket } from '@/api/repair'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dispatchDialogVisible = ref(false)
const dispatchFormRef = ref(null)
const maintainers = ref([])

const queryParams = reactive({
  page: 1,
  size: 10
})

const dispatchForm = reactive({
  reportId: null,
  assigneeId: null,
  repairRequirement: ''
})

const dispatchRules = {
  assigneeId: [{ required: true, message: '请选择维修人员', trigger: 'change' }]
}

const getStatusType = (status) => {
  const map = { 'PENDING': 'warning', 'CONFIRMED': 'primary', 'DISPATCHED': 'success', 'REJECTED': 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待处理', 'CONFIRMED': '已确认', 'DISPATCHED': '已派单', 'REJECTED': '已驳回' }
  return map[status] || status
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRepairReports(queryParams)
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

const handleDispatch = (row) => {
  dispatchForm.reportId = row.id
  dispatchForm.assigneeId = null
  dispatchForm.repairRequirement = ''
  dispatchDialogVisible.value = true
}

const handleDispatchSubmit = async () => {
  try {
    await dispatchFormRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    await dispatchTicket(dispatchForm)
    ElMessage.success('派单成功')
    dispatchDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('派单失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => fetchData())
</script>
