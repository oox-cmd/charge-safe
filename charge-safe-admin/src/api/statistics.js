import request from './request'

export function getDashboard(communityId) {
  return request.get('/api/statistics/dashboard', { params: { communityId } })
}
