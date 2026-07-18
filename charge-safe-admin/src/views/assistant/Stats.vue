<template>
  <div class="assistant-stats-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">今日问答</div>
              <div class="stat-value">{{ stats.todayCount || 0 }}</div>
            </div>
            <div class="stat-icon-wrapper" style="background: #e8f0fe;">
              <el-icon :size="24" style="color: #409eff;">
                <ChatDotRound />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">本周问答</div>
              <div class="stat-value">{{ stats.weekCount || 0 }}</div>
            </div>
            <div class="stat-icon-wrapper" style="background: #fef3e2;">
              <el-icon :size="24" style="color: #e6a23c;">
                <Calendar />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">本月问答</div>
              <div class="stat-value">{{ stats.monthCount || 0 }}</div>
            </div>
            <div class="stat-icon-wrapper" style="background: #e8f5e9;">
              <el-icon :size="24" style="color: #67c23a;">
                <DataAnalysis />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">总问答数</div>
              <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            </div>
            <div class="stat-icon-wrapper" style="background: #fce4ec;">
              <el-icon :size="24" style="color: #f56c6c;">
                <TrendCharts />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 近7天问答趋势 -->
      <el-col :span="16">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-title">近7天问答趋势</div>
          </template>
          <div ref="trendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>

      <!-- 回答来源分布 -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-title">回答来源分布</div>
          </template>
          <div ref="sourceChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 高频问题列表 -->
    <el-row :gutter="20" style="margin-top: 20px; padding-bottom: 20px;">
      <el-col :span="24">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-title">高频问题 TOP10</div>
          </template>
          <el-table :data="stats.hotQuestions || []" style="width: 100%">
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="question" label="问题" />
            <el-table-column prop="count" label="提问次数" width="120" align="center">
              <template #default="{ row }">
                <el-tag type="primary">{{ row.count }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getAssistantStats } from '@/api/assistant'

// 图表引用
const trendChartRef = ref(null)
const sourceChartRef = ref(null)

// 统计数据
const stats = ref({})

// 图表实例
let trendChart = null
let sourceChart = null

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getAssistantStats()
    if (res.code === 200) {
      stats.value = res.data || {}
      await nextTick()
      initCharts()
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  // 趋势图
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    const dates = (stats.value.dailyTrend || []).map(item => item.date)
    const counts = (stats.value.dailyTrend || []).map(item => item.count)

    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates
      },
      yAxis: { type: 'value' },
      series: [{
        name: '问答数',
        type: 'line',
        smooth: true,
        areaStyle: {},
        data: counts,
        itemStyle: { color: '#409eff' }
      }]
    })
  }

  // 来源分布图
  if (sourceChartRef.value) {
    sourceChart = echarts.init(sourceChartRef.value)
    const sourceData = Object.entries(stats.value.sourceDistribution || {}).map(([name, value]) => ({
      name,
      value
    }))

    sourceChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: '5%' },
      series: [{
        name: '回答来源',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 20, fontWeight: 'bold' }
        },
        labelLine: { show: false },
        data: sourceData
      }]
    })
  }
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  trendChart?.resize()
  sourceChart?.resize()
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  sourceChart?.dispose()
})
</script>

<style scoped>
.assistant-stats-page {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-card {
  border: none;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
