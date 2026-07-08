<template>
  <el-card class="cs-search-panel" shadow="never">
    <el-form :inline="true" :model="model" class="search-form">
      <slot></slot>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="$emit('search')">查询</el-button>
        <el-button icon="Refresh" @click="$emit('reset')">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
defineProps({
  model: { type: Object, required: true }
})
defineEmits(['search', 'reset'])
</script>

<style scoped>
.cs-search-panel {
  margin-bottom: 20px;
}
.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
:deep(.el-form-item) {
  margin-bottom: 12px !important;
  margin-right: 18px !important;
}
:deep(.el-form-item:last-child) {
  margin-bottom: 12px !important;
  margin-right: 0 !important;
}
</style>
