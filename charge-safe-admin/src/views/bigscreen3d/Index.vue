<template>
  <div class="bigscreen3d-container">
    <div ref="canvasContainer" class="canvas-container"></div>
    
    <!-- 2D Overlay UI -->
    <div class="ui-overlay">
      <!-- Header -->
      <header class="header">
        <h1>管理运维大屏 3D</h1>
        <div class="time">{{ currentTime }}</div>
      </header>
      
      <!-- Back Button -->
      <div v-if="currentLevel !== 'EARTH'" class="back-btn" @click="goBack">
        &larr; 返回上级
      </div>
      
      <!-- Data Panels -->
      <div v-if="bigscreenData" class="panels">
        <!-- Left Panel: Stats -->
        <div class="panel left">
          <h2>全网总览</h2>
          <div class="stat-grid">
            <div class="stat-item">
              <div class="label">设备总数</div>
              <div class="value">{{ bigscreenData.dashboard.totalDevices }}</div>
            </div>
            <div class="stat-item">
              <div class="label">端口总数</div>
              <div class="value">{{ bigscreenData.dashboard.totalPorts }}</div>
            </div>
            <div class="stat-item">
              <div class="label">在线率</div>
              <div class="value">{{ bigscreenData.dashboard.totalPorts === 0 ? '0.0' : (((bigscreenData.dashboard.totalPorts - bigscreenData.dashboard.offlinePorts) / bigscreenData.dashboard.totalPorts) * 100).toFixed(1) }}%</div>
            </div>
          </div>
        </div>
        
        <!-- Right Panel: Alarms -->
        <div class="panel right">
          <h2>实时告警</h2>
          <div class="alarm-list">
            <div 
              v-for="alarm in bigscreenData.alarms" 
              :key="alarm.id" 
              class="alarm-item"
              :class="alarm.alarmLevel.toLowerCase()"
            >
              <div class="alarm-type">[{{ alarm.alarmType }}]</div>
              <div class="alarm-content">{{ alarm.alarmContent }}</div>
              <div class="alarm-time">{{ new Date(alarm.createdTime).toLocaleString() }}</div>
            </div>
            <div v-if="!bigscreenData.alarms || bigscreenData.alarms.length === 0" class="no-data">
              暂无告警记录
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="loading" class="loading-overlay">数据加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useBigscreenData } from './composable/useBigscreenData'
import { Engine } from './scene/Engine'
import { EarthView } from './scene/EarthView'
import { CommunityView } from './scene/CommunityView'
import { StationView } from './scene/StationView'

const canvasContainer = ref(null)
const { bigscreenData, loading } = useBigscreenData()

const currentTime = ref(new Date().toLocaleString())
let timeTimer = null
let engine = null

const currentLevel = ref('EARTH') // EARTH, COMMUNITY, STATION
let earthView, communityView, stationView

onMounted(() => {
  timeTimer = setInterval(() => {
    currentTime.value = new Date().toLocaleString()
  }, 1000)
  
  engine = new Engine(canvasContainer.value)
  
  // Initialize views
  earthView = new EarthView(handleCommunityClick)
  engine.setView(earthView)
})

watch(bigscreenData, (newData) => {
  if (newData && currentLevel.value === 'COMMUNITY' && communityView) {
    // If we get new data and we are in community view, we might need to update the boxes
    // For MVP, we can just recreate it or let it be
  }
})

const handleCommunityClick = (communityData) => {
  currentLevel.value = 'COMMUNITY'
  communityView = new CommunityView(bigscreenData.value?.parkingAreaUsage, handleStationClick)
  engine.setView(communityView)
}

const handleStationClick = (stationData) => {
  currentLevel.value = 'STATION'
  stationView = new StationView(stationData)
  engine.setView(stationView)
}

const goBack = () => {
  if (currentLevel.value === 'STATION') {
    currentLevel.value = 'COMMUNITY'
    engine.setView(communityView)
  } else if (currentLevel.value === 'COMMUNITY') {
    currentLevel.value = 'EARTH'
    engine.setView(earthView)
  }
}

onUnmounted(() => {
  clearInterval(timeTimer)
  if (engine) {
    engine.dispose()
  }
})
</script>

<style scoped>
.bigscreen3d-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: #020914;
  color: #fff;
  font-family: 'Inter', sans-serif;
}

.canvas-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.ui-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none; /* Let clicks pass through to canvas */
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.header {
  height: 80px;
  background: linear-gradient(to bottom, rgba(2, 9, 20, 0.9), transparent);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
}

.header h1 {
  font-size: 24px;
  letter-spacing: 2px;
  margin: 0;
  text-shadow: 0 0 10px rgba(59, 130, 246, 0.8);
}

.time {
  font-size: 18px;
  font-family: monospace;
  opacity: 0.8;
}

.back-btn {
  pointer-events: auto;
  position: absolute;
  top: 100px;
  left: 40px;
  padding: 8px 16px;
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.5);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(59, 130, 246, 0.4);
}

.panels {
  flex: 1;
  display: flex;
  justify-content: space-between;
  padding: 40px;
  pointer-events: none;
}

.panel {
  pointer-events: auto;
  width: 320px;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(51, 65, 85, 0.5);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.panel h2 {
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 10px;
}

.stat-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.stat-item {
  background: rgba(255, 255, 255, 0.05);
  padding: 12px;
  border-radius: 8px;
}

.label {
  font-size: 14px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.value {
  font-size: 24px;
  font-weight: bold;
  color: #60a5fa;
}

.alarm-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.alarm-item {
  background: rgba(255, 255, 255, 0.05);
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid transparent;
}

.alarm-item.error {
  border-left-color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.alarm-item.warning {
  border-left-color: #f59e0b;
  background: rgba(245, 158, 11, 0.1);
}

.alarm-item.info {
  border-left-color: #3b82f6;
}

.alarm-type {
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 4px;
}

.alarm-content {
  font-size: 14px;
  margin-bottom: 8px;
}

.alarm-time {
  font-size: 12px;
  color: #94a3b8;
  text-align: right;
}

.loading-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.8);
  padding: 20px 40px;
  border-radius: 8px;
  font-size: 18px;
  letter-spacing: 2px;
}
</style>
