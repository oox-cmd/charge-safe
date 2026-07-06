<template>
  <el-pagination
    v-model:current-page="currentPageModel"
    v-model:page-size="pageSizeModel"
    :total="total"
    :page-sizes="pageSizes"
    :layout="layout"
    class="cs-pagination"
    @change="$emit('change')"
  />
</template>

<script setup>
const currentPageModel = defineModel('currentPage', { type: Number, required: true })
const pageSizeModel = defineModel('pageSize', { type: Number, required: true })

defineProps({
  total: { type: Number, required: true },
  pageSizes: { type: Array, default: () => [10, 20, 50] },
  layout: { type: String, default: 'total, sizes, prev, pager, next' }
})

defineEmits(['change'])
</script>

<style scoped>
.cs-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
