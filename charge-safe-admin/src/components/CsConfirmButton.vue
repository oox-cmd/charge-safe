<template>
  <el-popconfirm
    :title="title"
    :confirm-button-text="confirmText"
    :cancel-button-text="cancelText"
    :confirm-button-type="type === 'danger' ? 'danger' : 'primary'"
    @confirm="$emit('confirm')"
  >
    <template #reference>
      <el-button :type="buttonType" :link="link" :size="size" :icon="icon" :disabled="disabled">
        <slot></slot>
      </el-button>
    </template>
  </el-popconfirm>
</template>

<script setup>
defineProps({
  title: { type: String, default: '确定执行此操作吗？' },
  confirmText: { type: String, default: '确定' },
  cancelText: { type: String, default: '取消' },
  type: { type: String, default: 'warning' },
  buttonType: { type: String, default: 'primary' },
  link: { type: Boolean, default: true },
  size: { type: String, default: 'default' },
  icon: { type: String, default: '' },
  disabled: { type: Boolean, default: false }
})
defineEmits(['confirm'])
</script>
