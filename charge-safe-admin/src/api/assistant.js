import request from '@/api/request'

/**
 * 获取 AI 助手统计数据
 * @returns {Promise}
 */
export const getAssistantStats = () => {
  return request({
    url: '/api/assistant/stats',
    method: 'GET'
  })
}
