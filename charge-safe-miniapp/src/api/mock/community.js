/**
 * 小区 Mock 数据
 */
import { registerMock } from './registry'

// 5 个测试小区，分布在 3 个坐标区域
const communities = [
  { id: 1, name: '阳光花园小区', address: '朝阳区建国路88号', lat: 39.9087, lng: 116.4716, parkingCount: 3, idlePorts: 8 },
  { id: 2, name: '翠苑新城', address: '海淀区中关村南大街12号', lat: 39.9836, lng: 116.3112, parkingCount: 2, idlePorts: 4 },
  { id: 3, name: '碧水湾家园', address: '朝阳区望京西路36号', lat: 39.9982, lng: 116.4803, parkingCount: 1, idlePorts: 2 },
  { id: 4, name: '金色港湾', address: '丰台区方庄路15号', lat: 39.8654, lng: 116.4237, parkingCount: 2, idlePorts: 5 },
  { id: 5, name: '紫荆公寓', address: '通州区新华大街100号', lat: 39.9023, lng: 116.6688, parkingCount: 1, idlePorts: 0 }
]

// ============ 获取小区列表 ============
registerMock('GET', '/api/communities', () => {
  return { code: 200, message: 'success', data: communities }
})

// ============ 根据坐标匹配最近小区 ============
registerMock('GET', '/api/communities/nearest', (data) => {
  const { lat, lng } = data
  // 简单距离计算，Mock 下直接返回第一个匹配的
  const sorted = [...communities].sort((a, b) => {
    const da = Math.abs(a.lat - (lat || 39.9)) + Math.abs(a.lng - (lng || 116.4))
    const db = Math.abs(b.lat - (lat || 39.9)) + Math.abs(b.lng - (lng || 116.4))
    return da - db
  })
  return { code: 200, message: 'success', data: sorted[0] }
})
