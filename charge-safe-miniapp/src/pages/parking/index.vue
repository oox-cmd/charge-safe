<template>
  <view class="parking-page">
    <view class="parking-list">
      <view class="parking-item" v-for="item in parkingAreas" :key="item.id" @click="goDetail(item)">
        <view class="parking-info">
          <text class="parking-name">{{ item.name }}</text>
          <text class="parking-location">{{ item.location }}</text>
        </view>
        <view class="parking-stats">
          <text class="idle-count">{{ item.idlePorts }}个空闲</text>
          <text class="total-count">共{{ item.totalPorts }}个端口</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const parkingAreas = ref([
  { id: 1, name: 'A栋充电车棚', location: '小区东门入口处', idlePorts: 5, totalPorts: 8 },
  { id: 2, name: 'B栋充电车棚', location: '小区西门入口处', idlePorts: 4, totalPorts: 6 }
])

const goDetail = (item) => {
  uni.navigateTo({ url: `/pages/parking/detail?id=${item.id}&name=${item.name}` })
}
</script>

<style scoped>
.parking-page {
  padding: 20rpx;
  background: #f5f7fa;
  min-height: 100vh;
}
.parking-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.parking-name {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
}
.parking-location {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}
.parking-stats {
  text-align: right;
}
.idle-count {
  font-size: 28rpx;
  color: #67c23a;
  display: block;
}
.total-count {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}
</style>
