/**
 * 车棚与端口 API（居民端）
 */
import request from './request'

/** 获取当前居民所属社区的充电位概览（含车棚列表及聚合统计） */
export function getResidentDashboard() {
  return request({ url: '/api/resident/dashboard', method: 'GET' })
}

/** 按车棚 ID 获取端口明细列表 */
export function listPortsByParkingArea(parkingAreaId) {
  return request({ url: `/api/ports/parking-area/${parkingAreaId}`, method: 'GET' })
}

/** 按车棚 ID 获取空闲端口列表 */
export function listIdlePorts(parkingAreaId) {
  return request({ url: `/api/ports/idle/${parkingAreaId}`, method: 'GET' })
}
