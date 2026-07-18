export const PORT_STATUS_STYLES = Object.freeze({
  IDLE: Object.freeze({ label: '空闲', color: '#22c55e' }),
  RESERVED: Object.freeze({ label: '已预约', color: '#3b82f6' }),
  CHARGING: Object.freeze({ label: '充电中', color: '#f59e0b' }),
  COMPLETED_WAITING_PICKUP: Object.freeze({ label: '待取车', color: '#8b5cf6' }),
  OVERTIME_OCCUPIED: Object.freeze({ label: '超时占位', color: '#f97316' }),
  FAULT: Object.freeze({ label: '故障', color: '#ef4444' }),
  MAINTENANCE: Object.freeze({ label: '维修中', color: '#94a3b8' }),
  DISABLED: Object.freeze({ label: '停用', color: '#64748b' }),
  OFFLINE: Object.freeze({ label: '离线', color: '#475569' })
})

const UNKNOWN_STATUS_STYLE = Object.freeze({ label: '未知状态', color: '#cbd5e1' })

function isFiniteCoordinate(value) {
  return typeof value === 'number' && Number.isFinite(value)
}

export function buildCommunityMarkers(communities = []) {
  return communities
    .filter((community) => community?.id != null
      && isFiniteCoordinate(community.lat)
      && isFiniteCoordinate(community.lng))
    .map((community) => ({
      id: community.id,
      name: community.name || `社区 ${community.id}`,
      lat: community.lat,
      lng: community.lng,
      color: '#14b8a6'
    }))
}

export function selectAreasForCommunity(areas = [], communityId) {
  return areas.filter((area) => area?.id != null && String(area.communityId) === String(communityId))
}

export function getPortStatusStyle(status) {
  return PORT_STATUS_STYLES[status] || UNKNOWN_STATUS_STYLE
}

export const SIMULATION_DISCLAIMER = '演示环境：数据来自业务数据库与设备模拟器，不代表真实硬件实时采集。空间布局为示意，不是测绘地图。'
