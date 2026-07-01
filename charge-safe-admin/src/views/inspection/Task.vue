<template>
  <div class="task-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>巡检任务</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>创建任务
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="inspectorName" label="巡检人员" />
        <el-table-column prop="planStartTime" label="计划开始" width="180" />
        <el-table-column prop="planEndTime" label="计划结束" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" link @click="handleStart(row)">开始巡检</el-button>
            <el-button v-if="row.status === 'IN_PROGRESS' || row.status === 'ABNORMAL'" type="success" link @click="handleComplete(row)">完成</el-button>
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

    <!-- 创建任务对话框 -->
    <el-dialog v-model="dialogVisible" title="创建巡检任务" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="社区" prop="communityId">
          <el-select v-model="form.communityId" placeholder="选择社区" style="width: 100%">
            <el-option v-for="item in communities" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="巡检人员" prop="inspectorId">
          <el-select v-model="form.inspectorId" placeholder="选择巡检人员" style="width: 100%">
            <el-option v-for="item in inspectors" :key="item.id" :label="item.nickname" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务描述" prop="taskDesc">
          <el-input v-model="form.taskDesc" type="textarea" placeholder="请输入任务描述" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInspectionTasks, createInspectionTask, startInspectionTask, completeInspectionTask } from '@/api/inspection'
import { getCommunityList } from '@/api/community'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)
const communities = ref([])
const inspectors = ref([
  { id: 4, nickname: '巡检人员王五' }
])

const queryParams = reactive({
  page: 1,
  size: 10
})

const form = reactive({
  communityId: null,
  taskName: '',
  inspectorId: null,
  taskDesc: ''
})

const rules = {
  communityId: [{ required: true, message: '请选择社区', trigger: 'change' }],
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  inspectorId: [{ required: true, message: '请选择巡检人员', trigger: 'change' }]
}

const getStatusType = (status) => {
  const map = { 'PENDING': 'info', 'IN_PROGRESS': 'warning', 'COMPLETED': 'success', 'ABNORMAL': 'danger', 'OVERDUE': 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待执行', 'IN_PROGRESS': '执行中', 'COMPLETED': '已完成', 'ABNORMAL': '发现异常', 'OVERDUE': '已逾期' }
  return map[status] || status
}

const fetchCommunities = async () => {
  try {
    const res = await getCommunityList()
    if (res.code === 200) communities.value = res.data
  } catch (error) {}
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getInspectionTasks(queryParams)
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

const handleAdd = () => {
  form.communityId = null
  form.taskName = ''
  form.inspectorId = null
  form.taskDesc = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    await createInspectionTask(form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('创建失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleStart = async (row) => {
  try {
    await ElMessageBox.confirm('确定要开始巡检吗？', '提示', { type: 'warning' })
    await startInspectionTask(row.id)
    ElMessage.success('已开始巡检')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('操作失败:', error)
  }
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要完成巡检吗？', '提示', { type: 'warning' })
    await completeInspectionTask(row.id)
    ElMessage.success('巡检已完成')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('操作失败:', error)
  }
}

onMounted(() => {
  fetchCommunities()
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
