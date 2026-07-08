/**
 * 小区 API
 */
import request from './request'

/** 获取小区列表 */
export function getCommunityList() {
  return request({ url: '/api/communities', method: 'GET' })
}

/** 获取全部小区（无分页，给选择器用） */
export function getAllCommunities() {
  return request({ url: '/api/communities', method: 'GET' })
}

/** 根据坐标匹配最近小区 */
export function getNearestCommunity(lat, lng) {
  return request({ url: '/api/communities/nearest', method: 'GET', data: { lat, lng } })
}
