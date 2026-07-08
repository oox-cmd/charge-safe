/**
 * 用户 API
 */
import request from './request'

export function getUserInfo() {
  return request({ url: '/api/user/info', method: 'GET' })
}

export function updateProfile(params) {
  return request({ url: '/api/user/profile', method: 'PUT', data: params })
}

export function changePassword(params) {
  return request({ url: '/api/user/password', method: 'PUT', data: params })
}

export function getUnreadCount() {
  return request({ url: '/api/user/messages/unread-count', method: 'GET' })
}

/**
 * 上传头像
 */
export function uploadAvatar(filePath) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.uploadFile({
      url: 'http://localhost:8080/api/user/avatar',
      filePath,
      name: 'file',
      header: token ? { 'Authorization': `Bearer ${token}` } : {},
      success(res) {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) resolve(data)
          else reject(new Error(data.message))
        } catch { reject(new Error('解析失败')) }
      },
      fail: () => reject(new Error('上传失败'))
    })
  })
}
