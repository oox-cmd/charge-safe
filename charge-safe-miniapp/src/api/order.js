/**
 * 充电订单 API
 * 对接后端真实端点（ChargingController）。
 */
import request from './request'

/** 我的订单分页：GET /api/orders/my?status&page&size */
export function pageMyOrders(params = {}) {
  return request({ url: '/api/orders/my', method: 'GET', data: params })
}

/** 我的订单详情：GET /api/orders/my/{id} */
export function getMyOrderById(id) {
  return request({ url: `/api/orders/my/${id}`, method: 'GET' })
}
