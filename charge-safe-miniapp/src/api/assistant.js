import request from './request'
import { getConfiguredApiUrl } from '@/config/api.mjs'

/**
 * AI 助手聊天
 * @param {string} question - 用户问题
 * @param {string} pageContext - 页面上下文
 * @returns {Promise}
 */
export const chatWithAssistant = (question, pageContext = '') => {
  return request({
    url: '/api/assistant/chat',
    method: 'POST',
    data: { question, pageContext }
  })
}

/**
 * AI 助手聊天（流式输出）
 * @param {string} question - 用户问题
 * @param {string} pageContext - 页面上下文
 * @param {Function} onMessage - 收到消息的回调
 * @param {Function} onDone - 完成的回调
 * @param {Function} onError - 错误的回调
 */
export const chatWithAssistantStream = (question, pageContext = '', onMessage, onDone, onError) => {
  const token = uni.getStorageSync('token')
  const url = getConfiguredApiUrl('/api/assistant/chat/stream')

  // 使用 uni.request 的 SSE 方式
  uni.request({
    url,
    method: 'POST',
    data: { question, pageContext },
    header: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    success: (res) => {
      if (res.statusCode === 200) {
        // 解析 SSE 数据
        const data = res.data
        if (data && data.answer) {
          onMessage(data)
        }
        onDone()
      } else {
        onError(new Error('请求失败'))
      }
    },
    fail: (err) => {
      onError(err)
    }
  })
}
