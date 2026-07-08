# ChargeSafe 数据库设计文档

> 生成时间：2026-07-04

---

## 1. 数据库概述

- **数据库类型**：MySQL 8.x
- **存储引擎**：InnoDB
- **字符集**：utf8mb4
- **迁移工具**：Flyway
- **当前版本**：V2（V1__init_schema.sql + V2__init_data.sql）

---

## 2. 表结构设计

### 2.1 用户权限线

#### sys_user（用户表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 用户ID | 主键，自增 |
| username | VARCHAR(50) | 用户名 | 唯一 |
| password | VARCHAR(100) | 密码 | BCrypt加密 |
| nickname | VARCHAR(50) | 昵称 | 可空 |
| phone | VARCHAR(20) | 手机号 | 可空 |
| avatar | VARCHAR(255) | 头像URL | 可空 |
| status | TINYINT | 状态 | 0-禁用 1-启用 |
| community_id | BIGINT | 所属社区ID | 物业/运维/巡检人员关联 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：uk_username, idx_community_id

#### sys_role（角色表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 角色ID | 主键，自增 |
| role_name | VARCHAR(50) | 角色名称 | |
| role_code | VARCHAR(50) | 角色编码 | 唯一 |
| description | VARCHAR(200) | 角色描述 | 可空 |
| status | TINYINT | 状态 | 0-禁用 1-启用 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：uk_role_code

#### sys_user_role（用户角色关联表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | ID | 主键，自增 |
| user_id | BIGINT | 用户ID | |
| role_id | BIGINT | 角色ID | |
| created_time | DATETIME | 创建时间 | 自动 |

**索引**：uk_user_role, idx_role_id

#### sys_menu（菜单权限表）- 需新增

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 菜单ID | 主键，自增 |
| parent_id | BIGINT | 父菜单ID | 默认0 |
| menu_name | VARCHAR(50) | 菜单名称 | |
| menu_type | TINYINT | 菜单类型 | 1-目录 2-菜单 3-按钮 |
| path | VARCHAR(200) | 路由路径 | |
| component | VARCHAR(200) | 组件路径 | |
| icon | VARCHAR(50) | 图标 | 可空 |
| sort_order | INT | 排序 | 默认0 |
| status | TINYINT | 状态 | 0-禁用 1-启用 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

#### sys_role_menu（角色菜单关联表）- 需新增

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | ID | 主键，自增 |
| role_id | BIGINT | 角色ID | |
| menu_id | BIGINT | 菜单ID | |
| created_time | DATETIME | 创建时间 | 自动 |

**索引**：uk_role_menu

---

### 2.2 社区资源线

#### community（社区表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 社区ID | 主键，自增 |
| name | VARCHAR(100) | 社区名称 | |
| address | VARCHAR(255) | 社区地址 | 可空 |
| contact_name | VARCHAR(50) | 联系人 | 可空 |
| contact_phone | VARCHAR(20) | 联系电话 | 可空 |
| status | TINYINT | 状态 | 0-停用 1-启用 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

#### parking_area（车棚表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 车棚ID | 主键，自增 |
| community_id | BIGINT | 所属社区ID | |
| name | VARCHAR(100) | 车棚名称 | |
| location | VARCHAR(255) | 位置描述 | 可空 |
| port_count | INT | 端口总数 | 默认0 |
| status | TINYINT | 状态 | 0-停用 1-启用 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：idx_community_id

#### charging_device（充电设备表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 设备ID | 主键，自增 |
| parking_area_id | BIGINT | 所属车棚ID | |
| device_code | VARCHAR(50) | 设备编码 | 唯一 |
| device_name | VARCHAR(100) | 设备名称 | |
| device_type | VARCHAR(50) | 设备类型 | 可空 |
| port_count | INT | 端口数量 | 默认0 |
| status | TINYINT | 状态 | 0-停用 1-启用 2-故障 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：uk_device_code, idx_parking_area_id

#### charging_port（充电端口表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 端口ID | 主键，自增 |
| device_id | BIGINT | 所属设备ID | |
| port_number | INT | 端口号 | |
| port_name | VARCHAR(50) | 端口名称 | 可空 |
| status | VARCHAR(30) | 状态 | 见状态枚举 |
| power_rating | DECIMAL(10,2) | 额定功率(W) | 可空 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：uk_device_port, idx_status

**端口状态枚举**：

| 状态 | 含义 | 可流转到 |
|------|------|----------|
| IDLE | 空闲 | RESERVED, FAULT, DISABLED, OFFLINE |
| RESERVED | 已预约 | CHARGING, IDLE(超时取消) |
| CHARGING | 充电中 | IDLE(结束充电) |
| FAULT | 故障 | MAINTENANCE |
| MAINTENANCE | 维修中 | IDLE |
| DISABLED | 停用 | IDLE |
| OFFLINE | 离线 | IDLE |

---

### 2.3 预约与充电线

#### charging_reservation（充电预约表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 预约ID | 主键，自增 |
| user_id | BIGINT | 预约用户ID | |
| port_id | BIGINT | 预约端口ID | |
| community_id | BIGINT | 社区ID | 可空 |
| reserve_time | DATETIME | 预约时间 | 自动 |
| expire_time | DATETIME | 预约过期时间 | |
| status | VARCHAR(20) | 状态 | 见状态枚举 |
| cancel_reason | VARCHAR(200) | 取消原因 | 可空 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：idx_user_id, idx_port_id, idx_status

**预约状态枚举**：

| 状态 | 含义 |
|------|------|
| PENDING | 待生效 |
| CONVERTED | 已转订单 |
| CANCELLED | 用户取消 |
| EXPIRED | 超时取消 |
| FAULT_CANCELLED | 设备故障取消 |

#### charging_order（充电订单表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 订单ID | 主键，自增 |
| order_no | VARCHAR(50) | 订单编号 | 唯一 |
| user_id | BIGINT | 用户ID | |
| port_id | BIGINT | 端口ID | |
| reservation_id | BIGINT | 关联预约ID | 可空 |
| community_id | BIGINT | 社区ID | 可空 |
| start_time | DATETIME | 开始时间 | 可空 |
| end_time | DATETIME | 结束时间 | 可空 |
| duration_minutes | INT | 充电时长(分钟) | 可空 |
| power_consumed | DECIMAL(10,2) | 用电量(度) | 可空 |
| amount | DECIMAL(10,2) | 费用(元) | 默认0.00 |
| status | VARCHAR(30) | 状态 | 见状态枚举 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：uk_order_no, idx_user_id, idx_port_id, idx_status

**订单状态枚举**：

| 状态 | 含义 |
|------|------|
| CHARGING | 充电中 |
| COMPLETED | 已完成 |
| SETTLED | 已结算 |
| CANCELLED | 已取消 |
| ABNORMAL | 异常结束 |

---

### 2.4 报修与工单线

#### repair_report（报修记录表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 报修ID | 主键，自增 |
| reporter_id | BIGINT | 报修人ID | |
| community_id | BIGINT | 社区ID | |
| device_id | BIGINT | 设备ID | 可空 |
| port_id | BIGINT | 端口ID | 可空 |
| fault_type | VARCHAR(50) | 故障类型 | 可空 |
| fault_desc | VARCHAR(500) | 故障描述 | |
| fault_images | TEXT | 故障照片 | JSON数组，可空 |
| status | VARCHAR(20) | 状态 | 见状态枚举 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：idx_reporter_id, idx_community_id, idx_device_id

#### repair_ticket（维修工单表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 工单ID | 主键，自增 |
| ticket_no | VARCHAR(50) | 工单编号 | 唯一 |
| report_id | BIGINT | 关联报修ID | 可空 |
| community_id | BIGINT | 社区ID | |
| device_id | BIGINT | 设备ID | 可空 |
| port_id | BIGINT | 端口ID | 可空 |
| dispatcher_id | BIGINT | 派单人ID | 可空 |
| assignee_id | BIGINT | 维修人员ID | 可空 |
| fault_desc | VARCHAR(500) | 故障描述 | 可空 |
| repair_requirement | VARCHAR(500) | 维修要求 | 可空 |
| repair_result | VARCHAR(500) | 维修结果 | 可空 |
| repair_images | TEXT | 维修照片 | JSON数组，可空 |
| status | VARCHAR(30) | 状态 | 见状态枚举 |
| dispatch_time | DATETIME | 派单时间 | 可空 |
| accept_time | DATETIME | 接单时间 | 可空 |
| finish_time | DATETIME | 完成时间 | 可空 |
| close_time | DATETIME | 关闭时间 | 可空 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：uk_ticket_no, idx_report_id, idx_community_id, idx_assignee_id, idx_status

**工单状态枚举**：

| 状态 | 含义 | 可流转到 |
|------|------|----------|
| PENDING | 待受理 | DISPATCHED |
| DISPATCHED | 已派单 | PROCESSING |
| PROCESSING | 处理中 | WAITING_ACCEPTANCE |
| WAITING_ACCEPTANCE | 待验收 | FINISHED, REJECTED |
| FINISHED | 已完成 | CLOSED |
| CLOSED | 已关闭 | - |
| REJECTED | 已驳回 | DISPATCHED |

---

### 2.5 巡检与隐患线

#### inspection_task（巡检任务表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 任务ID | 主键，自增 |
| community_id | BIGINT | 社区ID | |
| task_name | VARCHAR(100) | 任务名称 | |
| task_desc | VARCHAR(500) | 任务描述 | 可空 |
| inspector_id | BIGINT | 巡检人员ID | 可空 |
| plan_start_time | DATETIME | 计划开始时间 | 可空 |
| plan_end_time | DATETIME | 计划结束时间 | 可空 |
| actual_start_time | DATETIME | 实际开始时间 | 可空 |
| actual_end_time | DATETIME | 实际结束时间 | 可空 |
| status | VARCHAR(20) | 状态 | 见状态枚举 |
| created_by | BIGINT | 创建人ID | 可空 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：idx_community_id, idx_inspector_id, idx_status

#### safety_hazard（安全隐患表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 隐患ID | 主键，自增 |
| community_id | BIGINT | 社区ID | |
| task_id | BIGINT | 关联巡检任务ID | 可空 |
| reporter_id | BIGINT | 上报人ID | |
| hazard_type | VARCHAR(50) | 隐患类型 | |
| severity | VARCHAR(20) | 严重程度 | GENERAL/SERIOUS/URGENT |
| description | VARCHAR(500) | 隐患描述 | |
| images | TEXT | 照片 | JSON数组，可空 |
| status | VARCHAR(30) | 状态 | 见状态枚举 |
| rectifier_id | BIGINT | 整改人ID | 可空 |
| rectify_desc | VARCHAR(500) | 整改情况 | 可空 |
| rectify_images | TEXT | 整改照片 | 可空 |
| rectify_time | DATETIME | 整改时间 | 可空 |
| reviewer_id | BIGINT | 复查人ID | 可空 |
| review_result | VARCHAR(20) | 复查结果 | 可空 |
| review_remark | VARCHAR(500) | 复查备注 | 可空 |
| review_time | DATETIME | 复查时间 | 可空 |
| created_time | DATETIME | 创建时间 | 自动 |
| updated_time | DATETIME | 更新时间 | 自动 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

**索引**：idx_community_id, idx_reporter_id, idx_status

**隐患状态枚举**：

| 状态 | 含义 | 可流转到 |
|------|------|----------|
| PENDING | 待处理 | RECTIFYING |
| RECTIFYING | 整改中 | WAITING_REVIEW |
| WAITING_REVIEW | 待复查 | REVIEW_PASSED, REVIEW_FAILED |
| REVIEW_PASSED | 复查通过 | CLOSED |
| REVIEW_FAILED | 复查不通过 | RECTIFYING |
| CLOSED | 已关闭 | - |

---

### 2.6 消息与统计线

#### notification（消息通知表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 消息ID | 主键，自增 |
| user_id | BIGINT | 接收用户ID | |
| title | VARCHAR(100) | 消息标题 | |
| content | VARCHAR(500) | 消息内容 | 可空 |
| type | VARCHAR(30) | 消息类型 | RESERVATION/CHARGING/REPAIR/INSPECTION/HAZARD/SYSTEM |
| business_type | VARCHAR(50) | 业务类型 | 可空 |
| business_id | BIGINT | 业务ID | 可空 |
| is_read | TINYINT | 是否已读 | 0-未读 1-已读 |
| created_time | DATETIME | 创建时间 | 自动 |

**索引**：idx_user_id, idx_is_read, idx_created_time

#### operation_log（操作日志表）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | BIGINT | 日志ID | 主键，自增 |
| user_id | BIGINT | 操作用户ID | 可空 |
| username | VARCHAR(50) | 操作用户名 | 可空 |
| operation | VARCHAR(50) | 操作描述 | |
| module | VARCHAR(50) | 操作模块 | 可空 |
| method | VARCHAR(200) | 请求方法 | 可空 |
| params | TEXT | 请求参数 | 可空 |
| url | VARCHAR(255) | 请求URL | 可空 |
| ip | VARCHAR(50) | IP地址 | 可空 |
| status | TINYINT | 操作状态 | 0-失败 1-成功 |
| error_msg | TEXT | 错误信息 | 可空 |
| duration | BIGINT | 耗时(毫秒) | 可空 |
| created_time | DATETIME | 创建时间 | 自动 |

**索引**：idx_user_id, idx_module, idx_created_time

---

## 3. 表关系

```
sys_user ──1:N── sys_user_role ──N:1── sys_role
  │
  ├──1:1── resident_profile ──N:1── community
  ├──1:N── vehicle
  ├──1:N── charging_reservation ──N:1── charging_port
  ├──1:N── charging_order
  ├──1:N── repair_report
  └──1:N── notification

community ──1:N── parking_area ──1:N── charging_device ──1:N── charging_port

repair_report ──1:1── repair_ticket ──1:N── repair_ticket_log
inspection_task ──1:N── inspection_record
inspection_task ──1:N── safety_hazard
```

---

## 4. 初始化数据

### 4.1 角色

| ID | 角色名称 | 角色编码 |
|----|----------|----------|
| 1 | 平台管理员 | PLATFORM_ADMIN |
| 2 | 物业管理员 | PROPERTY_ADMIN |
| 3 | 运维人员 | MAINTAINER |
| 4 | 巡检人员 | INSPECTOR |
| 5 | 居民用户 | RESIDENT |

### 4.2 用户

| 用户名 | 密码 | 角色 | 社区 |
|--------|------|------|------|
| admin | 123456 | PLATFORM_ADMIN | - |
| property01 | 123456 | PROPERTY_ADMIN | 阳光花园小区 |
| maintainer01 | 123456 | MAINTAINER | 阳光花园小区 |
| inspector01 | 123456 | INSPECTOR | 阳光花园小区 |
| resident01 | 123456 | RESIDENT | 阳光花园小区 |
| resident02 | 123456 | RESIDENT | 阳光花园小区 |

---

## 5. 设计参考来源

| 参考项目 | 参考内容 |
|----------|----------|
| RuoYi-Vue | sys_user, sys_role, sys_menu 权限表设计 |
| JeecgBoot | 企业级后台表结构、数据权限 |
| EV Charging Station Management | 充电业务表结构 |
| ev-charging-platform | 充电桩数据库设计思路 |
| OpenRemote | 设备告警、运维监控表设计 |
| 禅道/FeelDesk | 工单状态流转设计 |
