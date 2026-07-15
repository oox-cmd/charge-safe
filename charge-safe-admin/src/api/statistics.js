import request from './request'

export function getManagementBigscreen(communityId) {
  return request.get('/api/statistics/bigscreen', { params: { communityId } })
}

export function getDashboard(communityId) {
  return request.get('/api/statistics/dashboard', { params: { communityId } })
}

export function getPortStatus(communityId) {
  return request.get('/api/statistics/port-status', { params: { communityId } })
}

export function getOrderTrend(communityId) {
  return request.get('/api/statistics/order-trend', { params: { communityId } })
}

export function getRepairSummary(communityId) {
  return request.get('/api/statistics/repair-summary', { params: { communityId } })
}

export function getHazardSummary(communityId) {
  return request.get('/api/statistics/hazard-summary', { params: { communityId } })
}

export function getParkingAreaUsage(communityId) {
  return request.get('/api/statistics/parking-area-usage', { params: { communityId } })
}
