<template>
  <main class="bigscreen3d-container">
    <div ref="canvasContainer" class="canvas-container" aria-label="三级管理运维场景" />

    <div class="ui-overlay">
      <header class="header">
        <div>
          <p class="eyebrow">CHARGESAFE · 管理端</p>
          <h1>三级管理运维大屏</h1>
          <p class="level">当前层级：{{ levelLabel }}</p>
        </div>
        <time>{{ currentTime }}</time>
      </header>

      <button v-if="currentLevel !== 'EARTH'" class="back-button" type="button" @click="goBack">
        返回上级
      </button>

      <section v-if="bigscreenData" class="notice" role="status">
        {{ bigscreenData.dataDisclaimer || simulationDisclaimer }}
      </section>

      <section v-if="bigscreenData" class="side-panels">
        <article class="panel">
          <p class="panel-kicker">业务总览</p>
          <dl class="metrics">
            <div><dt>设备总数</dt><dd>{{ bigscreenData.dashboard?.totalDevices ?? 0 }}</dd></div>
            <div><dt>端口总数</dt><dd>{{ bigscreenData.dashboard?.totalPorts ?? 0 }}</dd></div>
            <div><dt>在线率</dt><dd>{{ onlineRate }}%</dd></div>
          </dl>
          <p v-if="lastUpdatedAt" class="updated">数据更新：{{ formatTime(lastUpdatedAt) }}</p>
        </article>

        <article class="panel panel-alarms">
          <p class="panel-kicker">告警记录</p>
          <ul v-if="bigscreenData.alarms?.length" class="alarm-list">
            <li v-for="alarm in bigscreenData.alarms" :key="alarm.id" :class="alarmClass(alarm.alarmLevel)">
              <strong>{{ alarm.alarmContent || '未命名告警' }}</strong>
              <time>{{ formatTime(alarm.createdTime) }}</time>
            </li>
          </ul>
          <p v-else class="empty">暂无告警记录</p>
        </article>
      </section>

      <section v-if="loading" class="state-card" role="status">正在加载场景数据…</section>
      <section v-else-if="renderError" class="state-card state-error" role="alert">
        <p>{{ renderError }}</p>
        <button type="button" @click="retry">重新加载</button>
      </section>
      <section v-else-if="error" class="state-card state-error" role="alert">
        <p>{{ error }}</p>
        <button type="button" @click="retry">重新请求</button>
      </section>
      <section v-else-if="bigscreenData && !markerCount" class="state-card" role="status">
        当前权限范围内没有已登记经纬度的社区，无法生成地球标记。
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useBigscreenData } from './composable/useBigscreenData'
import { Engine } from './scene/Engine'
import { EarthView } from './scene/EarthView'
import { CommunityView } from './scene/CommunityView'
import { StationView } from './scene/StationView'
import {
  buildCommunityMarkers,
  selectAreasForCommunity,
  SIMULATION_DISCLAIMER
} from './domain/sceneData.mjs'

const canvasContainer = ref(null)
const currentLevel = ref('EARTH')
const currentTime = ref(new Date().toLocaleString())
const renderError = ref('')
const selectedCommunity = ref(null)
const { bigscreenData, loading, error, lastUpdatedAt, fetchData } = useBigscreenData()

let timer = null
let engine = null
let earthView = null
let communityView = null
let stationView = null

const simulationDisclaimer = SIMULATION_DISCLAIMER
const levelLabel = computed(() => ({ EARTH: '社区总览', COMMUNITY: '社区车棚', STATION: '车棚端口' })[currentLevel.value])
const markerCount = computed(() => buildCommunityMarkers(bigscreenData.value?.communities || []).length)
const onlineRate = computed(() => {
  const dashboard = bigscreenData.value?.dashboard
  const total = Number(dashboard?.totalPorts || 0)
  const offline = Number(dashboard?.offlinePorts || 0)
  return total > 0 ? (((total - offline) / total) * 100).toFixed(1) : '0.0'
})

function renderEarth() {
  if (!engine || !bigscreenData.value) return
  if (!markerCount.value) return
  renderError.value = ''
  earthView = new EarthView(bigscreenData.value.communities, handleCommunityClick)
  engine.setView(earthView)
}

function handleCommunityClick(community) {
  selectedCommunity.value = community
  currentLevel.value = 'COMMUNITY'
  renderCommunity()
}

function renderCommunity() {
  if (!engine || !selectedCommunity.value) return
  const areas = selectAreasForCommunity(
    bigscreenData.value?.parkingAreaUsage?.areas,
    selectedCommunity.value.id
  )
  communityView = new CommunityView(areas, handleStationClick)
  engine.setView(communityView)
}

function handleStationClick(station) {
  currentLevel.value = 'STATION'
  stationView = new StationView(station)
  engine.setView(stationView)
}

function goBack() {
  if (currentLevel.value === 'STATION') {
    currentLevel.value = 'COMMUNITY'
    renderCommunity()
    return
  }
  currentLevel.value = 'EARTH'
  selectedCommunity.value = null
  renderEarth()
}

function retry() {
  renderError.value = ''
  fetchData()
  if (bigscreenData.value) renderEarth()
}

function formatTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString()
}

function alarmClass(level) {
  return level === 'ERROR' ? 'alarm-error' : level === 'WARNING' ? 'alarm-warning' : 'alarm-info'
}

watch(bigscreenData, (data) => {
  if (data && currentLevel.value === 'EARTH') renderEarth()
})

onMounted(() => {
  timer = setInterval(() => { currentTime.value = new Date().toLocaleString() }, 1000)
  try {
    engine = new Engine(canvasContainer.value)
    if (bigscreenData.value) renderEarth()
  } catch (cause) {
    renderError.value = cause?.message || '三维场景初始化失败。'
  }
})

onUnmounted(() => {
  clearInterval(timer)
  engine?.dispose()
})
</script>

<style scoped>
.bigscreen3d-container { position: relative; width: 100vw; height: 100vh; overflow: hidden; background: #020914; color: #e2e8f0; font-family: Inter, "Microsoft YaHei", sans-serif; }
.canvas-container { position: absolute; inset: 0; }
.ui-overlay { position: absolute; inset: 0; pointer-events: none; display: flex; flex-direction: column; padding: 28px 36px; }
.header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 1px solid rgba(148, 163, 184, .25); padding-bottom: 18px; }
.eyebrow, .level, .panel-kicker, .updated { margin: 0; color: #94a3b8; font-size: 12px; letter-spacing: .08em; }
h1 { margin: 6px 0; color: #f8fafc; font-size: 26px; letter-spacing: .04em; }
.header time { color: #cbd5e1; font-variant-numeric: tabular-nums; }
.notice { align-self: center; max-width: 760px; margin-top: 16px; border: 1px solid rgba(245, 158, 11, .45); background: rgba(120, 53, 15, .7); color: #fde68a; padding: 9px 14px; border-radius: 6px; font-size: 13px; }
.side-panels { display: flex; justify-content: space-between; gap: 18px; margin-top: 18px; }
.panel { width: min(330px, 31vw); pointer-events: auto; border: 1px solid rgba(71, 85, 105, .7); border-radius: 8px; background: rgba(15, 23, 42, .88); padding: 18px; backdrop-filter: blur(8px); }
.metrics { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin: 14px 0; }
.metrics div { border-left: 2px solid #14b8a6; padding-left: 9px; }
dt { color: #94a3b8; font-size: 12px; } dd { margin: 5px 0 0; color: #f8fafc; font-size: 20px; font-variant-numeric: tabular-nums; }
.alarm-list { list-style: none; margin: 12px 0 0; padding: 0; max-height: 220px; overflow-y: auto; }
.alarm-list li { border-left: 3px solid #64748b; padding: 8px 0 8px 10px; margin-bottom: 8px; background: rgba(15, 23, 42, .65); }
.alarm-list strong { display: block; color: #e2e8f0; font-size: 13px; font-weight: 500; }.alarm-list time { color: #94a3b8; font-size: 12px; }.alarm-error { border-left-color: #ef4444 !important; }.alarm-warning { border-left-color: #f59e0b !important; }.alarm-info { border-left-color: #3b82f6 !important; }
.empty { color: #94a3b8; font-size: 13px; }.back-button { pointer-events: auto; align-self: flex-start; margin-top: 18px; border: 1px solid #14b8a6; border-radius: 5px; background: rgba(15, 23, 42, .92); color: #ccfbf1; padding: 8px 12px; cursor: pointer; }.back-button:hover { background: #0f766e; }
.state-card { pointer-events: auto; align-self: center; margin: auto; max-width: 430px; border: 1px solid #475569; border-radius: 8px; background: rgba(15, 23, 42, .95); padding: 20px; text-align: center; color: #cbd5e1; }.state-error { border-color: #ef4444; color: #fecaca; }.state-card button { margin-top: 12px; border: 1px solid currentColor; border-radius: 5px; background: transparent; color: inherit; padding: 7px 10px; cursor: pointer; }
@media (max-width: 900px) { .ui-overlay { padding: 16px; }.side-panels { flex-direction: column; align-items: flex-end; }.panel { width: min(340px, 82vw); }.notice { align-self: stretch; }.header time { font-size: 12px; } }
</style>
