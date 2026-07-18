import test from 'node:test'
import assert from 'node:assert/strict'

import {
  buildCommunityMarkers,
  getPortStatusStyle,
  selectAreasForCommunity
} from '../src/views/bigscreen3d/domain/sceneData.mjs'

test('只为带真实 ID 和经纬度的社区创建地球标记', () => {
  const markers = buildCommunityMarkers([
    { id: 7, name: '东苑社区', lat: 22.54, lng: 114.05 },
    { id: 8, name: '缺少坐标的社区', lat: null, lng: 114.08 },
    { id: null, name: '缺少业务 ID', lat: 22.55, lng: 114.09 }
  ])

  assert.deepEqual(markers, [{
    id: 7,
    name: '东苑社区',
    lat: 22.54,
    lng: 114.05,
    color: '#14b8a6'
  }])
})

test('二级车棚严格按真实 communityId 过滤', () => {
  const areas = [
    { id: 101, communityId: 7 },
    { id: 102, communityId: 8 },
    { id: 103, communityId: 7 }
  ]

  assert.deepEqual(selectAreasForCommunity(areas, 7).map((area) => area.id), [101, 103])
})

test('九种端口状态都有独立的展示语义', () => {
  const statuses = [
    'IDLE', 'RESERVED', 'CHARGING', 'COMPLETED_WAITING_PICKUP', 'OVERTIME_OCCUPIED',
    'FAULT', 'MAINTENANCE', 'DISABLED', 'OFFLINE'
  ]

  const styles = statuses.map(getPortStatusStyle)
  assert.equal(new Set(styles.map((style) => style.label)).size, 9)
  assert.equal(getPortStatusStyle('UNKNOWN').label, '未知状态')
})
