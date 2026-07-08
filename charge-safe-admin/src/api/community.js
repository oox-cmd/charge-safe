import request from './request'

// 社区
export function getCommunities(params) {
  return request.get('/api/communities', { params })
}

export function getCommunityList() {
  return request.get('/api/communities/list')
}

export function getCommunity(id) {
  return request.get(`/api/communities/${id}`)
}

export function createCommunity(data) {
  return request.post('/api/communities', data)
}

export function updateCommunity(id, data) {
  return request.put(`/api/communities/${id}`, data)
}

export function updateCommunityStatus(id, status) {
  return request.put(`/api/communities/${id}/status`, null, { params: { status } })
}

// 车棚
export function getParkingAreas(params) {
  return request.get('/api/parking-areas', { params })
}

export function getParkingAreaList(communityId) {
  return request.get('/api/parking-areas/list', { params: { communityId } })
}

export function getParkingArea(id) {
  return request.get(`/api/parking-areas/${id}`)
}

export function createParkingArea(data) {
  return request.post('/api/parking-areas', data)
}

export function updateParkingArea(id, data) {
  return request.put(`/api/parking-areas/${id}`, data)
}

export function deleteParkingArea(id) {
  return request.delete(`/api/parking-areas/${id}`)
}
