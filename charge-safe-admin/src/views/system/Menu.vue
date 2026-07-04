<template>
  <div class="menu-page">
    <!-- 查询表单 -->
    <el-card class="query-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="菜单名称">
          <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable @keyup.enter="fetchData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增菜单
          </el-button>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
        stripe
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="200" />
        <el-table-column prop="menuType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'DIRECTORY'" type="primary">目录</el-tag>
            <el-tag v-else-if="row.menuType === 'MENU'" type="success">菜单</el-tag>
            <el-tag v-else-if="row.menuType === 'BUTTON'" type="warning">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" width="150" />
        <el-table-column prop="component" label="组件" width="150" />
        <el-table-column prop="permission" label="权限标识" width="150" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleAddChild(row)">新增子菜单</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeOptions"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="请选择上级菜单（留空为顶级）"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="DIRECTORY">目录</el-radio>
            <el-radio value="MENU">菜单</el-radio>
            <el-radio value="BUTTON">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'BUTTON'" label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'MENU'" label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'BUTTON'" label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="如 system:user:add" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { getMenus, getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/system'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const menuTreeOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryParams = reactive({
  menuName: '',
  status: null
})

const form = reactive({
  id: null,
  parentId: 0,
  menuType: 'MENU',
  menuName: '',
  path: '',
  component: '',
  permission: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    if (res.code === 200) {
      // 过滤启用的菜单
      tableData.value = filterMenuTree(res.data, queryParams)
    }
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

const filterMenuTree = (tree, params) => {
  if (!tree) return []
  return tree.filter(node => {
    // 名称过滤
    if (params.menuName && !node.menuName.includes(params.menuName)) {
      // 检查子节点是否匹配
      const children = filterMenuTree(node.children, params)
      if (children.length > 0) {
        node.children = children
        return true
      }
      return false
    }
    // 状态过滤
    if (params.status !== null && params.status !== undefined && node.status !== params.status) {
      return false
    }
    // 递归过滤子节点
    if (node.children && node.children.length > 0) {
      node.children = filterMenuTree(node.children, params)
    }
    return true
  })
}

const fetchMenuTreeOptions = async () => {
  try {
    const res = await getMenuTree()
    if (res.code === 200) {
      // 添加虚拟根节点
      menuTreeOptions.value = [{ id: 0, menuName: '顶级菜单', children: res.data }]
    }
  } catch (error) {
    console.error('获取菜单树失败:', error)
  }
}

const resetQuery = () => {
  queryParams.menuName = ''
  queryParams.status = null
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增菜单'
  form.id = null
  form.parentId = 0
  form.menuType = 'MENU'
  form.menuName = ''
  form.path = ''
  form.component = ''
  form.permission = ''
  form.sortOrder = 0
  form.status = 1
  fetchMenuTreeOptions()
  dialogVisible.value = true
}

const handleAddChild = (row) => {
  dialogTitle.value = '新增子菜单'
  form.id = null
  form.parentId = row.id
  form.menuType = row.menuType === 'DIRECTORY' ? 'MENU' : 'BUTTON'
  form.menuName = ''
  form.path = ''
  form.component = ''
  form.permission = ''
  form.sortOrder = 0
  form.status = 1
  fetchMenuTreeOptions()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑菜单'
  form.id = row.id
  form.parentId = row.parentId
  form.menuType = row.menuType
  form.menuName = row.menuName
  form.path = row.path
  form.component = row.component
  form.permission = row.permission
  form.sortOrder = row.sortOrder
  form.status = row.status
  fetchMenuTreeOptions()
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
      await updateMenu(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createMenu(form)
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
    await ElMessageBox.confirm(`确定要删除菜单 "${row.menuName}" 吗？删除后子菜单也会被删除。`, '提示', { type: 'warning' })
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.query-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
