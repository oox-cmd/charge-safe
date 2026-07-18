<template>
  <div class="device-category-container">
    <CsPageHeader title="设备分类" description="维护充电设备分类、适用功率和启停状态，用于充电设备建档。" />



    <!-- 搜索过滤面板 -->
    <CsSearchPanel :model="queryParams" @search="handleQuery" @reset="resetQuery">
      <el-form-item label="分类名称">
        <el-input v-model="queryParams.name" placeholder="请输入分类名称" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="选择状态" clearable style="width: 150px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
    </CsSearchPanel>

    <!-- 数据表格面板 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="handleQuery">
        <template #left>
          <el-button v-if="hasPermission('device:category:create')" type="primary" icon="Plus" @click="handleAdd">新增分类</el-button>
        </template>
      </CsTableToolbar>

      <el-table :data="filteredCategories" stripe style="width: 100%">
        <el-table-column prop="name" label="分类名称" width="180" />
        <el-table-column prop="code" label="分类编码" width="150" />
        <el-table-column prop="power" label="适用功率" width="120" />
        <el-table-column prop="description" label="描述说明" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-if="hasPermission('device:category:edit')"
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
            <el-tag v-else :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasPermission('device:category:edit')" link type="primary" icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="hasPermission('device:category:delete')" link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增设备分类' : '编辑设备分类'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：慢充交流桩、快充直流桩" />
        </el-form-item>
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="form.code" placeholder="例如：SLOW_AC、FAST_DC" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="适用功率" prop="power">
          <el-input v-model="form.power" placeholder="例如：7kW、60kW、120kW" />
        </el-form-item>
        <el-form-item label="描述说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入分类的适用场景和描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CsPageHeader from '@/components/CsPageHeader.vue'
import CsSearchPanel from '@/components/CsSearchPanel.vue'
import CsTableToolbar from '@/components/CsTableToolbar.vue'
import { usePermission } from '@/composables/usePermission'
import { getDeviceCategories, createDeviceCategory, updateDeviceCategory, deleteDeviceCategory } from '@/api/device'
import { onMounted } from 'vue'

const queryParams = ref({
  name: '',
  status: null
})

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const { hasPermission } = usePermission()

const categories = ref([])

const loadCategories = async () => {
  try {
    const res = await getDeviceCategories()
    if (res.code === 200) {
      categories.value = res.data
    } else {
      ElMessage.error(res.message || '获取分类失败')
    }
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadCategories()
})

const queryActive = ref({
  name: '',
  status: null
})

const filteredCategories = computed(() => {
  return categories.value.filter(item => {
    const nameMatch = !queryActive.value.name || item.name.includes(queryActive.value.name)
    const statusMatch = queryActive.value.status === null || item.status === queryActive.value.status
    return nameMatch && statusMatch
  })
})

const handleQuery = () => {
  queryActive.value = { ...queryParams.value }
  // 也可以在这里重新调用后端的分页查询，目前数据少，直接前端过滤也可以
}

const resetQuery = () => {
  queryParams.value = {
    name: '',
    status: null
  }
  queryActive.value = { ...queryParams.value }
}

// 表单定义
const form = ref({
  id: null,
  name: '',
  code: '',
  power: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入分类编码', trigger: 'blur' }],
  power: [{ required: true, message: '请输入适用功率', trigger: 'blur' }]
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    id: null,
    name: '',
    code: '',
    power: '',
    description: '',
    status: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleStatusChange = (row) => {
  const text = row.status === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${text}设备分类 "${row.name}" 吗？`, '系统确认', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await updateDeviceCategory(row.id, { ...row, status: row.status })
      if (res.code === 200) {
        ElMessage.success(`${text}成功`)
      } else {
        ElMessage.error(res.message || `${text}失败`)
        row.status = row.status === 1 ? 0 : 1
      }
    } catch (error) {
      console.error(error)
      row.status = row.status === 1 ? 0 : 1
    }
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除分类 "${row.name}" 吗？该操作不可恢复！`, '警示', {
    type: 'danger'
  }).then(async () => {
    try {
      const res = await deleteDeviceCategory(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadCategories()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  try {
    if (dialogType.value === 'add') {
      const res = await createDeviceCategory(form.value)
      if (res.code === 200) {
        ElMessage.success('新增分类成功')
        dialogVisible.value = false
        loadCategories()
      } else {
        ElMessage.error(res.message || '新增失败')
      }
    } else {
      const res = await updateDeviceCategory(form.value.id, form.value)
      if (res.code === 200) {
        ElMessage.success('更新分类成功')
        dialogVisible.value = false
        loadCategories()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    }
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.device-category-container {
  padding: 10px 0;
}

.mock-alert {
  margin-bottom: 16px;
}

</style>
