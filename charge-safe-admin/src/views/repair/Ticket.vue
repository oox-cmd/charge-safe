<template>
  <div class="ticket-page">
    <!-- 页面标题 -->
    <CsPageHeader title="维修工单" description="跟踪和处理设备维修任务的生命周期状态流转。" />

    <!-- 数据表格 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="fetchData" />

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ticketNo" label="工单编号" width="180" />
        <el-table-column prop="faultDesc" label="故障描述" show-overflow-tooltip />
        <el-table-column prop="assigneeName" label="维修人员" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <CsStatusTag :status="row.status" module="repair_ticket" />
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <CsConfirmButton
              v-if="row.status === 'DISPATCHED'"
              v-permission="'repair:ticket:accept'"
              title="确定要对该工单进行接单吗？"
              buttonType="primary"
              type="primary"
              @confirm="handleAccept(row)"
            >
              接单
            </CsConfirmButton>
            <el-button v-if="row.status === 'PROCESSING'" v-permission="'repair:ticket:finish'" type="success" link icon="Checked" @click="handleFinish(row)">完成维修</el-button>
            <CsConfirmButton
              v-if="row.status === 'WAITING_ACCEPTANCE'"
              v-permission="'repair:ticket:verify'"
              title="确定确认完成验收并关闭此工单吗？"
              buttonType="success"
              type="success"
              @confirm="handleAcceptRepair(row)"
            >
              验收通过
            </CsConfirmButton>
            <el-button v-if="row.status === 'WAITING_ACCEPTANCE'" v-permission="'repair:ticket:reject'" type="danger" link icon="Close" @click="handleReject(row)">驳回</el-button>
            <CsConfirmButton
              v-if="row.status === 'FINISHED'"
              v-permission="'repair:ticket:close'"
              title="确定关闭此工单吗？"
              buttonType="info"
              type="info"
              @confirm="handleClose(row)"
            >
              关闭
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

    <!-- 完成维修对话框 -->
    <el-dialog v-model="finishDialogVisible" title="完成维修" width="500px">
      <el-form ref="finishFormRef" :model="finishForm" :rules="finishRules" label-width="80px">
        <el-form-item label="维修结果" prop="repairResult">
          <el-input v-model="finishForm.repairResult" type="textarea" :rows="3" placeholder="请输入维修结果" />
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
import CsPagination from '@/components/CsPagination.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRepairTickets, acceptTicket, finishTicket, acceptTicketByProperty, rejectTicket, closeTicket } from '@/api/repair'
import CsPageHeader from '@/components/CsPageHeader.vue'
import CsTableToolbar from '@/components/CsTableToolbar.vue'
import CsStatusTag from '@/components/CsStatusTag.vue'
import CsConfirmButton from '@/components/CsConfirmButton.vue'

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
    await acceptTicket(row.id)
    ElMessage.success('接单成功')
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
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
    await acceptTicketByProperty(row.id)
    ElMessage.success('验收通过')
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
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
    await closeTicket(row.id)
    ElMessage.success('工单已关闭')
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
</style>
