<template>
  <div class="task-page">
    <!-- 页面标题 -->
    <CsPageHeader title="巡检任务" description="创建和派发日常安全巡检任务，并跟踪记录巡检人员的执行进度。" />

    <!-- 数据表格 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="fetchData">
        <template #left>
          <el-button v-permission="'inspection-task:view'" type="primary" icon="Plus" @click="handleAdd">创建任务</el-button>
        </template>
      </CsTableToolbar>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="inspectorName" label="巡检人员" width="120" />
        <el-table-column prop="planStartTime" label="计划开始" width="180" />
        <el-table-column prop="planEndTime" label="计划结束" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <CsStatusTag :status="row.status" module="inspection_task" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <CsConfirmButton
              v-if="row.status === 'PENDING'"
              v-permission="'inspection-task:view'"
              title="确定要对该任务开始巡检吗？"
              buttonType="primary"
              type="primary"
              @confirm="handleStart(row)"
            >
              开始巡检
            </CsConfirmButton>
            <CsConfirmButton
              v-if="row.status === 'IN_PROGRESS' || row.status === 'ABNORMAL'"
              v-permission="'inspection-task:view'"
              title="确定确认结束并完成本项巡检任务吗？"
              buttonType="success"
              type="success"
              @confirm="handleComplete(row)"
            >
              完成
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
            <el-option v-for="item in inspectors" :key="item.id" :label="item.nickname || item.username" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务描述" prop="taskDesc">
          <el-input v-model="form.taskDesc" type="textarea" :rows="3" placeholder="请输入任务具体描述或要求" />
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
import { ElMessage } from 'element-plus'
import { getInspectionTasks, createInspectionTask, startInspectionTask, completeInspectionTask } from '@/api/inspection'
import { getCommunityList } from '@/api/community'
import { getUsers } from '@/api/system'
import CsPageHeader from '@/components/CsPageHeader.vue'
import CsTableToolbar from '@/components/CsTableToolbar.vue'
import CsStatusTag from '@/components/CsStatusTag.vue'
import CsConfirmButton from '@/components/CsConfirmButton.vue'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)
const communities = ref([])
const inspectors = ref([])

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

const fetchCommunities = async () => {
  try {
    const res = await getCommunityList()
    if (res.code === 200) communities.value = res.data
  } catch (error) {
    console.error('获取社区失败:', error)
  }
}

const fetchInspectors = async () => {
  try {
    const res = await getUsers({ roleCode: 'INSPECTOR', page: 1, size: 100 })
    if (res.code === 200) {
      inspectors.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取巡检人员失败:', error)
  }
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
    await startInspectionTask(row.id)
    ElMessage.success('已开始巡检')
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleComplete = async (row) => {
  try {
    await completeInspectionTask(row.id)
    ElMessage.success('巡检已完成')
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => {
  fetchCommunities()
  fetchInspectors()
  fetchData()
})
</script>

<style scoped>
</style>
