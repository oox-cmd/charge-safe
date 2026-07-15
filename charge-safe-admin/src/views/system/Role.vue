<template>
  <div class="role-page">
    <!-- 页面标题 -->
    <CsPageHeader title="角色管理" description="管理系统安全控制角色，为角色分配对应的系统菜单和按钮访问权限。" />

    <!-- 搜索面板 -->
    <CsSearchPanel :model="queryParams" @search="fetchData" @reset="resetQuery">
      <el-form-item label="角色名称">
        <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable @keyup.enter="fetchData" />
      </el-form-item>
      <el-form-item label="角色编码">
        <el-input v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable @keyup.enter="fetchData" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 150px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
    </CsSearchPanel>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="fetchData">
        <template #left>
          <el-button v-if="hasPermission('system:role:create')" type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
        </template>
      </CsTableToolbar>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="roleName" label="角色名称" width="180" />
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasPermission('system:role:update')" type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="hasPermission('system:role:assign-menu')" type="primary" link icon="Setting" @click="handleAssignMenus(row)">菜单</el-button>
            <CsConfirmButton
              v-if="hasPermission('system:role:delete')"
              :title="`确定要删除角色 【${row.roleName}】 吗？此操作将清除对应角色的所有菜单绑定！`"
              buttonType="danger"
              type="danger"
              @confirm="handleDelete(row)"
            >
              删除
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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

    <!-- 分配菜单对话框 -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="500px">
      <el-form label-width="80px">
        <el-form-item label="角色名称">
          <el-input :model-value="currentRole?.roleName" disabled />
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            :props="{ label: 'menuName', children: 'children' }"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedMenuIds"
            :default-expand-all="true"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAssignMenusSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import CsPagination from '@/components/CsPagination.vue'
import { ElMessage } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole, assignRoleMenus, getMenuTree } from '@/api/system'
import { usePermission } from '@/composables/usePermission'
import CsPageHeader from '@/components/CsPageHeader.vue'
import CsSearchPanel from '@/components/CsSearchPanel.vue'
import CsTableToolbar from '@/components/CsTableToolbar.vue'
import CsConfirmButton from '@/components/CsConfirmButton.vue'

const { hasPermission } = usePermission()

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

// 菜单分配
const menuDialogVisible = ref(false)
const currentRole = ref(null)
const menuTree = ref([])
const checkedMenuIds = ref([])
const menuTreeRef = ref(null)

const queryParams = reactive({
  page: 1,
  size: 10,
  roleName: '',
  roleCode: '',
  status: null
})

const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRoles(queryParams)
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

const resetQuery = () => {
  queryParams.roleName = ''
  queryParams.roleCode = ''
  queryParams.status = null
  queryParams.page = 1
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  form.id = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  form.id = row.id
  form.roleName = row.roleName
  form.roleCode = row.roleCode
  form.description = row.description
  form.status = row.status
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
      await updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
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

const handleAssignMenus = async (row) => {
  currentRole.value = row
  checkedMenuIds.value = row.menuIds || []

  // 获取菜单树
  try {
    const res = await getMenuTree()
    if (res.code === 200) {
      menuTree.value = res.data
    }
  } catch (error) {
    console.error('获取菜单树失败:', error)
  }

  menuDialogVisible.value = true
}

const handleAssignMenusSubmit = async () => {
  submitLoading.value = true
  try {
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
    const menuIds = [...checkedKeys, ...halfCheckedKeys]

    await assignRoleMenus(currentRole.value.id, menuIds)
    ElMessage.success('分配菜单成功')
    menuDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('分配菜单失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
</style>
