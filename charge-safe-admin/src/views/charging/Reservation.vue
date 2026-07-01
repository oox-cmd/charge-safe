<template>
  <div class="reservation-page">
    <el-card>
      <template #header>
        <span>预约记录</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="预约用户" />
        <el-table-column prop="portName" label="预约端口" />
        <el-table-column prop="reserveTime" label="预约时间" width="180" />
        <el-table-column prop="expireTime" label="过期时间" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getReservations } from '@/api/charging'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10
})

const getStatusType = (status) => {
  const map = { 'PENDING': 'warning', 'CONVERTED': 'success', 'CANCELLED': 'info', 'EXPIRED': 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待生效', 'CONVERTED': '已转订单', 'CANCELLED': '用户取消', 'EXPIRED': '超时取消' }
  return map[status] || status
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getReservations(queryParams)
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

onMounted(() => fetchData())
</script>
