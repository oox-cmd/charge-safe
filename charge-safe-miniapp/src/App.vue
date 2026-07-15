<script>
import { USE_MOCK } from '@/api/request'

export default {
  onLaunch: function () {
    console.log('App Launch')
    if (USE_MOCK) {
      uni.showToast({ title: '开发模拟数据模式', icon: 'none', duration: 3000 })
    }
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  },
}
</script>

<style>
/*每个页面公共css */
</style>
