import { getConfiguredApiUrl } from '@/config/api.mjs'
import { parseApiResponse } from './response.mjs'

export { USE_MOCK } from '@/config/runtime.mjs'

const request = (options) => {
  return new Promise((resolve, reject) => {
    let url
    try {
      url = getConfiguredApiUrl(options.url)
    } catch (error) {
      uni.showToast({ title: error.message, icon: 'none' })
      reject(error)
      return
    }

    const token = options.skipAuth ? '' : uni.getStorageSync('token')
    const header = { 'Content-Type': 'application/json' }
    if (token) header.Authorization = `Bearer ${token}`

    uni.request({
      url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success: (res) => {
        try {
          resolve(parseApiResponse(res))
        } catch (error) {
          if (error.statusCode === 401 || error.code === 401) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.redirectTo({ url: '/pages/login' })
          } else {
            uni.showToast({ title: error.message, icon: 'none' })
          }
          reject(error)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default request
