<template>
  <div class="order-page">
    <el-card>
      <template #header>
        <span>充电订单</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="userName" label="用户" />
        <el-table-column prop="portName" label="端口" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="durationMinutes" label="时长(分)" width="80" />
        <el-table-column prop="amount" label="费用(元)" width="80" />
        <el-table-column prop="status" label="状态" width="100">
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
import { getOrders } from '@/api/charging'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10
})

const getStatusType = (status) => {
  const map = { 'CHARGING': 'warning', 'COMPLETED': 'success', 'SETTLED': 'success', 'CANCELLED': 'info', 'ABNORMAL': 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 'CHARGING': '充电中', 'COMPLETED': '已完成', 'SETTLED': '已结算', 'CANCELLED': '已取消', 'ABNORMAL': '异常结束' }
  return map[status] || status
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOrders(queryParams)
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
