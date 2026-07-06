<template>
  <div class="reservation-page">
    <!-- 页面标题 -->
    <CsPageHeader title="预约记录" description="查看社区居民提交的充电端口预约记录及当前生效状态。" />

    <!-- 数据表格 -->
    <el-card shadow="never">
      <CsTableToolbar @refresh="fetchData" />

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="预约用户" />
        <el-table-column prop="portName" label="预约端口" />
        <el-table-column prop="reserveTime" label="预约时间" width="180" />
        <el-table-column prop="expireTime" label="过期时间" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <CsStatusTag :status="row.status" module="reservation" />
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
import { getReservations } from '@/api/charging'
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

<style scoped>
</style>
