<template>
  <div class="cs-page-header">
    <div class="header-left">
      <h2 class="title">{{ title }}</h2>
      <p v-if="description" class="description">{{ description }}</p>
    </div>
    <div class="header-right">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' }
})
</script>

<style scoped>
.cs-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.title {
  margin: 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}
.description {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #909399;
}
</style>
