import request from './request'

// 巡检任务
export function getInspectionTasks(params) {
  return request.get('/api/inspection-tasks', { params })
}

export function getMyInspectionTasks(params) {
  return request.get('/api/inspection-tasks/my', { params })
}

export function getInspectionTask(id) {
  return request.get(`/api/inspection-tasks/${id}`)
}

export function createInspectionTask(data) {
  return request.post('/api/inspection-tasks', data)
}

export function startInspectionTask(id) {
  return request.post(`/api/inspection-tasks/${id}/start`)
}

export function submitInspection(id, data) {
  return request.post(`/api/inspection-tasks/${id}/submit`, data)
}

export function completeInspectionTask(id) {
  return request.post(`/api/inspection-tasks/${id}/complete`)
}

// 安全隐患
export function getHazards(params) {
  return request.get('/api/hazards', { params })
}

export function getHazard(id) {
  return request.get(`/api/hazards/${id}`)
}

export function reportHazard(data) {
  return request.post('/api/hazards', data)
}

export function rectifyHazard(id, data) {
  return request.post(`/api/hazards/${id}/rectify`, data)
}

export function reviewHazard(id, data) {
  return request.post(`/api/hazards/${id}/review`, data)
}

export function closeHazard(id) {
  return request.post(`/api/hazards/${id}/close`)
}
