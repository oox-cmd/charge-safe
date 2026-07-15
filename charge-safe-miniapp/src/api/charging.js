/**
 * 预约与充电 API
 * 对接后端真实端点（ChargingController）。
 */
import request from './request'

/** 创建预约：POST /api/reservations，body: { portId } */
export function createReservation(portId) {
  return request({ url: '/api/reservations', method: 'POST', data: { portId } })
}

/** 取消预约：POST /api/reservations/{id}/cancel */
export function cancelReservation(id) {
  return request({ url: `/api/reservations/${id}/cancel`, method: 'POST' })
}

/** 我的有效预约（单个）：GET /api/reservations/my */
export function getMyActiveReservation() {
  return request({ url: '/api/reservations/my', method: 'GET' })
}

/** 我的预约分页：GET /api/reservations/my/page?status&page&size */
export function pageMyReservations(params = {}) {
  return request({ url: '/api/reservations/my/page', method: 'GET', data: params })
}

/** 开始充电（凭预约）：POST /api/charging/start?reservationId= */
export function startCharging(reservationId) {
  return request({ url: '/api/charging/start', method: 'POST', data: { reservationId } })
}

/** 无预约直接开始充电：POST /api/charging/start-direct?portId= */
export function startChargingDirectly(portId) {
  return request({ url: '/api/charging/start-direct', method: 'POST', data: { portId } })
}

/** 结束充电：POST /api/charging/{orderId}/finish */
export function finishCharging(orderId) {
  return request({ url: `/api/charging/${orderId}/finish`, method: 'POST' })
}
