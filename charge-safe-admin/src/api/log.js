import request from './request'

export function getLogs(params) {
  return request.get('/api/logs', { params })
}

export function getLog(id) {
  return request.get(`/api/logs/${id}`)
}
