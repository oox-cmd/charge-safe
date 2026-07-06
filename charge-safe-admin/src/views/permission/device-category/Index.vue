<template>
  <div class="device-category-container">
    <CsPageHeader title="设备分类" description="维护充电设备分类、适用功率和启停状态，当前用于权限端基础配置演示。" />

    <el-alert
      title="当前设备分类为前端演示数据，尚未接入真实设备分类接口。"
      type="warning"
      show-icon
      :closable="false"
      class="mock-alert"
    />

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
          <el-button type="primary" icon="Plus" @click="handleAdd">新增分类</el-button>
        </template>
      </CsTableToolbar>

      <el-table :data="filteredCategories" stripe style="width: 100%">
        <el-table-column prop="name" label="分类名称" width="180" />
        <el-table-column prop="code" label="分类编码" width="150" />
        <el-table-column prop="power" label="适用功率" width="120" />
        <el-table-column prop="desc" label="描述说明" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="描述说明" prop="desc">
          <el-input v-model="form.desc" type="textarea" :rows="3" placeholder="请输入分类的适用场景和描述" />
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

const queryParams = ref({
  name: '',
  status: null
})

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)

// 初始模拟数据
const categories = ref([
  { id: 1, name: '家用慢充交流桩', code: 'SLOW_AC', power: '7kW', desc: '适用于居民小区夜间长时间充电，对电网负荷影响较小。', status: 1, createTime: '2026-07-01 10:00:00' },
  { id: 2, name: '公共快充直流桩', code: 'FAST_DC', power: '60kW', desc: '适用于公共停车场、车棚，满足中短时间快速补充电量需求。', status: 1, createTime: '2026-07-01 10:15:00' },
  { id: 3, name: '超级快充直流桩', code: 'SUPER_DC', power: '120kW', desc: '适用于高速服务区、公交总站等需要极速充电的专用场景。', status: 1, createTime: '2026-07-01 10:20:00' },
  { id: 4, name: '低功耗便携式插座', code: 'PORTABLE_SOCKET', power: '2.2kW', desc: '适用于零散露天车棚，仅提供基础低功率慢充，主要面向自行车。', status: 1, createTime: '2026-07-02 09:00:00' }
])

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
  desc: '',
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
    desc: '',
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
  }).then(() => {
    ElMessage.success(`${text}成功`)
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除分类 "${row.name}" 吗？该操作不可恢复！`, '警示', {
    type: 'danger'
  }).then(() => {
    categories.value = categories.value.filter(c => c.id !== row.id)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  if (dialogType.value === 'add') {
    // 校验编码是否唯一
    const exists = categories.value.some(c => c.code.toUpperCase() === form.value.code.toUpperCase())
    if (exists) {
      ElMessage.error('分类编码已存在，请使用其他编码！')
      return
    }

    const newId = categories.value.length > 0 ? Math.max(...categories.value.map(c => c.id)) + 1 : 1
    const newCategory = {
      ...form.value,
      code: form.value.code.toUpperCase(),
      id: newId,
      createTime: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    categories.value.push(newCategory)
    ElMessage.success('新增分类成功')
  } else {
    const index = categories.value.findIndex(c => c.id === form.value.id)
    if (index > -1) {
      categories.value[index] = { ...form.value }
      ElMessage.success('更新分类成功')
    }
  }
  dialogVisible.value = false
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
