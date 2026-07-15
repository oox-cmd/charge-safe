import request from './request'

export function getResidentDashboard() {
  return request.get('/api/resident/dashboard')
}
