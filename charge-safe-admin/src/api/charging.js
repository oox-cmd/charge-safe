import request from './request'

// 预约
export function getReservations(params) {
  return request.get('/api/reservations', { params })
}

export function createReservation(data) {
  return request.post('/api/reservations', data)
}

export function cancelReservation(id) {
  return request.post(`/api/reservations/${id}/cancel`)
}

export function getMyReservation() {
  return request.get('/api/reservations/my')
}

// 充电
export function startCharging(reservationId) {
  return request.post('/api/charging/start', null, { params: { reservationId } })
}

export function finishCharging(orderId) {
  return request.post(`/api/charging/${orderId}/finish`)
}

// 订单
export function getOrders(params) {
  return request.get('/api/orders', { params })
}

export function getMyOrders(params) {
  return request.get('/api/orders/my', { params })
}

export function getOrder(id) {
  return request.get(`/api/orders/${id}`)
}
