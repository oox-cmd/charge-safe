import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/api/request'

export function useBigscreenData() {
  const bigscreenData = ref(null)
  const loading = ref(true)
  let timer = null

  const fetchData = async () => {
    try {
      const res = await request.get('/api/statistics/bigscreen')
      if (res.code === 200) {
        bigscreenData.value = res.data
      }
    } catch (error) {
      console.error('Failed to fetch bigscreen data:', error)
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    fetchData()
    // 轮询：15秒
    timer = setInterval(fetchData, 15000)
  })

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  return {
    bigscreenData,
    loading
  }
}
