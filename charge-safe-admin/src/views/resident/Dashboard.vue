<template>
  <main class="resident-dashboard">
    <header class="page-header">
      <div>
        <p class="eyebrow">社区充电</p>
        <h1>{{ dashboard.communityName || '我的社区' }}</h1>
        <p class="subtitle">查看附近车棚的真实可用情况，再决定去哪里充电。</p>
      </div>
      <div class="header-actions">
        <p class="updated-at" aria-live="polite">{{ updatedText }}</p>
        <el-button :loading="loading" @click="loadDashboard">刷新状态</el-button>
        <el-button text @click="handleLogout">退出登录</el-button>
      </div>
    </header>

    <section v-if="loading && !loaded" class="state-panel" aria-live="polite">
      <el-skeleton :rows="6" animated />
    </section>

    <section v-else-if="errorMessage" class="state-panel error-state" role="alert">
      <h2>暂时无法获取充电位状态</h2>
      <p>{{ errorMessage }}</p>
      <el-button type="primary" @click="loadDashboard">重新加载</el-button>
    </section>

    <section v-else-if="!dashboard.communityId" class="state-panel empty-state">
      <h2>当前账号尚未关联社区</h2>
      <p>请联系物业管理员为居民账号设置所属社区，系统不会自动展示其他社区的数据。</p>
    </section>

    <template v-else>
      <section class="availability-summary" aria-labelledby="availability-title">
        <div>
          <p class="summary-label" id="availability-title">当前可预约端口</p>
          <p class="available-number">{{ dashboard.idleCount }}</p>
          <p class="availability-copy">
            {{ dashboard.availableForReservation ? '有空闲端口，可前往下方车棚' : '当前暂无空闲端口，请稍后刷新' }}
          </p>
        </div>
        <dl class="summary-grid">
          <div><dt>车棚</dt><dd>{{ dashboard.totalParkingAreas }}</dd></div>
          <div><dt>全部端口</dt><dd>{{ dashboard.totalPorts }}</dd></div>
          <div><dt>已预约</dt><dd>{{ dashboard.reservedCount }}</dd></div>
          <div><dt>充电中</dt><dd>{{ dashboard.chargingCount }}</dd></div>
          <div><dt>故障</dt><dd>{{ dashboard.faultCount }}</dd></div>
          <div><dt>维修中</dt><dd>{{ dashboard.maintenanceCount }}</dd></div>
        </dl>
      </section>

      <section class="parking-section" aria-labelledby="parking-title">
        <div class="section-heading">
          <div>
            <p class="eyebrow">按车棚查看</p>
            <h2 id="parking-title">选择更合适的充电位置</h2>
          </div>
          <p>空闲、预约、充电与异常状态均来自当前社区实时业务数据。</p>
        </div>

        <div v-if="dashboard.parkingAreas.length" class="parking-grid">
          <article v-for="area in dashboard.parkingAreas" :key="area.parkingAreaId" class="parking-card">
            <div class="card-heading">
              <div>
                <h3>{{ area.parkingAreaName }}</h3>
                <p>{{ area.locationDescription || '暂无位置说明' }}</p>
              </div>
              <span class="availability-badge" :class="{ unavailable: !area.availableForReservation }">
                {{ area.availableForReservation ? '可预约' : '暂无空闲' }}
              </span>
            </div>

            <div class="primary-count">
              <strong>{{ area.idleCount }}</strong>
              <span>个空闲端口</span>
            </div>

            <dl class="status-list">
              <div><dt><span class="status-dot reserved"></span>已预约</dt><dd>{{ area.reservedCount }}</dd></div>
              <div><dt><span class="status-dot charging"></span>充电中</dt><dd>{{ area.chargingCount }}</dd></div>
              <div><dt><span class="status-dot fault"></span>故障</dt><dd>{{ area.faultCount }}</dd></div>
              <div><dt><span class="status-dot maintenance"></span>维修中</dt><dd>{{ area.maintenanceCount }}</dd></div>
              <div><dt><span class="status-dot unavailable"></span>其他不可用</dt><dd>{{ area.unavailableCount }}</dd></div>
              <div><dt>端口总数</dt><dd>{{ area.totalPorts }}</dd></div>
            </dl>
          </article>
        </div>

        <div v-else class="state-panel empty-state">
          <h3>社区暂未配置可用车棚</h3>
          <p>当前没有可展示的启用车棚，请联系物业确认充电设施配置。</p>
        </div>
      </section>
    </template>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getResidentDashboard } from '@/api/resident'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loaded = ref(false)
const errorMessage = ref('')
const updatedAt = ref(null)
const dashboard = reactive({
  communityId: null,
  communityName: '',
  totalParkingAreas: 0,
  totalPorts: 0,
  idleCount: 0,
  reservedCount: 0,
  chargingCount: 0,
  faultCount: 0,
  maintenanceCount: 0,
  unavailableCount: 0,
  availableForReservation: false,
  parkingAreas: []
})

const updatedText = computed(() => updatedAt.value
  ? `更新于 ${updatedAt.value.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  : '尚未更新')

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await getResidentDashboard()
    Object.assign(dashboard, response.data || {}, {
      parkingAreas: response.data?.parkingAreas || []
    })
    updatedAt.value = new Date()
    loaded.value = true
  } catch (error) {
    errorMessage.value = error?.message || '网络连接失败，请检查后重试。'
  } finally {
    loading.value = false
  }
}

function handleLogout() {
  userStore.logout()
  router.replace('/login')
}

onMounted(loadDashboard)
</script>

<style scoped>
.resident-dashboard {
  min-height: 100dvh;
  padding: clamp(20px, 4vw, 52px);
  background: #f4f6f3;
  color: #18231c;
}

.page-header,
.availability-summary,
.section-heading,
.card-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.page-header {
  width: min(1180px, 100%);
  margin: 0 auto 32px;
}

.eyebrow {
  margin-bottom: 8px;
  color: #2f6f4e;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

h1, h2, h3, p { margin: 0; }
h1 { font-size: clamp(30px, 5vw, 48px); line-height: 1.15; }
h2 { font-size: clamp(22px, 3vw, 30px); }
h3 { font-size: 20px; }

.subtitle,
.section-heading > p,
.card-heading p,
.state-panel p {
  color: #5c6960;
  line-height: 1.65;
}

.subtitle { margin-top: 10px; font-size: 16px; }
.header-actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; justify-content: flex-end; }
.updated-at { color: #6d786f; font-size: 14px; }

.availability-summary,
.parking-section,
.state-panel {
  width: min(1180px, 100%);
  margin-inline: auto;
}

.availability-summary {
  padding: clamp(24px, 4vw, 40px);
  border: 1px solid #cfd9d1;
  border-radius: 22px;
  background: #183b2a;
  color: #f8fbf9;
  box-shadow: 0 18px 44px rgba(31, 65, 45, 0.14);
}

.summary-label { color: #c8dace; font-weight: 600; }
.available-number { margin: 8px 0; font-size: clamp(54px, 9vw, 86px); font-weight: 750; line-height: 1; font-variant-numeric: tabular-nums; }
.availability-copy { color: #dce8df; }

.summary-grid {
  min-width: min(520px, 55vw);
  display: grid;
  grid-template-columns: repeat(3, minmax(100px, 1fr));
  gap: 12px;
}

.summary-grid div {
  padding: 16px;
  border: 1px solid rgba(255,255,255,0.13);
  border-radius: 12px;
  background: rgba(255,255,255,0.06);
}

dt { color: inherit; }
dd { margin: 6px 0 0; font-size: 24px; font-weight: 700; font-variant-numeric: tabular-nums; }

.parking-section { margin-top: 44px; }
.section-heading { margin-bottom: 20px; }
.section-heading > p { max-width: 440px; font-size: 14px; }

.parking-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(320px, 100%), 1fr));
  gap: 18px;
}

.parking-card {
  padding: 24px;
  border: 1px solid #d9e0da;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(33, 52, 40, 0.06);
}

.availability-badge {
  flex: none;
  padding: 6px 10px;
  border-radius: 999px;
  background: #e7f6ec;
  color: #23663f;
  font-size: 13px;
  font-weight: 700;
}

.availability-badge.unavailable { background: #f1f2f1; color: #657068; }
.primary-count { display: flex; align-items: baseline; gap: 8px; margin: 28px 0 22px; }
.primary-count strong { color: #23663f; font-size: 44px; line-height: 1; font-variant-numeric: tabular-nums; }
.primary-count span { color: #5c6960; }

.status-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0 20px; }
.status-list div { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-top: 1px solid #edf0ed; }
.status-list dt { display: flex; align-items: center; gap: 8px; color: #556159; font-size: 14px; }
.status-list dd { margin: 0; font-size: 16px; }

.status-dot { width: 10px; height: 10px; border-radius: 50%; background: #22c55e; }
.status-dot.reserved { background: #3b82f6; }
.status-dot.charging { background: #f59e0b; }
.status-dot.fault { background: #ef4444; }
.status-dot.maintenance, .status-dot.unavailable { background: #94a3b8; }
.status-dot.unavailable { border-radius: 2px; }

.state-panel {
  padding: 40px;
  border: 1px solid #d9e0da;
  border-radius: 16px;
  background: #fff;
}
.state-panel h2, .state-panel h3 { margin-bottom: 10px; }
.error-state { border-color: #e6c4bd; }
.error-state .el-button { margin-top: 22px; }

@media (max-width: 820px) {
  .page-header, .availability-summary, .section-heading { flex-direction: column; }
  .header-actions { justify-content: flex-start; }
  .summary-grid { min-width: 0; width: 100%; grid-template-columns: repeat(2, 1fr); }
  .section-heading > p { max-width: none; }
}

@media (max-width: 480px) {
  .resident-dashboard { padding: 18px; }
  .summary-grid, .status-list { grid-template-columns: 1fr; }
  .parking-card, .state-panel { padding: 20px; }
  .header-actions .el-button { min-height: 44px; }
}
</style>
