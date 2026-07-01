<template>
  <div class="log-page">
    <el-card>
      <template #header>
        <span>操作日志</span>
      </template>

      <el-form :inline="true" :model="queryParams" style="margin-bottom: 20px">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="queryParams.module" placeholder="选择模块" clearable>
            <el-option label="认证管理" value="认证管理" />
            <el-option label="社区管理" value="社区管理" />
            <el-option label="预约充电" value="预约充电" />
            <el-option label="报修管理" value="报修管理" />
            <el-option label="巡检管理" value="巡检管理" />
            <el-option label="隐患管理" value="隐患管理" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作人" width="100" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="operation" label="操作描述" />
        <el-table-column prop="method" label="方法" show-overflow-tooltip width="200" />
        <el-table-column prop="ip" label="IP" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)" width="90" />
        <el-table-column prop="createdTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detail.module }}</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ detail.operation }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detail.method }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ detail.url }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detail.ip }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap; font-size: 12px;">{{ detail.params }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 1 ? 'success' : 'danger'">
            {{ detail.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="detail.errorMsg">
          <span style="color: #f56c6c">{{ detail.errorMsg }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.createdTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLogs } from '@/api/log'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref({})

const queryParams = reactive({
  page: 1,
  size: 10,
  username: '',
  module: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLogs(queryParams)
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

const handleDetail = (row) => {
  detail.value = row
  detailVisible.value = true
}

onMounted(() => fetchData())
</script>
