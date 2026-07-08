<template>
  <div class="cs-table-toolbar">
    <div class="toolbar-left">
      <slot name="left"></slot>
    </div>
    <div class="toolbar-right">
      <slot name="right"></slot>
      <el-tooltip content="刷新数据" placement="top">
        <el-button icon="Refresh" circle @click="$emit('refresh')" />
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
defineEmits(['refresh'])
</script>

<style scoped>
.cs-table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
}
.toolbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
