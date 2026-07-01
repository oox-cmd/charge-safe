<template>
  <div class="parking-area-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>车棚管理</span>
          <div>
            <el-select v-model="queryParams.communityId" placeholder="选择社区" clearable style="margin-right: 10px; width: 200px">
              <el-option v-for="item in communities" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增车棚
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="车棚名称" />
        <el-table-column prop="communityName" label="所属社区" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="portCount" label="端口数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="社区" prop="communityId">
          <el-select v-model="form.communityId" placeholder="选择社区" style="width: 100%">
            <el-option v-for="item in communities" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入车棚名称" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入位置描述" />
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
import { getParkingAreas, createParkingArea, updateParkingArea, deleteParkingArea } from '@/api/community'
import { getCommunityList } from '@/api/community'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const communities = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  communityId: null
})

const form = reactive({
  id: null,
  communityId: null,
  name: '',
  location: ''
})

const rules = {
  communityId: [{ required: true, message: '请选择社区', trigger: 'change' }],
  name: [{ required: true, message: '请输入车棚名称', trigger: 'blur' }]
}

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

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getParkingAreas(queryParams)
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
  dialogTitle.value = '新增车棚'
  form.id = null
  form.communityId = null
  form.name = ''
  form.location = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑车棚'
  form.id = row.id
  form.communityId = row.communityId
  form.name = row.name
  form.location = row.location
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
      await updateParkingArea(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createParkingArea(form)
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该车棚吗？', '提示', { type: 'warning' })
    await deleteParkingArea(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
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
