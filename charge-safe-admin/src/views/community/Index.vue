<template>
  <div class="community-page">
    <!-- 页面标题 -->
    <CsPageHeader title="社区管理" description="管理接入 ChargeSafe 平台的社区资源及联系方式。" />

    <!-- 数据表格 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="fetchData">
        <template #left>
          <el-button v-permission="'community:list:view'" type="primary" icon="Plus" @click="handleAdd">新增社区</el-button>
        </template>
      </CsTableToolbar>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="社区名称" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="150" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'community:list:view'" type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <CsConfirmButton
              v-permission="'community:list:view'"
              :title="`确定要${row.status === 1 ? '停用' : '启用'}社区 【${row.name}】 吗？`"
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
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入社区名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
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
import { getCommunities, createCommunity, updateCommunity, updateCommunityStatus } from '@/api/community'
import CsPageHeader from '@/components/CsPageHeader.vue'
import CsTableToolbar from '@/components/CsTableToolbar.vue'
import CsConfirmButton from '@/components/CsConfirmButton.vue'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryParams = reactive({
  page: 1,
  size: 10
})

const form = reactive({
  id: null,
  name: '',
  address: '',
  contactName: '',
  contactPhone: ''
})

const rules = {
  name: [{ required: true, message: '请输入社区名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCommunities(queryParams)
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
  dialogTitle.value = '新增社区'
  form.id = null
  form.name = ''
  form.address = ''
  form.contactName = ''
  form.contactPhone = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑社区'
  form.id = row.id
  form.name = row.name
  form.address = row.address
  form.contactName = row.contactName
  form.contactPhone = row.contactPhone
  dialogVisible.value = true
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
      await updateCommunity(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createCommunity(form)
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
    await updateCommunityStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
</style>
