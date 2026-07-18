import request from './request'

// 报修
export function getRepairReports(params) {
  return request.get('/api/repair-reports', { params })
}

export function getRepairReport(id) {
  return request.get(`/api/repair-reports/${id}`)
}

export function createRepairReport(data) {
  return request.post('/api/repair-reports', data)
}

// 工单
export function getRepairTickets(params) {
  return request.get('/api/repair-tickets', { params })
}

export function getRepairTicket(id) {
  return request.get(`/api/repair-tickets/${id}`)
}

export function dispatchTicket(data) {
  return request.post('/api/repair-tickets/dispatch', data)
}

export function getDispatchableMaintainers(reportId) {
  return request.get(`/api/repair-reports/${reportId}/dispatchable-maintainers`)
}

export function acceptTicket(id) {
  return request.post(`/api/repair-tickets/${id}/accept`)
}

export function startTicket(id) {
  return request.post(`/api/repair-tickets/${id}/start`)
}

export function finishTicket(id, data) {
  return request.post(`/api/repair-tickets/${id}/finish`, data)
}

export function acceptTicketByProperty(id) {
  return request.post(`/api/repair-tickets/${id}/accept-repair`)
}

export function rejectTicket(id, reason) {
  return request.post(`/api/repair-tickets/${id}/reject`, null, { params: { reason } })
}

export function closeTicket(id) {
  return request.post(`/api/repair-tickets/${id}/close`)
}
