# ChargeSafe 三级大屏数据与场景建模

## 1. 设计原则

- 业务 ID 是事实，模型节点名只是资源实现细节。
- 当前状态、场景配置和统计指标分层管理。
- MVP 不新增数据库迁移；坐标放在前端显式配置中。
- 模拟状态必须标记 `source: demo`，不得伪装成真实设备数据。
- 同一端口在场景、详情和统计中由同一个 `portId` 驱动。

## 2. 三层状态机

```ts
type SceneLevel = 'globe' | 'community' | 'station'

interface DashboardSceneState {
  sceneLevel: SceneLevel
  selectedCommunityId: number | null
  selectedStationId: number | null
  selectedPortId: number | null
  cameraState: 'idle' | 'moving' | 'user-controlled'
  transitionState: 'idle' | 'loading' | 'entering' | 'leaving' | 'failed'
  loadingState: Record<SceneLevel, boolean>
  errorState: { level: SceneLevel; message: string } | null
}
```

合法主转移：`globe -> community -> station` 与逆向返回。转场期间禁用重复进入，但必须允许取消或返回，失败后保留上一级可用场景。

## 3. 场景配置模型

```ts
interface CommunitySceneConfig {
  communityId: number
  name: string
  approximateLocation: { lat: number; lng: number }
  cameraPreset: CameraPreset
  buildings: BuildingPlacement[]
  stationMarkers: StationMarker[]
}

interface StationSceneConfig {
  stationId: number
  communityId: number
  modelUrl?: string
  cameraPreset: CameraPreset
  portMappings: PortSceneMapping[]
  equipmentMappings: EquipmentSceneMapping[]
  alarmAnchors: AlarmAnchor[]
}

interface PortSceneMapping {
  portId: number
  stationId: number
  sceneObjectId: string
  instanceIndex?: number
  position: [number, number, number]
  rotation: [number, number, number]
  labelAnchor: [number, number, number]
  defaultState: PortVisualState
}
```

`sceneObjectId` 只用于查找场景对象；更新业务状态时先按 `portId` 查映射。模型换版可以改 `sceneObjectId` 而不改变后端 ID。

## 4. 业务数据 DTO 建议

### 4.1 一级社区摘要

```json
{
  "communityId": 1,
  "name": "示例社区",
  "lat": 0,
  "lng": 0,
  "stationCount": 2,
  "totalPorts": 40,
  "faultCount": 1,
  "alarmLevel": "WARNING",
  "updatedAt": "ISO-8601"
}
```

`lat/lng` 可由 V4 社区字段提供；页面需明确“示意位置”。

### 4.2 二级站点状态

```ts
interface StationStatus {
  stationId: number
  communityId: number
  name: string
  totalPorts: number
  idleCount: number
  reservedCount: number
  chargingCount: number
  completedWaitingPickupCount: number
  overtimeOccupiedCount: number
  faultCount: number
  maintenanceCount: number
  disabledCount: number
  offlineCount: number
  alarmLevel: 'NONE' | 'INFO' | 'WARNING' | 'CRITICAL'
  updatedAt: string
}
```

当前 `ParkingAreaUsageVO` 只有 total/idle/charging/fault/usageRate，不足以表达全部状态；下一阶段可新增 VO/查询，但不在本轮修改接口。

### 4.3 三级端口状态

```ts
interface PortStatus {
  portId: number
  deviceId: number
  stationId: number
  portNumber: number
  portName: string
  status: PortVisualState
  reservationId?: number
  chargingOrderId?: number
  updatedAt: string
}
```

只返回展示必要信息，不把居民手机号、JWT、完整订单或敏感日志送入大屏。大屏默认处于管理端认证和权限体系内，不以公开页面为设计前提。

## 5. 状态视觉矩阵

后端真实端口状态共 9 种，不能只实现用户初稿中的 5 种。

| 后端原始状态 | 业务含义 | 大屏分组 | 颜色 | 图标/文字 | 是否告警 |
| --- | --- | --- | --- | --- | --- |
| IDLE | 可预约 | IDLE | `#22c55e` | 空心圆/“空闲” | 否 |
| RESERVED | 已预约 | RESERVED | `#3b82f6` | 时钟/“预约” | 否 |
| CHARGING | 充电中 | CHARGING | `#f59e0b` | 闪电/“充电” | 否 |
| COMPLETED_WAITING_PICKUP | 已结束待取车 | CHARGING | `#f59e0b` | 对勾加车辆/“待取车” | 否，可提示超时阈值 |
| OVERTIME_OCCUPIED | 超时占位 | FAULT | `#ef4444` | 计时警告/“超时” | 是 |
| FAULT | 故障 | FAULT | `#ef4444` | 叉号/“故障” | 是 |
| MAINTENANCE | 维修中 | MAINTENANCE | `#94a3b8` | 扳手/“维修” | 是，运维事件 |
| DISABLED | 停用 | MAINTENANCE | `#94a3b8` | 禁止/“停用” | 否，需显式展示 |
| OFFLINE | 离线 | FAULT | `#ef4444` | 断链/“离线” | 是 |

图例、详情面板、实例材质和统计数必须从同一状态元数据表生成。

## 6. 数据来源与缺口

| 数据 | 当前事实源 | MVP 策略 | 后期策略 |
| --- | --- | --- | --- |
| 社区经纬度 | `community.lat/lng` | 直接使用并标记近似 | 需要时校准 |
| 站点空间位置 | 无 | `communitySceneConfig` | 多场景后台配置稳定后再迁移 |
| 端口空间位置 | 无 | `stationSceneConfig.portMappings` | 确有运营维护需求后再迁移 |
| 端口当前状态 | `charging_port.status` | 新增只读端口级 DTO，10-15 秒可配置轮询 | 准实时需求成立后评估 WebSocket/SSE |
| 工单/隐患 | 现有表与服务 | 精简聚合 | 增加站点维度 |
| 告警 | 仅 `alarm_record` 表 | MVP 可用“故障/隐患”并明确来源 | 实现正式告警模块 |
| 时间回放 | 无状态历史 | 不做 | 设计历史表及新 Flyway |

## 7. 接口组合建议

MVP 可规划但本轮不实现：

- `GET /api/dashboard-3d/communities`：一级摘要。
- `GET /api/dashboard-3d/communities/{id}/stations`：二级站点汇总。
- `GET /api/dashboard-3d/stations/{id}/ports`：三级端口列表。
- `GET /api/dashboard-3d/stations/{id}/events`：精简工单/隐患/故障事件。

是否新建路径需与现有 `/api/statistics` 归属比较后通过 ADR。接口必须复用 DataScope，不能因大屏而绕过认证。

## 8. 前端模块边界建议

```text
views/bigscreen3d/
├── Index.vue                 # 页面壳与可访问降级
├── composables/
│   ├── useSceneState.ts      # 三层状态机
│   ├── useSceneResources.ts  # 加载、缓存、dispose
│   └── useDashboardData.ts   # API/轮询/错误
├── scenes/
│   ├── GlobeScene.ts
│   ├── CommunityScene.ts
│   └── StationScene.ts
├── config/
│   ├── communities.ts
│   └── stations.ts
├── domain/
│   ├── port-status.ts
│   └── scene-mapping.ts
└── components/
    ├── SceneBreadcrumb.vue
    ├── StationSummary.vue
    ├── PortDetails.vue
    └── SceneFallback.vue
```

现有 `bigscreen/Index.vue` 在迁移完成前保留为二维页，不直接把 Three 逻辑继续塞入 987 行单文件。MVP 使用原生 Three.js；Vue 只负责页面、状态与生命周期，不引入 TresJS，也不并行维护两套实现。

## 9. 刷新、失败与一致性

- 首次进入按层请求；一级不预加载全部站点模型。
- 可见页默认每 10-15 秒轮询，可通过配置调整；页面隐藏或场景切换时暂停轮询和渲染。
- 同一轮响应带 `updatedAt`，旧响应不得覆盖新状态。
- 端口状态更新先更新 domain store，再由场景和面板共同订阅。
- 请求失败保留最后一次成功值并显示“数据更新时间/连接异常”，不显示假正常。
- 不支持 WebGL 时显示二维站点与端口列表，核心状态仍可读。

## 10. 为什么现在不新增迁移

当前只有一个演示场景方向，场景坐标、模型拆分和可配置需求尚未通过原型。MVP 使用真实 `communityId`、`stationId`、`deviceId`、`portId` 和业务状态，程序化建筑、站点与端口的场景坐标保存在前端显式配置中，且不得伪装成经纬度。直接加字段会把渲染实现固化进业务数据库；只有后续需要后台配置多个场景时，才重新设计新表和新版本迁移。当前不新增迁移，任何未来迁移都必须从 V6 以后开始，绝不修改 V1-V5。
