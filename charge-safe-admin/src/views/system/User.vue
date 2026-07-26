<template>
  <div class="user-page">
    <!-- 页面标题 -->
    <CsPageHeader title="用户管理" description="管理系统所有的系统用户，分配其对应的安全访问角色与所属社区。" />

    <!-- 搜索面板 -->
    <CsSearchPanel :model="queryParams" @search="fetchData" @reset="resetQuery">
      <el-form-item label="用户名">
        <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="fetchData" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter="fetchData" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 150px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="queryParams.roleId" placeholder="全部" clearable style="width: 180px">
          <el-option v-for="item in roleList" :key="item.id" :label="item.roleName" :value="item.id" />
        </el-select>
      </el-form-item>
    </CsSearchPanel>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="fetchData">
        <template #left>
          <el-button v-if="hasPermission('system:user:create')" type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
        </template>
      </CsTableToolbar>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="nickname" label="昵称" width="130" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="roleNames" label="角色" width="180">
          <template #default="{ row }">
            <el-tag v-if="row.roleNames?.length">{{ row.roleNames.join('、') }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="communityName" label="所属社区" show-overflow-tooltip />
        <el-table-column prop="deptName" label="所属部门" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasPermission('system:user:update')" type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="hasPermission('system:user:assign-role')" type="primary" link icon="Key" @click="handleAssignRoles(row)">角色</el-button>
            <CsConfirmButton
              v-if="hasPermission('system:user:reset-password')"
              :title="`确定重置用户 【${row.username}】 的密码吗？`"
              buttonType="info"
              type="warning"
              @confirm="handleResetPassword(row)"
            >
              重置密码
            </CsConfirmButton>
            <CsConfirmButton
              v-if="hasPermission('system:user:status')"
              :title="`确定要${row.status === 1 ? '停用' : '启用'}用户 【${row.username}】 吗？`"
              :buttonType="row.status === 1 ? 'warning' : 'success'"
              :type="row.status === 1 ? 'warning' : 'success'"
              @confirm="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </CsConfirmButton>
            <CsConfirmButton
              v-if="hasPermission('system:user:delete')"
              :title="`确定要删除用户 【${row.username}】 吗？此操作不可逆！`"
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所属社区" prop="communityId">
          <el-select v-model="form.communityId" placeholder="请选择社区" clearable filterable style="width: 100%">
            <el-option v-for="item in communityList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门（可选）" clearable filterable style="width: 100%">
            <el-option v-for="item in departmentList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input :model-value="currentUser?.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="selectedRoleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roleList" :key="item.id" :label="item.roleName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAssignRolesSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import CsPagination from '@/components/CsPagination.vue'
import { ElMessage } from 'element-plus'
import {
  getUsers, createUser, updateUser, deleteUser,
  updateUserStatus, assignUserRoles, resetUserPassword
} from '@/api/system'
import { getCommunityList } from '@/api/community'
import { getRoleList, getDepartments } from '@/api/system'
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
const communityList = ref([])
const departmentList = ref([])
const roleList = ref([])

// 角色分配
const roleDialogVisible = ref(false)
const currentUser = ref(null)
const selectedRoleIds = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  username: '',
  phone: '',
  status: null,
  roleId: null
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  phone: '',
  communityId: null,
  deptId: null
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUsers(queryParams)
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

const fetchCommunityList = async () => {
  try {
    const res = await getCommunityList()
    if (res.code === 200) {
      communityList.value = res.data
    }
  } catch (error) {
    console.error('获取社区列表失败:', error)
  }
}

const fetchRoleList = async () => {
  try {
    const res = await getRoleList()
    if (res.code === 200) {
      roleList.value = res.data
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const fetchDepartmentList = async () => {
  try {
    const res = await getDepartments()
    if (res.code === 200) {
      departmentList.value = res.data.filter((item) => item.status === 1)
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

const resetQuery = () => {
  queryParams.username = ''
  queryParams.phone = ''
  queryParams.status = null
  queryParams.roleId = null
  queryParams.page = 1
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  form.id = null
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.phone = ''
  form.communityId = null
  form.deptId = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  form.id = row.id
  form.username = row.username
  form.password = ''
  form.nickname = row.nickname
  form.phone = row.phone
  form.communityId = row.communityId
  form.deptId = row.deptId
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
      await updateUser(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
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
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleResetPassword = async (row) => {
  try {
    await resetUserPassword(row.id)
    ElMessage.success('密码已重置')
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleAssignRoles = (row) => {
  currentUser.value = row
  selectedRoleIds.value = row.roleIds || []
  roleDialogVisible.value = true
}

const handleAssignRolesSubmit = async () => {
  submitLoading.value = true
  try {
    await assignUserRoles(currentUser.value.id, selectedRoleIds.value)
    ElMessage.success('分配角色成功')
    roleDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('分配角色失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

onMounted(() => {
  fetchData()
  fetchCommunityList()
  fetchRoleList()
  fetchDepartmentList()
})
</script>

<style scoped>
</style>
