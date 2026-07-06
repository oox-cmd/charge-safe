<template>
  <div class="department-container">
    <CsPageHeader title="部门管理" description="维护平台组织架构和部门基础信息，当前用于权限端组织管理演示。" />

    <el-alert
      title="当前部门组织架构为前端演示数据，尚未接入真实部门管理接口。"
      type="warning"
      show-icon
      :closable="false"
      class="mock-alert"
    />

    <el-row :gutter="20">
      <!-- 左侧部门树 -->
      <el-col :span="6">
        <el-card class="box-card" shadow="never" style="min-height: 780px">
          <template #header>
            <div class="card-header">
              <span>部门组织架构</span>
              <el-button type="primary" size="small" circle icon="Plus" @click="handleAddDept(null)" />
            </div>
          </template>
          <el-input
            v-model="filterText"
            placeholder="输入关键字进行过滤"
            clearable
            prefix-icon="Search"
            style="margin-bottom: 15px"
          />
          <el-tree
            ref="treeRef"
            :data="deptTree"
            :props="defaultProps"
            node-key="id"
            default-expand-all
            :filter-node-method="filterNode"
            highlight-current
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ node.label }}</span>
                <span class="tree-node-actions">
                  <el-button link type="primary" size="small" icon="Plus" @click.stop="handleAddDept(data)" />
                  <el-button link type="primary" size="small" icon="Edit" @click.stop="handleEditDept(data)" />
                  <el-button
                    link
                    type="danger"
                    size="small"
                    icon="Delete"
                    @click.stop="handleDeleteDept(data)"
                    v-if="data.id !== 1"
                  />
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧部门列表 -->
      <el-col :span="18">
        <el-card class="box-card" shadow="never" style="min-height: 780px">
          <template #header>
            <div class="card-header">
              <span>{{ selectedNodeName }} - 下级部门</span>
              <el-button type="primary" icon="Plus" @click="handleAddDept(selectedNode)">新增子部门</el-button>
            </div>
          </template>

          <el-table :data="tableData" stripe style="width: 100%">
            <el-table-column prop="name" label="部门名称" />
            <el-table-column prop="code" label="部门编码" />
            <el-table-column prop="leader" label="负责人" width="120" />
            <el-table-column prop="phone" label="联系电话" width="150" />
            <el-table-column prop="sort" label="排序" width="80" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" icon="Edit" @click="handleEditDept(row)">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDeleteDept(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增部门' : '编辑部门'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTree"
            :props="treeSelectProps"
            placeholder="选择上级部门"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="显示排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
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
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CsPageHeader from '@/components/CsPageHeader.vue'

const filterText = ref('')
const treeRef = ref(null)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)

const selectedNode = ref(null)

// 初始模拟部门数据
const departments = ref([
  { id: 1, parentId: null, name: 'ChargeSafe集团', code: 'CS_GROUP', leader: '张健', phone: '18888888888', sort: 1, status: 1, createTime: '2026-07-01 10:00:00' },
  { id: 2, parentId: 1, name: '系统研发部', code: 'DEPT_RD', leader: '金昊然', phone: '13900000001', sort: 1, status: 1, createTime: '2026-07-01 10:30:00' },
  { id: 3, parentId: 1, name: '项目运营部', code: 'DEPT_OPS', leader: '吴诗颖', phone: '13900000002', sort: 2, status: 1, createTime: '2026-07-01 10:35:00' },
  { id: 4, parentId: 1, name: '财务管理部', code: 'DEPT_FI', leader: '王小红', phone: '13900000003', sort: 3, status: 1, createTime: '2026-07-01 10:40:00' },
  { id: 5, parentId: 3, name: '华北区域运营组', code: 'OPS_NORTH', leader: '李华', phone: '13910000001', sort: 1, status: 1, createTime: '2026-07-01 11:00:00' },
  { id: 6, parentId: 3, name: '华东区域运营组', code: 'OPS_EAST', leader: '张明', phone: '13920000002', sort: 2, status: 1, createTime: '2026-07-01 11:05:00' },
  { id: 7, parentId: 3, name: '华南区域运营组', code: 'OPS_SOUTH', leader: '赵磊', phone: '13930000003', sort: 3, status: 1, createTime: '2026-07-01 11:10:00' }
])

const defaultProps = {
  children: 'children',
  label: 'name'
}

const treeSelectProps = {
  value: 'id',
  label: 'name',
  children: 'children'
}

// 转换扁平数组为树形结构
const buildTree = (list, parentId = null) => {
  return list
    .filter(item => item.parentId === parentId)
    .map(item => {
      const children = buildTree(list, item.id)
      return {
        ...item,
        children: children.length > 0 ? children : undefined
      }
    })
    .sort((a, b) => a.sort - b.sort)
}

const deptTree = computed(() => {
  return buildTree(departments.value)
})

// 监听关键字搜索树节点
watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

const filterNode = (value, data) => {
  if (!value) return true
  return data.name.includes(value)
}

// 获取当前选中部门的名称
const selectedNodeName = computed(() => {
  if (!selectedNode.value) return 'ChargeSafe集团'
  return selectedNode.value.name
})

// 获取选中部门的子部门列表
const tableData = computed(() => {
  const pid = selectedNode.value ? selectedNode.value.id : 1
  return departments.value.filter(d => d.parentId === pid).sort((a, b) => a.sort - b.sort)
})

const handleNodeClick = (data) => {
  selectedNode.value = data
}

// 表单及校验
const form = ref({
  id: null,
  parentId: null,
  name: '',
  code: '',
  leader: '',
  phone: '',
  sort: 1,
  status: 1
})

const rules = {
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }],
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  leader: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

const handleAddDept = (parent) => {
  dialogType.value = 'add'
  form.value = {
    id: null,
    parentId: parent ? parent.id : 1,
    name: '',
    code: '',
    leader: '',
    phone: '',
    sort: tableData.value.length + 1,
    status: 1
  }
  dialogVisible.value = true
}

const handleEditDept = (row) => {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDeleteDept = (row) => {
  ElMessageBox.confirm(`确定要删除部门 "${row.name}" 吗？`, '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    // 检查是否有子部门
    const hasChildren = departments.value.some(d => d.parentId === row.id)
    if (hasChildren) {
      ElMessage.error('该部门下存有子部门，无法直接删除！')
      return
    }
    
    departments.value = departments.value.filter(d => d.id !== row.id)
    ElMessage.success('删除成功')
    if (selectedNode.value?.id === row.id) {
      selectedNode.value = null
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  if (dialogType.value === 'add') {
    const newId = departments.value.length > 0 ? Math.max(...departments.value.map(d => d.id)) + 1 : 1
    const newDept = {
      ...form.value,
      id: newId,
      createTime: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    departments.value.push(newDept)
    ElMessage.success('新增部门成功')
  } else {
    const index = departments.value.findIndex(d => d.id === form.value.id)
    if (index > -1) {
      departments.value[index] = { ...form.value }
      ElMessage.success('更新部门成功')
    }
  }
  dialogVisible.value = false
}
</script>

<style scoped>
.department-container {
  padding: 10px 0;
}

.mock-alert {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.tree-node-actions {
  display: none;
}

.custom-tree-node:hover .tree-node-actions {
  display: inline-flex;
  gap: 4px;
}

:deep(.el-tree-node__content) {
  height: 32px;
}
</style>
