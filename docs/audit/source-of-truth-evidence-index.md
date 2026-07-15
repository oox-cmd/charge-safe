# ChargeSafe 源码事实证据索引

> 基线：`d0c624975fb33fb8ebda50b7d3f1ab0847b12ff4`
> 用途：把“代码事实”与中期文档章节绑定。计划文档不构成实现证据。
> 增量更新：2026-07-15 JWT fail-open 修复，提交待本次提交生成。
> 增量更新：2026-07-15 权限码一致性修复随本次提交固化，哈希以 Git 历史为准。
> 增量更新：2026-07-15 管理运维大屏与居民页面访问模型拆分；新增 V6、居民社区范围接口和路由权限，哈希以 Git 历史为准。

## 1. 源码事实矩阵

| 主题 | 当前代码事实 | 主要证据 | 一致性/缺失 | 建议处理 |
| --- | --- | --- | --- | --- |
| 项目目标 | 社区电动自行车充电安全运维，课程演示，不接真实硬件 | `README.md` 项目边界 | 基本一致 | 作为范围唯一来源 |
| 角色 | PLATFORM_ADMIN、PROPERTY_ADMIN、MAINTAINER、INSPECTOR、RESIDENT | `DataScope.java`、V2/V3 | 一致 | 文档统一用枚举码 |
| 认证 | JWT + `UserContext`；`POST /api/auth/login` 可匿名，其余 `/api/*` 缺失、格式错误、无效、过期或失效用户 Token 时返回统一 HTTP 401 且不进入 Controller；请求开始和结束均清理上下文 | `charge-safe-backend/src/main/java/com/chargesafe/security/JwtAuthenticationFilter.java`、`charge-safe-backend/src/test/java/com/chargesafe/security/JwtAuthenticationFilterHttpTest.java`、`SecurityConfig.java` | fail-open 已关闭，401 与权限不足 403 语义保持分离 | 继续以 HTTP 安全测试防回归 |
| 权限 | Controller 注解是接口安全边界，V3 `permission_code` 向角色分发同一规范码，管理端只精确匹配 `/api/auth/permissions` 返回值；历史 30 个错误引用与本地通配符矩阵已移除 | `PermissionAspect.java`、`AuthController.java`、`MenuServiceImpl.java`、`usePermission.js`、V3/V5、`PermissionCodeConsistencyTest.java` | 已修复现有码不一致；未做 RBAC 重构，未补所有端点的方法级注解 | 后续权限覆盖扩展必须独立评审角色边界，不修改 V1-V5 |
| 社区 | CRUD、状态、经纬度 | `CommunityController.java`、V4 | 已实现 | 经纬度只作近似定位 |
| 车棚 | CRUD、社区归属 | `ParkingAreaController.java`、`parking_area` | 已实现 | 三级大屏称“站点”前需统一术语 |
| 设备/端口 | 设备、端口、状态修改、批量端口 | `DeviceController.java`、`PortController.java` | 已实现 | 补端口级大屏 DTO |
| 预约/充电 | 后端业务流存在；小程序未接 | `ChargingController.java`、`ChargingServiceImpl.java` | 跨端不一致 | 先闭环小程序 |
| 报修 | 后端报修/工单状态流存在；小程序假提交 | repair 模块、`repair/submit.vue` | 跨端不一致 | 接真实 API |
| 巡检 | 任务、记录、隐患整改复查 | inspection 模块 | 基本实现 | 增加测试 |
| 通知 | 后端/管理端真实；小程序 Mock | notification 模块、miniapp mock | 跨端不一致 | 统一用户接口 |
| 操作日志 | AOP 写入、后台查询 | `OperationLogAspect.java`、`LogController.java` | 已实现 | 增加脱敏规则说明 |
| 统计 | 6 个聚合接口 | statistics 模块 | 可支撑二维统计 | 不足以支撑端口级三维场景 |
| 管理大屏 | `/bigscreen` 是登录后的二维管理运维页，要求 `statistics:bigscreen:view`，页面只调用同权限保护的管理聚合接口；视觉和静态内容仍沿用旧实现 | `charge-safe-admin/src/router/index.js`、`views/bigscreen/Index.vue`、`StatisticsController.java#getManagementBigscreen` | 访问模型已修复；仍不是 3D/数字孪生，仍含合成/静态展示 | 后续按 Three.js + three-globe 独立实施并真实化 |
| 居民页面 | `/resident/dashboard` 要求 `resident:dashboard:view`；接口不接受客户端社区范围，使用 `UserContext.communityId` 从查询层限定社区，只返回安全精简 DTO | `views/resident/Dashboard.vue`、`ResidentDashboardController.java`、`ResidentDashboardServiceImpl.java`、`ResidentDashboardVO.java` | 最小真实二维页面已实现，不含预约动作或三维展示 | 后续业务闭环保持居民数据域 |
| 数据库 | 23 张 Flyway 建表；逻辑关联，无物理外键 | V1、V3 | 设计文档过时 | 以迁移为唯一结构来源 |
| Flyway | V1-V6，`classpath:db/migration`；V6 只新增隐藏权限 `resident:dashboard:view` 并通过稳定角色码与权限码关联 RESIDENT | `application.yml`、`V6__add_resident_dashboard_permission.sql` | V1-V5 checksum 未变，V6 已在 Windows MySQL 成功执行 | 禁止改已发布迁移 |
| 本地开发 | Windows MySQL；WSL 动态网关 | local profile、WSL 脚本 | README 较准确 | 保持无 Docker |
| 测试 | 47 项；新增居民服务社区隔离、居民/管理 HTTP 权限、V6 契约和前端路由静态契约，2026-07-15 在 OpenJDK 21.0.11 下完整测试和打包通过 | `ResidentDashboardServiceTest.java`、`DashboardAccessHttpTest.java`、`ResidentDashboardPermissionMigrationTest.java`、`DashboardRouteAccessContractTest.java`、既有 JWT/权限测试 | 访问边界已有后端回归；管理端无测试框架，使用静态契约、构建和浏览器冒烟补足 | 后续补正式管理端 router/store 测试基础 |

## 2. 后端接口映射

### 2.1 权限码规范映射（2026-07-15）

权威关系是“Controller 注解定义接口安全边界，V3 菜单数据分发同名权限码，管理端消费后端返回值”。非系统业务当前沿用既有页面级权限码保护写操作，这是保持现有角色边界的事实，不代表已经拆成细粒度动作权限。

| 业务操作 | 规范权限码 | 后端证据 | 菜单证据 | 管理端证据 |
| --- | --- | --- | --- | --- |
| 用户新增/编辑/删除/启停/分配角色/重置密码 | `system:user:create/update/delete/status/assign-role/reset-password` | `UserController.java` | V3 IDs 1111-1116 | `views/system/User.vue` |
| 角色新增/编辑/删除/分配菜单 | `system:role:create/update/delete/assign-menu` | `RoleController.java` | V3 IDs 1121-1124 | `views/system/Role.vue` |
| 菜单新增/编辑/删除 | `system:menu:create/update/delete` | `MenuController.java` | V3 IDs 1131-1133 | `views/system/Menu.vue` |
| 社区新增/编辑/启停 | `community:list:view` | `CommunityController.java` | V3 ID 1210，PROPERTY_ADMIN 由 V5 补充 | `views/community/Index.vue` |
| 车棚新增/编辑/删除 | `parking-area:view` | `ParkingAreaController.java` | V3 ID 1220 | `views/community/ParkingArea.vue` |
| 设备新增/编辑/启停 | `device:list:view` | `DeviceController.java` | V3 ID 1310 | `views/device/Index.vue` |
| 端口状态更新 | `port:view` | `PortController.java` | V3 ID 1320 | `views/device/Port.vue` |
| 报修派单与工单处理 | `repair-ticket:view` | `RepairController.java` | V3 ID 1520 | `views/repair/Report.vue`、`Ticket.vue` |
| 巡检任务创建/开始/完成 | `inspection-task:view` | `InspectionController.java` | V3 ID 1530 | `views/inspection/Task.vue` |
| 隐患整改/复查/关闭 | `hazard:view` | `InspectionController.java` | V3 ID 1540 | `views/inspection/Hazard.vue` |
| 管理运维大屏 | `statistics:bigscreen:view` | `StatisticsController.java#getManagementBigscreen` | V3 ID 1630，PLATFORM_ADMIN/PROPERTY_ADMIN 原关联 | `/bigscreen`、`views/bigscreen/Index.vue` |
| 居民充电可用性页 | `resident:dashboard:view` | `ResidentDashboardController.java#getDashboard` | V6 隐藏权限节点，仅映射 RESIDENT | `/resident/dashboard`、`views/resident/Dashboard.vue` |

V6 后只读运行态权限数为 PLATFORM_ADMIN 40、PROPERTY_ADMIN 22、MAINTAINER 5、INSPECTOR 6、RESIDENT 1。RESIDENT 仅包含 `resident:dashboard:view`，不含 `statistics:bigscreen:view` 或后台管理权限；平台管理员因既有“枚举全部启用权限码”语义可见新权限，但 V6 的角色菜单映射只授予 RESIDENT。运行验证只调用登录、权限和 GET 接口，没有调用写接口或手工修改角色菜单数据。

实际 Controller 16 个、REST 映射 104 个。下表按模块归并；精确请求 DTO/VO 以对应 Controller 方法签名为准。

| 模块 | Controller / 路径 | 端点数 | 权限方式 | Service / Mapper | 主要表 | 管理端 | 小程序 |
| --- | --- | ---: | --- | --- | --- | --- | --- |
| 认证 | `AuthController` `/api/auth` | 4 | 登录公开，其余依赖上下文 | `AuthService`、User/Role/Menu Mapper | sys_user、sys_role、sys_menu | 登录、动态菜单 | 密码登录参数不匹配；短信/注册不存在 |
| 用户 | `UserController` `/api/users` | 7 | 部分 `system:user:*` | `UserService` | sys_user、sys_user_role | 用户管理 | Mock 用户资料 |
| 角色 | `RoleController` `/api/roles` | 6 | `system:role:*` | `RoleService` | sys_role、sys_role_menu | 角色管理 | 无 |
| 菜单 | `MenuController` `/api/menus` | 5 | `system:menu:*` | `MenuService` | sys_menu | 菜单管理 | 无 |
| 社区 | `CommunityController` `/api/communities` | 6 | 变更接口注解 | `CommunityService` | community | 社区管理 | 列表可能走 Mock；nearest 不存在 |
| 车棚 | `ParkingAreaController` `/api/parking-areas` | 6 | 变更接口注解 | `ParkingAreaService` | parking_area | 车棚管理 | 页面静态 |
| 设备 | `DeviceController` `/api/devices` | 6 | 变更接口注解 | `DeviceService` | charging_device | 设备管理 | 无真实接入 |
| 端口 | `PortController` `/api/ports` | 9 | 状态/批量接口注解 | `PortService` | charging_port | 端口管理 | 详情端口静态 |
| 预约/订单 | `ChargingController` `/api` | 12 | 业务归属与部分角色逻辑 | `ChargingService` | charging_reservation、charging_order | 预约/订单只读监控 | 页面本地模拟 |
| 报修/工单 | `RepairController` `/api` | 12 | 流程动作注解 | `RepairService` | repair_report、repair_ticket、repair_ticket_log | 报修、派单、维修、验收 | 假提交/静态列表 |
| 巡检/隐患 | `InspectionController` `/api` | 13 | 流程动作注解 | `InspectionService` | inspection_task、inspection_record、safety_hazard | 任务与隐患 | 无 |
| 通知 | `NotificationController` `/api/notifications` | 4 | 用户上下文 | `NotificationService` | notification | 消息页 | 调用不存在的 `/api/user/*` |
| 日志 | `LogController` `/api/logs` | 2 | 未见方法注解 | `OperationLogService` | operation_log | 日志页 | 无 |
| 统计 | `StatisticsController` `/api/statistics` | 7 | 管理聚合为 `statistics:bigscreen:view`；共享 dashboard/port 为 `dashboard:view`；分析接口为 `statistics:analysis:view` | `StatisticsService` | 多表聚合 | 仪表盘、统计、管理大屏 | 无 |
| 居民可用性 | `ResidentDashboardController` `/api/resident/dashboard` | 1 | `resident:dashboard:view` | `ResidentDashboardService` | community、parking_area、charging_device、charging_port | 居民页面 | 无 |
| 模拟器 | `SimulatorController` `/api/simulator` | 4 | 未见方法注解 | 组合多个 Service | port/order/reservation | 模拟器页 | 无 |

### 2.1 前端声明但后端不存在或不匹配

- 管理端 API 声明 `GET /api/users/{id}`、`GET /api/roles/{id}`、`GET /api/menus/{id}`，对应 Controller 无该 GET；当前页面未依赖这些调用。
- 小程序不存在的后端能力：短信发送/登录、注册、重置密码、nearest community、`/api/user/info|profile|password|avatar|messages/unread-count`。
- 管理端菜单表单使用 `permission`，后端模型使用 `permissionCode`，需在联调时确认序列化契约。
- 计划文档中的 `/api/ports/statistics` 不存在；真实接口是 `/api/statistics/port-status`。

## 3. 管理端页面映射

| 路由/页面 | 功能/API | 权限 | 数据状态 | 文档覆盖与问题 |
| --- | --- | --- | --- | --- |
| `/login` | 登录 `/api/auth/login` | 公开 | 真实 | 视觉普通 |
| `/permission/dashboard` | 用户、角色、社区、设备、日志、统计聚合 | 平台管理员路由 | 真实 | 快捷入口含演示页 |
| `/permission/system/user` | 用户 CRUD、状态、角色 | 使用 V3/Controller 的 `system:user:create/update/delete/status/assign-role/reset-password` | 真实 | 权限码已统一；页面设计文档仍过时 |
| `/permission/system/role` | 角色 CRUD、菜单分配 | 使用 V3/Controller 的 `system:role:create/update/delete/assign-menu` | 真实 | 权限码已统一；页面设计文档仍过时 |
| `/permission/system/menu` | 菜单树 CRUD | 使用 V3/Controller 的 `system:menu:create/update/delete` | 真实 | 权限码已统一；`permission`/`permissionCode` 表单字段契约仍需独立联调 |
| `/permission/system/dept` | 部门树 CRUD 外观 | 路由限制 | 静态演示 | 无后端，明确未实现 |
| `/permission/device-category` | 分类 CRUD 外观 | 路由限制 | 静态演示 | 无后端，明确未实现 |
| `/permission/simulator` | 状态/故障/恢复/完成模拟 | 路由限制 | 真实演示接口 | 后端安全边界偏弱 |
| `/permission/log` | 操作日志查询 | 路由限制 | 真实 | 搜索和错误状态可加强 |
| `/ops/dashboard` | 物业/维修/巡检工作台 | 角色路由 | 真实聚合 | 不同角色共用逻辑复杂 |
| `/ops/community` | 社区查询/变更 | 按钮权限 | 真实 | CRUD 模板化 |
| `/ops/parking-area` | 车棚 CRUD | 按钮权限 | 真实 | 缺空间示意 |
| `/ops/device` | 设备 CRUD/状态 | 按钮权限 | 真实 | 缺设备健康解释 |
| `/ops/port` | 端口查询/状态/批量 | 按钮权限 | 真实 | 9 状态视觉需统一 |
| `/ops/reservation` | 预约查询 | 路由限制 | 真实 | 只读表格 |
| `/ops/order` | 订单查询 | 路由限制 | 真实 | 只读表格 |
| `/ops/repair-report` | 报修查询/派单 | 按钮权限 | 真实 | 主流程可用 |
| `/ops/repair-ticket` | 接单/完成/验收/驳回/关闭 | 按钮权限 | 真实 | 主流程可用 |
| `/ops/inspection-task` | 创建/开始/完成 | 按钮权限 | 真实 | 主流程可用 |
| `/ops/hazard` | 整改/复查/关闭 | 按钮权限 | 真实 | 主流程可用 |
| `/ops/statistics` | 6 类统计图表 | 角色路由 | 真实聚合 | 请求失败静默；大 chunk |
| `/ops/notification` | 消息查询/已读 | 角色路由 | 真实 | 基本可用 |
| `/bigscreen` | 3 个二维 Tab | `requiresAuth:true` + `statistics:bigscreen:view`；权限恢复后判断 | 管理聚合真实，端口矩阵与告警/电气仍混合静态 | 不属于三级三维大屏；访问模型已修复 |
| `/resident/dashboard` | 居民寻找可用充电位置 | `requiresAuth:true` + `resident:dashboard:view`；RESIDENT 登录默认进入 | 登录社区内真实车棚/端口状态，含加载、空、错误和无社区状态 | 最小真实二维页，不是后台菜单或管理大屏 |

## 4. 小程序页面映射

| 页面路径 | 用户场景 | API/状态流转 | 当前状态 | 主要缺口 |
| --- | --- | --- | --- | --- |
| `pages/login` | 密码/短信登录 | Mock auth | 部分实现 | 参数与后端不匹配 |
| `pages/register` | 注册 | Mock register | 仅 Mock | 后端不存在 |
| `pages/forgot-password` | 重置密码 | Mock SMS/reset | 仅 Mock | 后端不存在 |
| `pages/index/index` | 社区、快捷入口、未读 | community/user Mock | 部分实现 | 近期订单静态，核心任务不突出 |
| `pages/parking/index` | 找车棚 | 无真实 API | 静态 | 无位置/空闲决策数据 |
| `pages/parking/detail` | 看端口、预约/报修 | 本地对象跳转 | 静态 | 端口非后端 ID |
| `pages/reservation/confirm` | 确认预约 | `setTimeout` | 仅演示 | 未调用预约接口 |
| `pages/reservation/index` | 预约记录/取消 | 本地数组 | 静态 | 未调用接口 |
| `pages/charging/charging` | 充电计时/结束 | 本地计时 | 仅演示 | 无真实订单/端口状态 |
| `pages/order/list` | 订单列表 | 本地数组 | 静态 | 无真实分页 |
| `pages/order/detail` | 订单详情 | 本地拼装 | 静态 | 非后端数据 |
| `pages/repair/submit` | 提交报修 | 定时器+随机工单号 | 仅演示 | 未上传/未提交 |
| `pages/repair/list` | 报修记录 | 本地数组 | 静态 | 无真实进度 |
| `pages/notification/list` | 消息 | 本地数组 | 静态 | 未接 notification |
| `pages/profile/index` | 个人中心 | Mock user | 部分实现 | 头像地址硬编码 localhost |
| `pages/profile/edit-profile` | 资料/手机 | Mock user/SMS | 仅 Mock | 后端不存在 |
| `pages/profile/settings` | 改密/退出 | Mock password | 部分实现 | 后端不存在改密接口 |

核心路径可点击但未闭环；“构建成功”不能替代“业务接通”。

## 5. 数据库与 Flyway 证据

### 5.1 唯一正式迁移源

- 配置：`charge-safe-backend/src/main/resources/application.yml` 中 `spring.flyway.locations: classpath:db/migration`。
- 文件：`V1__init_schema.sql`、`V2__init_data.sql`、`V3__add_menu_tables.sql`、`V4__add_community_coordinates.sql`、`V5__adjust_property_admin_menus.sql`、`V6__add_resident_dashboard_permission.sql`。
- V6 不新增表或字段；只创建隐藏权限节点 `resident:dashboard:view` 并以稳定角色码、权限码和 `NOT EXISTS` 建立幂等角色映射。
- 根目录 `sql/schema.sql`、`sql/data.sql` 未被当前 Spring/脚本自动引用，不符合 Flyway 版本命名，不得与 Flyway 同时执行。
- H2 使用独立 `db/h2/schema.sql` 和 `data.sql`，且当前缺少 H2 依赖。

### 5.2 表与实现覆盖

| 数据域 | 表 | 后端覆盖 |
| --- | --- | --- |
| RBAC | sys_user、sys_role、sys_user_role、sys_menu、sys_role_menu | 有 |
| 社区资源 | community、parking_area、charging_device、charging_port | 有 |
| 居民资产 | resident_profile、vehicle | 无业务模块 |
| 充电 | charging_reservation、charging_order、charging_fee_rule | 预约/订单有；费率规则无模块 |
| 报修 | repair_report、repair_ticket、repair_ticket_log | 有 |
| 巡检 | inspection_task、inspection_record、safety_hazard | 有 |
| 消息/审计 | notification、operation_log | 有 |
| 告警 | alarm_record | 仅表，无后端模块 |

迁移没有物理外键，关联由 `*_id`、索引和 Service 逻辑维护。

### 5.3 状态矩阵

| 对象 | 真实状态 | 管理端 | 小程序 | 大屏 |
| --- | --- | --- | --- | --- |
| 端口 | IDLE、RESERVED、CHARGING、COMPLETED_WAITING_PICKUP、OVERTIME_OCCUPIED、FAULT、MAINTENANCE、DISABLED、OFFLINE | 基本覆盖 | 静态子集 | 缺前两种完成/超时状态 |
| 预约 | PENDING、CONVERTED、CANCELLED、EXPIRED、FAULT_CANCELLED | 查询 | 静态 | 只统计今日数 |
| 订单 | CHARGING、COMPLETED、SETTLED、CANCELLED、ABNORMAL | 查询 | 静态 | 仅趋势/数量 |
| 报修 | PENDING、CONFIRMED、DISPATCHED、REJECTED | 覆盖 | 静态 | 静态告警替代 |
| 工单 | PENDING、DISPATCHED、PROCESSING、WAITING_ACCEPTANCE、FINISHED、CLOSED、REJECTED | 覆盖 | 未接 | 静态列表 |
| 巡检任务 | PENDING、IN_PROGRESS、COMPLETED、ABNORMAL、OVERDUE、CANCELLED | 覆盖 | 无 | 无 |
| 隐患 | PENDING、RECTIFYING、WAITING_REVIEW、REVIEW_PASSED、REVIEW_FAILED、CLOSED | 覆盖 | 无 | 静态列表 |

## 6. 大屏数据证据

| 所需数据 | 当前来源 | 能否直接使用 | 缺口 |
| --- | --- | --- | --- |
| 社区位置 | community.lat/lng（V4） | 可用于一级近似标点 | 无边界/真实 GIS |
| 车棚位置 | parking_area.location 文本 | 否 | 无经纬度/场景坐标 |
| 端口当前状态 | charging_port.status | 是 | 需端口级 DTO，不应从聚合数生成 |
| 单站状态汇总 | 可由端口聚合 | 部分 | 现有 VO 缺 reserved/maintenance/alarm |
| 告警 | alarm_record 表 | 否 | 无实体/接口 |
| 工单/隐患 | 已有业务表与分页接口 | 部分 | 需大屏精简 DTO/按站聚合 |
| 历史回放 | 无端口状态历史 | 否 | 后期再设计迁移 |
| 三维坐标 | 无 | 否 | MVP 用前端配置，不伪装经纬度 |

## 7. 构建与测试证据

| 命令 | 2026-07-14 结果 | 说明 |
| --- | --- | --- |
| `mvn clean test` | 失败 | 16 项中 1 通过，15 项因 JDK 21 下 Mockito/Byte Buddy agent 自附加失败；无业务断言失败证据 |
| `npm run build`（admin） | 通过 | Vite 8.1.2；存在 >500 kB chunk、Lottie `eval` 等警告 |
| `npx -y npm@10.9.8 run build:mp-weixin` | 通过 | 生成 `dist/build/mp-weixin`，目录被忽略 |
| local Profile | 本轮未启动 | 避免修改数据库/运行态；历史基线为 Windows MySQL |

## 8. 证据抽查清单

以下 34 条路径均应在完成前重新检查存在性：

1. `charge-safe-backend/pom.xml`
2. `charge-safe-backend/src/main/resources/application.yml`
3. `charge-safe-backend/src/main/resources/application-local.yml`
4. `scripts/dev-backend-wsl.sh`
5. `charge-safe-backend/src/main/resources/db/migration/V1__init_schema.sql`
6. `charge-safe-backend/src/main/resources/db/migration/V5__adjust_property_admin_menus.sql`
7. `charge-safe-backend/src/main/java/com/chargesafe/config/SecurityConfig.java`
8. `charge-safe-backend/src/main/java/com/chargesafe/security/JwtAuthenticationFilter.java`
9. `charge-safe-backend/src/main/java/com/chargesafe/security/PermissionAspect.java`
10. `charge-safe-backend/src/main/java/com/chargesafe/security/DataScope.java`
11. `charge-safe-backend/src/main/java/com/chargesafe/modules/auth/controller/AuthController.java`
12. `charge-safe-backend/src/main/java/com/chargesafe/modules/charging/controller/ChargingController.java`
13. `charge-safe-backend/src/main/java/com/chargesafe/modules/charging/service/impl/ChargingServiceImpl.java`
14. `charge-safe-backend/src/main/java/com/chargesafe/modules/repair/controller/RepairController.java`
15. `charge-safe-backend/src/main/java/com/chargesafe/modules/inspection/controller/InspectionController.java`
16. `charge-safe-backend/src/main/java/com/chargesafe/modules/statistics/controller/StatisticsController.java`
17. `charge-safe-backend/src/main/java/com/chargesafe/modules/statistics/service/impl/StatisticsServiceImpl.java`
18. `charge-safe-backend/src/test/java/com/chargesafe/modules/charging/service/ChargingServiceTest.java`
19. `charge-safe-admin/package.json`
20. `charge-safe-admin/src/router/index.js`
21. `charge-safe-admin/src/api/request.js`
22. `charge-safe-admin/src/main.js`
23. `charge-safe-admin/src/composables/usePermission.js`
24. `charge-safe-admin/src/views/system/Menu.vue`
25. `charge-safe-admin/src/views/permission/system/Department.vue`
26. `charge-safe-admin/src/views/permission/device-category/Index.vue`
27. `charge-safe-admin/src/views/statistics/Index.vue`
28. `charge-safe-admin/src/views/bigscreen/Index.vue`
29. `charge-safe-miniapp/package.json`
30. `charge-safe-miniapp/src/main.js`
31. `charge-safe-miniapp/src/api/request.js`
32. `charge-safe-miniapp/src/api/mock/index.js`
33. `charge-safe-miniapp/src/pages/reservation/confirm.vue`
34. `charge-safe-miniapp/src/pages/repair/submit.vue`
