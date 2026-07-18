import request from './request'

export function createRepairReport(data) {
  return request({ url: '/api/resident/repair-reports', method: 'POST', data })
}

export function pageMyRepairReports(params) {
  return request({ url: '/api/resident/repair-reports', method: 'GET', data: params })
}

export function getMyRepairReport(id) {
  return request({ url: `/api/resident/repair-reports/${id}`, method: 'GET' })
}
