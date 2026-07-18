import { onMounted, onUnmounted, ref } from 'vue'
import request from '@/api/request'

const POLL_INTERVAL_MS = 15_000

export function useBigscreenData() {
  const bigscreenData = ref(null)
  const loading = ref(true)
  const error = ref('')
  const lastUpdatedAt = ref(null)
  let timer = null

  const stopPolling = () => {
    if (timer) clearInterval(timer)
    timer = null
  }

  const fetchData = async () => {
    if (document.hidden) return
    try {
      const res = await request.get('/api/statistics/bigscreen')
      if (res.code !== 200) throw new Error(res.message || '大屏数据请求失败')
      bigscreenData.value = res.data
      lastUpdatedAt.value = new Date()
      error.value = ''
    } catch (cause) {
      error.value = cause?.message || '无法获取大屏数据，请检查网络、登录状态和接口权限。'
    } finally {
      loading.value = false
    }
  }

  const startPolling = () => {
    if (timer || document.hidden) return
    timer = setInterval(fetchData, POLL_INTERVAL_MS)
  }

  const onVisibilityChange = () => {
    if (document.hidden) stopPolling()
    else {
      fetchData()
      startPolling()
    }
  }

  onMounted(() => {
    fetchData()
    startPolling()
    document.addEventListener('visibilitychange', onVisibilityChange)
  })

  onUnmounted(() => {
    stopPolling()
    document.removeEventListener('visibilitychange', onVisibilityChange)
  })

  return { bigscreenData, loading, error, lastUpdatedAt, fetchData }
}
