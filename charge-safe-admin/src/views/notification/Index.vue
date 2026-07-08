<template>
  <div class="notification-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <el-button type="primary" @click="handleMarkAllRead">全部已读</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 0 ? 'danger' : 'success'">
              {{ row.isRead === 0 ? '未读' : '已读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.isRead === 0" type="primary" link @click="handleMarkRead(row)">标为已读</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import CsPagination from '@/components/CsPagination.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNotifications, markAsRead, markAllAsRead } from '@/api/notification'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10
})

const getTypeText = (type) => {
  const map = { 'RESERVATION': '预约', 'CHARGING': '充电', 'REPAIR': '报修', 'INSPECTION': '巡检', 'HAZARD': '隐患', 'SYSTEM': '系统' }
  return map[type] || type
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getNotifications(queryParams)
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

const handleMarkRead = async (row) => {
  try {
    await markAsRead(row.id)
    ElMessage.success('已标为已读')
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定要将所有消息标为已读吗？', '提示', { type: 'warning' })
    await markAllAsRead()
    ElMessage.success('操作成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('操作失败:', error)
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
