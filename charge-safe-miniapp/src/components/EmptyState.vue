<template>
  <view class="empty-state">
    <text class="empty-icon">{{ iconMap[type] }}</text>
    <text class="empty-title">{{ title }}</text>
    <text class="empty-desc" v-if="description">{{ description }}</text>
    <view class="empty-action" v-if="btnText">
      <view class="btn-refresh" @click="$emit('action')">{{ btnText }}</view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
  btnText: { type: String, default: '' },
  type: { type: String, default: 'empty' }
})
defineEmits(['action'])

const iconMap = {
  empty: '📭',
  error: '❌',
  network: '📡'
}
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;
}
.empty-icon { font-size: 80rpx; margin-bottom: 24rpx; }
.empty-title { font-size: 30rpx; color: #666; margin-bottom: 12rpx; }
.empty-desc { font-size: 26rpx; color: #999; text-align: center; line-height: 1.6; }
.empty-action { margin-top: 40rpx; }
.btn-refresh {
  display: flex; align-items: center; justify-content: center;
  height: 72rpx; padding: 0 48rpx;
  background: #409eff; color: #fff; font-size: 28rpx; border-radius: 36rpx;
}
</style>
