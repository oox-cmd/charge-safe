<template>
  <el-tag :type="tagType" :effect="effect" class="cs-status-tag">
    {{ statusLabel }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true },
  module: { type: String, default: 'default' },
  effect: { type: String, default: 'light' }
})

// 统一状态字典定义与颜色映射（支持多业务模块解耦）
const statusMap = {
  default: {
    // 设备 / 端口状态
    'IDLE': { label: '空闲', type: 'success' },
    'RESERVED': { label: '已预约', type: 'primary' },
    'CHARGING': { label: '充电中', type: 'warning' },
    'FAULT': { label: '故障', type: 'danger' },
    'MAINTENANCE': { label: '维修中', type: 'info' },
    'DISABLED': { label: '停用', type: 'info' },
    'OFFLINE': { label: '离线', type: 'info' },
    'COMPLETED_WAITING_PICKUP': { label: '待取车', type: 'success' },
    'OVERTIME_OCCUPIED': { label: '超时占用', type: 'danger' },
    
    // 报修记录 / 安全隐患整改状态
    'PENDING': { label: '待处理', type: 'danger' },
    'RECTIFYING': { label: '整改中', type: 'warning' },
    'REVIEWING': { label: '待复查', type: 'primary' },
    'CLOSED': { label: '已完成/已关闭', type: 'success' },
    
    // 维修工单状态
    'DISPATCHED': { label: '待处理', type: 'warning' },
    'PROCESSING': { label: '进行中', type: 'primary' },
    'RESOLVED': { label: '已完成', type: 'success' },
    
    // 巡检任务状态
    'TODO': { label: '待处理', type: 'warning' },
    'DOING': { label: '进行中', type: 'primary' },
    'DONE': { label: '已完成', type: 'success' }
  },
  reservation: {
    'PENDING': { label: '待生效', type: 'warning' },
    'CONVERTED': { label: '已转订单', type: 'success' },
    'CANCELLED': { label: '用户取消', type: 'info' },
    'EXPIRED': { label: '超时取消', type: 'danger' }
  },
  order: {
    'CHARGING': { label: '充电中', type: 'warning' },
    'COMPLETED': { label: '已完成', type: 'success' },
    'SETTLED': { label: '已结算', type: 'success' },
    'CANCELLED': { label: '已取消', type: 'info' },
    'ABNORMAL': { label: '异常结束', type: 'danger' }
  },
  repair_report: {
    'PENDING': { label: '待处理', type: 'warning' },
    'CONFIRMED': { label: '已确认', type: 'primary' },
    'DISPATCHED': { label: '已派单', type: 'success' },
    'REJECTED': { label: '已驳回', type: 'danger' }
  },
  repair_ticket: {
    'PENDING': { label: '待受理', type: 'info' },
    'DISPATCHED': { label: '已派单', type: 'warning' },
    'PROCESSING': { label: '处理中', type: 'primary' },
    'WAITING_ACCEPTANCE': { label: '待验收', type: 'warning' },
    'FINISHED': { label: '已完成', type: 'success' },
    'CLOSED': { label: '已关闭', type: 'info' },
    'REJECTED': { label: '已驳回', type: 'danger' }
  },
  inspection_task: {
    'PENDING': { label: '待执行', type: 'info' },
    'IN_PROGRESS': { label: '执行中', type: 'warning' },
    'COMPLETED': { label: '已完成', type: 'success' },
    'ABNORMAL': { label: '发现异常', type: 'danger' },
    'OVERDUE': { label: '已逾期', type: 'danger' }
  },
  inspection_hazard: {
    'PENDING': { label: '待处理', type: 'warning' },
    'RECTIFYING': { label: '整改中', type: 'primary' },
    'WAITING_REVIEW': { label: '待复查', type: 'warning' },
    'REVIEW_PASSED': { label: '复查通过', type: 'success' },
    'REVIEW_FAILED': { label: '复查不通过', type: 'danger' },
    'CLOSED': { label: '已关闭', type: 'info' }
  },
  hazard_severity: {
    'GENERAL': { label: '一般', type: 'info' },
    'SERIOUS': { label: '严重', type: 'warning' },
    'URGENT': { label: '紧急', type: 'danger' }
  }
}

const statusLabel = computed(() => {
  const map = statusMap[props.module] || statusMap.default
  return map[props.status]?.label || props.status
})

const tagType = computed(() => {
  const map = statusMap[props.module] || statusMap.default
  return map[props.status]?.type || 'info'
})
</script>

<style scoped>
.cs-status-tag {
  font-weight: 500;
}
</style>
