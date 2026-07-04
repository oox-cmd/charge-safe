# ChargeSafe 当前源码现状盘点

> 生成时间：2026-07-04
> 分析范围：后端源码、前端管理端、小程序端、数据库迁移文件

---

## 1. 项目整体结构

```
charge-safe/
├── charge-safe-backend/          # Spring Boot 后端 (107 个 Java 文件)
├── charge-safe-admin/            # Vue3 Web 管理端 (31 个 Vue/JS 文件)
├── charge-safe-miniapp/          # uni-app 小程序端 (基础骨架)
├── docs/                         # 项目文档
│   ├── design/                   # 设计文档 (本次生成)
│   ├── tasks/                    # 任务规划
│   ├── references/               # 参考源码 (已 gitignore)
│   └── 调研记录7.3/              # 调研截图和报告
├── sql/                          # 原始数据库脚本
├── README.md
└── .gitignore
```

---

## 2. 后端当前实现情况

### 2.1 模块实现状态

| 模块 | Controller | Service | Mapper | Entity | DTO/VO | 数据库表 | 当前状态 | 建议处理 |
|------|:----------:|:-------:|:------:|:------:|:------:|:--------:|----------|----------|
| auth (认证) | ✅ | ✅ | ❌ | ❌ | ✅ | - | 已基本完成 | 保留 |
| user (用户) | ❌ | ❌ | ✅ | ✅ | ❌ | sys_user, sys_role, sys_user_role | 部分完成 | 小修 |
| community (社区) | ✅ | ✅ | ✅ | ✅ | ✅ | community, parking_area | 已基本完成 | 保留 |
| device (设备) | ✅ | ✅ | ✅ | ✅ | ✅ | charging_device, charging_port | 已基本完成 | 保留 |
| charging (充电) | ✅ | ✅ | ✅ | ✅ | ✅ | charging_reservation, charging_order | 已基本完成 | 保留 |
| repair (报修) | ✅ | ✅ | ✅ | ✅ | ✅ | repair_report, repair_ticket, repair_ticket_log | 已基本完成 | 保留 |
| inspection (巡检) | ✅ | ✅ | ✅ | ✅ | ✅ | inspection_task, inspection_record, safety_hazard | 已基本完成 | 保留 |
| notification (通知) | ✅ | ✅ | ✅ | ✅ | ❌ | notification | 已基本完成 | 小修 |
| statistics (统计) | ✅ | ✅ | ❌ | ❌ | ✅ | - | 部分完成 | 小修 |
| simulator (模拟) | ✅ | ✅ | ❌ | ❌ | ❌ | - | 部分完成 | 小修 |
| log (日志) | ✅ | ❌ | ✅ | ✅ | ❌ | operation_log | 已基本完成 | 保留 |

### 2.2 公共组件

| 组件 | 文件 | 状态 | 说明 |
|------|------|------|------|
| Result | Result.java | ✅ 完成 | 统一返回格式 |
| ResultCode | ResultCode.java | ✅ 完成 | 错误码枚举 |
| PageResult | PageResult.java | ✅ 完成 | 分页返回 |
| PageQuery | PageQuery.java | ✅ 完成 | 分页查询参数 |
| BusinessException | BusinessException.java | ✅ 完成 | 业务异常 |
| GlobalExceptionHandler | GlobalExceptionHandler.java | ✅ 完成 | 全局异常处理 |
| OperationLog | OperationLog.java | ✅ 完成 | 日志注解 |
| OperationLogAspect | OperationLogAspect.java | ✅ 完成 | 日志切面 |

### 2.3 安全组件

| 组件 | 文件 | 状态 | 说明 |
|------|------|------|------|
| JwtUtils | JwtUtils.java | ✅ 完成 | JWT 工具类 |
| JwtAuthenticationFilter | JwtAuthenticationFilter.java | ✅ 完成 | JWT 过滤器 |
| LoginUser | LoginUser.java | ✅ 完成 | 登录用户信息 |
| UserContext | UserContext.java | ✅ 完成 | 用户上下文 |

### 2.4 配置类

| 组件 | 文件 | 状态 | 说明 |
|------|------|------|------|
| MybatisPlusConfig | MybatisPlusConfig.java | ✅ 完成 | MyBatis-Plus 配置 |
| SecurityConfig | SecurityConfig.java | ✅ 完成 | 安全配置 |
| WebConfig | WebConfig.java | ✅ 完成 | CORS 配置 |

### 2.5 后端缺口分析

| 缺口 | 优先级 | 说明 |
|------|--------|------|
| user 模块缺少 Controller | P0 | 用户管理、角色管理、权限管理的 CRUD 接口 |
| user 模块缺少 DTO/VO | P0 | 请求参数和响应对象 |
| notification 模块缺少 DTO/VO | P1 | 消息通知的请求参数和响应对象 |
| statistics 模块缺少 Mapper | P1 | 统计数据的查询逻辑 |
| simulator 模块缺少 Entity | P1 | 模拟数据的实体定义 |
| 缺少菜单权限表 | P0 | RBAC 需要 menu 表 |
| 缺少数据权限控制 | P1 | 当前只有社区 ID 隔离 |
| 缺少飞书/钉钉通知 | P2 | 后续扩展 |

---

## 3. 前端管理端当前实现情况

### 3.1 页面实现状态

| 页面 | 文件位置 | API 对接 | 当前状态 | 缺口 | 建议处理 |
|------|----------|----------|----------|------|----------|
| 登录页 | auth/Login.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 工作台首页 | dashboard/Index.vue | ✅ 已对接 | 已基本完成 | 图表较少 | 小修 |
| 社区管理 | community/Index.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 车棚管理 | community/ParkingArea.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 设备管理 | device/Index.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 端口管理 | device/Port.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 预约记录 | charging/Reservation.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 充电订单 | charging/Order.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 报修记录 | repair/Report.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 维修工单 | repair/Ticket.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 巡检任务 | inspection/Task.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 安全隐患 | inspection/Hazard.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 消息通知 | notification/Index.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 操作日志 | log/Index.vue | ✅ 已对接 | 已基本完成 | 无 | 保留 |
| 统计分析 | - | - | 未实现 | 需新增 | 新增 |
| 可视化大屏 | - | - | 未实现 | 需新增 | 新增 |

### 3.2 前端缺口分析

| 缺口 | 优先级 | 说明 |
|------|--------|------|
| 缺少统计分析页面 | P1 | 需要新增独立的统计分析页面 |
| 缺少可视化大屏页面 | P1 | 需要新增大屏页面 |
| 缺少用户管理页面 | P0 | 需要新增用户、角色、权限管理页面 |
| 缺少菜单权限配置 | P1 | 需要新增菜单管理页面 |
| 缺少数据权限控制 | P1 | 前端需要根据角色显示不同菜单 |
| 缺少标签页切换 | P2 | 参考 vue-element-plus-admin |
| 缺少暗黑模式 | P2 | 后续优化 |

---

## 4. 小程序端当前实现情况

小程序端（charge-safe-miniapp）当前只有基础骨架：

| 页面 | 文件位置 | 状态 |
|------|----------|------|
| 登录页 | pages/login.vue | 基础骨架 |
| 首页 | pages/index/index.vue | 基础骨架 |
| 车棚列表 | pages/parking/index.vue | 基础骨架 |

**结论**：小程序端当前未实现完整功能，后续需要从零搭建或只做原型。

---

## 5. 数据库当前实现情况

### 5.1 已有表

| 表名 | 用途 | 核心字段 | 状态字段 | 索引 |
|------|------|----------|----------|------|
| sys_user | 用户表 | id, username, password, community_id | status | uk_username, idx_community_id |
| sys_role | 角色表 | id, role_name, role_code | status | uk_role_code |
| sys_user_role | 用户角色关联 | user_id, role_id | - | uk_user_role |
| community | 社区表 | id, name, address | status | - |
| parking_area | 车棚表 | id, community_id, name | status | idx_community_id |
| charging_device | 充电设备表 | id, parking_area_id, device_code | status | uk_device_code |
| charging_port | 充电端口表 | id, device_id, port_number | status | uk_device_port, idx_status |
| resident_profile | 居民档案 | user_id, community_id, building | - | uk_user_id |
| vehicle | 车辆表 | user_id, plate_number | - | idx_user_id |
| charging_reservation | 充电预约 | user_id, port_id, expire_time | status | idx_user_id, idx_port_id, idx_status |
| charging_order | 充电订单 | order_no, user_id, port_id, amount | status | uk_order_no, idx_user_id, idx_status |
| repair_report | 报修记录 | reporter_id, device_id, fault_desc | status | idx_reporter_id, idx_community_id |
| repair_ticket | 维修工单 | ticket_no, assignee_id | status | uk_ticket_no, idx_assignee_id, idx_status |
| repair_ticket_log | 工单日志 | ticket_id, action | - | idx_ticket_id |
| inspection_task | 巡检任务 | community_id, inspector_id | status | idx_community_id, idx_inspector_id, idx_status |
| inspection_record | 巡检记录 | task_id, check_item | - | idx_task_id |
| safety_hazard | 安全隐患 | community_id, hazard_type, severity | status | idx_community_id, idx_reporter_id, idx_status |
| notification | 消息通知 | user_id, title, type | is_read | idx_user_id, idx_is_read, idx_created_time |
| operation_log | 操作日志 | user_id, module, operation | status | idx_user_id, idx_module, idx_created_time |
| charging_fee_rule | 费率规则 | community_id, fee_per_hour | status | idx_community_id |
| alarm_record | 告警记录 | device_id, alarm_type | status | idx_device_id, idx_status |

### 5.2 初始化数据

| 数据类型 | 内容 |
|----------|------|
| 角色 | PLATFORM_ADMIN, PROPERTY_ADMIN, MAINTAINER, INSPECTOR, RESIDENT |
| 用户 | admin, property01, maintainer01, inspector01, resident01, resident02 |
| 社区 | 阳光花园小区 |
| 车棚 | A栋充电车棚, B栋充电车棚 |
| 设备 | DEV-001 ~ DEV-004 |
| 端口 | 14 个端口 (IDLE, FAULT, CHARGING, DISABLED, MAINTENANCE) |
| 居民档案 | 赵六, 钱七 |
| 车辆 | 京A12345, 京B67890 |
| 费率规则 | 标准费率 1.5元/小时 |

### 5.3 数据库缺口分析

| 缺口 | 优先级 | 说明 |
|------|--------|------|
| 缺少 sys_menu 表 | P0 | RBAC 需要菜单权限表 |
| 缺少 sys_role_menu 关联表 | P0 | 角色菜单关联 |
| 缺少 resident_profile 数据 | P1 | 初始化数据不完整 |
| 缺少 charging_fee_rule 数据 | P1 | 费率规则需要补充 |
| 缺少 alarm_record 数据 | P2 | 告警记录需要模拟数据 |

---

## 6. 当前项目最重要的缺口

### P0 必须补

1. **user 模块 Controller** - 用户管理、角色管理的 CRUD 接口
2. **sys_menu 表** - RBAC 菜单权限表
3. **前端用户管理页面** - 用户列表、角色分配、权限配置
4. **预约冲突检测完善** - 当前预约逻辑需要加强

### P1 建议补

1. **统计分析页面** - 需要新增独立的统计分析页面
2. **可视化大屏页面** - 需要新增大屏页面
3. **小程序端完整实现** - 需要从零搭建居民端核心流程
4. **数据权限控制** - 前端需要根据角色显示不同菜单

### P2 后续优化

1. **标签页切换** - 参考 vue-element-plus-admin
2. **暗黑模式** - 后续优化
3. **3D 场景** - 第二阶段

---

## 7. 最大改动判断

### 结论：在现有基础上继续开发，局部优化

**原因**：

1. **后端结构清晰**：模块划分合理，Controller-Service-Mapper-Entity 结构完整
2. **数据库设计合理**：表结构规范，状态字段完整，索引设计合理
3. **前端已对接**：大部分页面已对接后端 API
4. **业务流程完整**：预约、充电、报修、工单、巡检、隐患的核心流程已实现

**需要优化的地方**：

1. **补充 user 模块**：需要新增用户管理、角色管理的 Controller
2. **补充前端页面**：需要新增用户管理、统计分析、可视化大屏页面
3. **小程序端**：需要从零搭建或只做原型
4. **大屏和 3D**：第一阶段做 2D 大屏，第二阶段做 3D

---

## 8. 调研资料能帮助优化的地方

| 调研资料 | 可优化的地方 | 对应模块 |
|----------|--------------|----------|
| RuoYi-Vue | 用户管理、角色管理、菜单管理 | user 模块 |
| JeecgBoot | 企业级后台结构、数据权限 | 整体架构 |
| vue-element-plus-admin | 后台布局、标签页、暗黑模式 | 前端布局 |
| Vue3Echarts | 大屏布局、图表组件 | 可视化大屏 |
| es-big-screen | 大屏适配、滚动列表 | 可视化大屏 |
| smartPark-3D | 3D 场景、设备点位 | 第二阶段 |
| EV Charging Station | 充电业务流程 | charging 模块 |
| 禅道/FeelDesk | 工单状态流转 | repair 模块 |
| OpenRemote | 设备告警、运维监控 | 通知和告警模块 |
