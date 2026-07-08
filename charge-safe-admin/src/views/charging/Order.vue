<template>
  <div class="order-page">
    <!-- 页面标题 -->
    <CsPageHeader title="充电订单" description="监控并管理社区居民提交的充电消费订单流转与费用明细。" />

    <!-- 数据表格 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="fetchData" />

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="userName" label="用户" />
        <el-table-column prop="portName" label="端口" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="durationMinutes" label="时长(分)" width="100" />
        <el-table-column prop="amount" label="费用(元)" width="100" />
        <el-table-column prop="status" label="状态" width="120" fixed="right">
          <template #default="{ row }">
            <CsStatusTag :status="row.status" module="order" />
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
import { getOrders } from '@/api/charging'
import CsPageHeader from '@/components/CsPageHeader.vue'
import CsTableToolbar from '@/components/CsTableToolbar.vue'
import CsStatusTag from '@/components/CsStatusTag.vue'
import CsPagination from '@/components/CsPagination.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10
})

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

<style scoped>
</style>
