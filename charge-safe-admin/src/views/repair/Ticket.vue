<template>
  <div class="ticket-page">
    <el-card>
      <template #header>
        <span>维修工单</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ticketNo" label="工单编号" width="150" />
        <el-table-column prop="faultDesc" label="故障描述" show-overflow-tooltip />
        <el-table-column prop="assigneeName" label="维修人员" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button v-if="row.status === 'DISPATCHED'" type="primary" link @click="handleAccept(row)">接单</el-button>
            <el-button v-if="row.status === 'PROCESSING'" type="success" link @click="handleFinish(row)">完成维修</el-button>
            <el-button v-if="row.status === 'WAITING_ACCEPTANCE'" type="warning" link @click="handleAcceptRepair(row)">验收通过</el-button>
            <el-button v-if="row.status === 'WAITING_ACCEPTANCE'" type="danger" link @click="handleReject(row)">驳回</el-button>
            <el-button v-if="row.status === 'FINISHED'" type="info" link @click="handleClose(row)">关闭</el-button>
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

    <!-- 完成维修对话框 -->
    <el-dialog v-model="finishDialogVisible" title="完成维修" width="500px">
      <el-form ref="finishFormRef" :model="finishForm" :rules="finishRules" label-width="80px">
        <el-form-item label="维修结果" prop="repairResult">
          <el-input v-model="finishForm.repairResult" type="textarea" placeholder="请输入维修结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleFinishSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRepairTickets, acceptTicket, finishTicket, acceptTicketByProperty, rejectTicket, closeTicket } from '@/api/repair'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const finishDialogVisible = ref(false)
const finishFormRef = ref(null)

const queryParams = reactive({
  page: 1,
  size: 10
})

const finishForm = reactive({
  ticketId: null,
  repairResult: ''
})

const finishRules = {
  repairResult: [{ required: true, message: '请输入维修结果', trigger: 'blur' }]
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'info', 'DISPATCHED': 'warning', 'PROCESSING': 'primary',
    'WAITING_ACCEPTANCE': 'warning', 'FINISHED': 'success', 'CLOSED': 'info', 'REJECTED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待受理', 'DISPATCHED': '已派单', 'PROCESSING': '处理中',
    'WAITING_ACCEPTANCE': '待验收', 'FINISHED': '已完成', 'CLOSED': '已关闭', 'REJECTED': '已驳回'
  }
  return map[status] || status
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRepairTickets(queryParams)
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

const handleAccept = async (row) => {
  try {
    await ElMessageBox.confirm('确定要接单吗？', '提示', { type: 'warning' })
    await acceptTicket(row.id)
    ElMessage.success('接单成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('操作失败:', error)
  }
}

const handleFinish = (row) => {
  finishForm.ticketId = row.id
  finishForm.repairResult = ''
  finishDialogVisible.value = true
}

const handleFinishSubmit = async () => {
  try {
    await finishFormRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    await finishTicket(finishForm.ticketId, { repairResult: finishForm.repairResult })
    ElMessage.success('维修完成')
    finishDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleAcceptRepair = async (row) => {
  try {
    await ElMessageBox.confirm('确定验收通过吗？', '提示', { type: 'warning' })
    await acceptTicketByProperty(row.id)
    ElMessage.success('验收通过')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('操作失败:', error)
  }
}

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回工单', {
      inputType: 'textarea',
      inputValidator: (val) => !!val || '请输入驳回原因'
    })
    await rejectTicket(row.id, value)
    ElMessage.success('已驳回')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('操作失败:', error)
  }
}

const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm('确定要关闭工单吗？', '提示', { type: 'warning' })
    await closeTicket(row.id)
    ElMessage.success('工单已关闭')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('操作失败:', error)
  }
}

onMounted(() => fetchData())
</script>
