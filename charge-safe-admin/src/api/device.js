import request from './request'

// 设备
export function getDevices(params) {
  return request.get('/api/devices', { params })
}

export function getDeviceList(parkingAreaId) {
  return request.get('/api/devices/list', { params: { parkingAreaId } })
}

export function getDevice(id) {
  return request.get(`/api/devices/${id}`)
}

export function createDevice(data) {
  return request.post('/api/devices', data)
}

export function updateDevice(id, data) {
  return request.put(`/api/devices/${id}`, data)
}

export function updateDeviceStatus(id, status) {
  return request.put(`/api/devices/${id}/status`, null, { params: { status } })
}

// 端口
export function getPorts(params) {
  return request.get('/api/ports', { params })
}

export function getPortList(deviceId) {
  return request.get('/api/ports/list', { params: { deviceId } })
}

export function getPortsByParkingArea(parkingAreaId) {
  return request.get(`/api/ports/parking-area/${parkingAreaId}`)
}

export function getIdlePorts(parkingAreaId) {
  return request.get(`/api/ports/idle/${parkingAreaId}`)
}

export function getPort(id) {
  return request.get(`/api/ports/${id}`)
}

export function createPort(data) {
  return request.post('/api/ports', data)
}

export function batchCreatePorts(deviceId, count) {
  return request.post('/api/ports/batch', null, { params: { deviceId, count } })
}

export function updatePortStatus(id, status) {
  return request.put(`/api/ports/${id}/status`, null, { params: { status } })
}

export function getPortStatistics(communityId) {
  return request.get('/api/ports/statistics', { params: { communityId } })
}

// ==================== 设备分类管理 ====================

export function getDeviceCategories() {
  return request.get('/api/devices/categories')
}

export function createDeviceCategory(data) {
  return request.post('/api/devices/categories', data)
}

export function updateDeviceCategory(id, data) {
  return request.put(`/api/devices/categories/${id}`, data)
}

export function deleteDeviceCategory(id) {
  return request.delete(`/api/devices/categories/${id}`)
}
