<template>
  <div class="hazard-page">
    <!-- 页面标题 -->
    <CsPageHeader title="安全隐患" description="记录巡检过程中发现的安全隐患点，对其进行整改、复查及闭环关闭管理。" />

    <!-- 数据表格 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="fetchData" />

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="hazardType" label="隐患类型" width="120" />
        <el-table-column prop="severity" label="严重程度" width="120" align="center">
          <template #default="{ row }">
            <CsStatusTag :status="row.severity" module="hazard_severity" />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="隐患描述" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="上报人" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <CsStatusTag :status="row.status" module="inspection_hazard" />
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="上报时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" v-permission="'hazard:view'" type="warning" link icon="Tools" @click="handleRectify(row)">整改</el-button>
            <el-button v-if="row.status === 'WAITING_REVIEW'" v-permission="'hazard:view'" type="primary" link icon="Search" @click="handleReview(row)">复查</el-button>
            <CsConfirmButton
              v-if="row.status === 'REVIEW_PASSED'"
              v-permission="'hazard:view'"
              title="确认关闭该安全隐患吗？"
              buttonType="success"
              type="success"
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

    <!-- 整改对话框 -->
    <el-dialog v-model="rectifyDialogVisible" title="隐患整改" width="500px">
      <el-form ref="rectifyFormRef" :model="rectifyForm" :rules="rectifyRules" label-width="80px">
        <el-form-item label="整改情况" prop="rectifyDesc">
          <el-input v-model="rectifyForm.rectifyDesc" type="textarea" :rows="3" placeholder="请输入整改情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rectifyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleRectifySubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 复查对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="隐患复查" width="500px">
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="80px">
        <el-form-item label="复查结果" prop="reviewResult">
          <el-radio-group v-model="reviewForm.reviewResult">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="FAIL">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="复查备注" prop="reviewRemark">
          <el-input v-model="reviewForm.reviewRemark" type="textarea" :rows="3" placeholder="请输入复查备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleReviewSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import CsPagination from '@/components/CsPagination.vue'
import { ElMessage } from 'element-plus'
import { getHazards, rectifyHazard, reviewHazard, closeHazard } from '@/api/inspection'
import CsPageHeader from '@/components/CsPageHeader.vue'
import CsTableToolbar from '@/components/CsTableToolbar.vue'
import CsStatusTag from '@/components/CsStatusTag.vue'
import CsConfirmButton from '@/components/CsConfirmButton.vue'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const rectifyDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const rectifyFormRef = ref(null)
const reviewFormRef = ref(null)

const queryParams = reactive({
  page: 1,
  size: 10
})

const rectifyForm = reactive({
  hazardId: null,
  rectifyDesc: ''
})

const reviewForm = reactive({
  hazardId: null,
  reviewResult: 'PASS',
  reviewRemark: ''
})

const rectifyRules = {
  rectifyDesc: [{ required: true, message: '请输入整改情况', trigger: 'blur' }]
}

const reviewRules = {
  reviewResult: [{ required: true, message: '请选择复查结果', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getHazards(queryParams)
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

const handleRectify = (row) => {
  rectifyForm.hazardId = row.id
  rectifyForm.rectifyDesc = ''
  rectifyDialogVisible.value = true
}

const handleRectifySubmit = async () => {
  try { await rectifyFormRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    await rectifyHazard(rectifyForm.hazardId, { rectifyDesc: rectifyForm.rectifyDesc })
    ElMessage.success('整改提交成功')
    rectifyDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleReview = (row) => {
  reviewForm.hazardId = row.id
  reviewForm.reviewResult = 'PASS'
  reviewForm.reviewRemark = ''
  reviewDialogVisible.value = true
}

const handleReviewSubmit = async () => {
  try { await reviewFormRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    await reviewHazard(reviewForm.hazardId, reviewForm)
    ElMessage.success('复查完成')
    reviewDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleClose = async (row) => {
  try {
    await closeHazard(row.id)
    ElMessage.success('隐患已关闭')
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
</style>
