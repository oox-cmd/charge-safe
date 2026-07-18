# ChargeSafe 源码事实证据索引

> 基线：`d0c624975fb33fb8ebda50b7d3f1ab0847b12ff4`
> 用途：把“代码事实”与中期文档章节绑定。计划文档不构成实现证据。
> 增量更新：2026-07-15 JWT fail-open 修复，提交待本次提交生成。
> 增量更新：2026-07-15 权限码一致性修复随本次提交固化，哈希以 Git 历史为准。
> 增量更新：2026-07-15 管理运维大屏与居民页面访问模型拆分；新增 V6、居民社区范围接口和路由权限，哈希以 Git 历史为准。
> 增量更新：2026-07-15 后端构建基线统一至 Java 21；Surefire 显式加载 Byte Buddy Agent，消除 Mockito 运行时自附加警告。
> 增量更新：2026-07-18 V7 补齐部门/设备分类业务关联、居民报修隐藏权限和管理端大屏入口；H2 Profile 已移除，开发验证统一走 MySQL/Flyway。同日已在本机 MySQL `charge_safe_dev` 执行并确认 V1–V7 Flyway 历史全部成功。
>
> **当前状态覆盖说明（2026-07-18）**：本文件中带有“2026-07-15”“V6 后”或旧端点计数的细表是当时的取证快照，不能覆盖下列当前事实：小程序核心链路与居民报修已接真实 API，管理 3D 大屏已实现真实业务 ID 映射，模拟器写接口已要求 `simulator:manage`，部门/设备分类已具备服务端 CRUD、按钮权限与引用完整性约束，后端最新新鲜回归为 86 项测试通过。本机 MySQL/Flyway 已验证 V1–V7 成功；后端 `local` Profile 已完成平台、物业、居民、运维、巡检角色的核心只读 HTTP 冒烟。浏览器/真机端到端验收仍待执行。

## 1. 源码事实矩阵

| 主题 | 当前代码事实 | 主要证据 | 一致性/缺失 | 建议处理 |
| --- | --- | --- | --- | --- |
| 项目目标 | 社区电动自行车充电安全运维，课程演示，不接真实硬件 | `README.md` 项目边界 | 基本一致 | 作为范围唯一来源 |
| 角色 | PLATFORM_ADMIN、PROPERTY_ADMIN、MAINTAINER、INSPECTOR、RESIDENT | `DataScope.java`、V2/V3 | 一致 | 文档统一用枚举码 |
| 认证 | JWT + `UserContext`；`POST /api/auth/login` 可匿名，其余 `/api/*` 缺失、格式错误、无效、过期或失效用户 Token 时返回统一 HTTP 401 且不进入 Controller；请求开始和结束均清理上下文 | `charge-safe-backend/src/main/java/com/chargesafe/security/JwtAuthenticationFilter.java`、`charge-safe-backend/src/test/java/com/chargesafe/security/JwtAuthenticationFilterHttpTest.java`、`SecurityConfig.java` | fail-open 已关闭，401 与权限不足 403 语义保持分离 | 继续以 HTTP 安全测试防回归 |
| 权限 | Controller 注解是接口安全边界，管理查询、模拟写操作、居民报修及居民端口接口均有对应权限码；管理端只精确匹配 `/api/auth/permissions` 返回值 | `PermissionAspect.java`、各 Controller、`MenuServiceImpl.java`、V3/V6/V7、`PermissionCodeConsistencyTest.java` | V1–V5 保持不变；V7 追加部门/分类/模拟器/3D 大屏菜单与居民隐藏权限 | 后续权限覆盖扩展必须独立评审角色边界，不修改已发布迁移 |
| 社区 | CRUD、状态、经纬度 | `CommunityController.java`、V4 | 已实现 | 经纬度只作近似定位 |
| 车棚 | CRUD、社区归属 | `ParkingAreaController.java`、`parking_area` | 已实现 | 三级大屏称“站点”前需统一术语 |
| 设备/端口 | 设备、端口、状态修改、批量端口；设备可关联已启用分类 | `DeviceController.java`、`PortController.java`、`DeviceServiceImpl.java` | 已实现；端口统计按车棚→设备→端口链路限定社区 | 继续以真实数据驱动大屏 |
| 预约/充电 | 后端状态机与小程序预约、充电、订单页面已接本人 API | `ChargingController.java`、`ChargingServiceImpl.java`、miniapp `reservation/`、`charging/`、`order/` | 已接通，待真实后端冒烟 | 保持状态机与本人/社区边界测试 |
| 报修 | 居民报修创建、列表、详情为专用受限 API；管理端维持工单流程 | repair 模块、miniapp `repair/` | 已接通，待真实后端冒烟 | 保持报告人与社区隔离测试 |
| 巡检 | 任务、记录、隐患整改复查 | inspection 模块 | 基本实现 | 增加测试 |
| 通知 | 后端、管理端与小程序均调用真实通知接口 | notification 模块、miniapp `notification/list.vue` | 已接通，待真实后端冒烟 | 保持本人范围校验 |
| 操作日志 | AOP 写入、后台查询 | `OperationLogAspect.java`、`LogController.java` | 已实现 | 增加脱敏规则说明 |
| 统计 | 6 个聚合接口 | statistics 模块 | 可支撑二维统计 | 不足以支撑端口级三维场景 |
| 管理大屏 | `/bigscreen` 为有来源声明的二维管理页；`/bigscreen3d` 使用原生 Three.js + three-globe，并显式映射真实业务 ID | `views/bigscreen/Index.vue`、`views/bigscreen3d/`、`StatisticsController.java#getManagementBigscreen` | 3D MVP 已实现；无数据/错误状态不伪装为实时数据 | 真实 API 和浏览器验收后再扩大场景能力 |
| 居民页面 | `/resident/dashboard` 要求 `resident:dashboard:view`；接口不接受客户端社区范围，使用 `UserContext.communityId` 从查询层限定社区，只返回安全精简 DTO | `views/resident/Dashboard.vue`、`ResidentDashboardController.java`、`ResidentDashboardServiceImpl.java`、`ResidentDashboardVO.java` | 最小真实二维页面已实现，不含预约动作或三维展示 | 后续业务闭环保持居民数据域 |
| 数据库 | 25 张 Flyway 建表；逻辑关联，无物理外键 | V1、V3、V7 | 服务层维护部门/分类的关键引用完整性 | 以迁移为唯一结构来源 |
| Flyway | V1-V7，`classpath:db/migration`；V7 新增部门、分类、两个关联字段和权限数据 | `application.yml`、`V7__add_dept_and_device_category.sql` | V1-V5 checksum 不变；2026-07-18 本机 MySQL 已确认 V1–V7 成功 | 禁止改已发布迁移 |
| Java 构建 | `java.version`、`maven.compiler.release` 均为 21，Enforcer 要求 `[21,22)`；编译器显式启用注解处理 | `charge-safe-backend/pom.xml` | Maven 不再接受 Java 17 或未来 JDK 作为正式构建运行时 | JDK 21 下执行完整测试与打包 |
| 本地开发 | Windows MySQL；WSL 动态网关 | local profile、WSL 脚本 | README 较准确 | 保持无 Docker |
| 测试 | 86 项后端测试；Surefire 在 JVM 启动时使用 BOM 管理的 Byte Buddy Agent，Mockito 不再自附加；2026-07-18 JDK 21 完整打包通过 | `pom.xml`、各后端 `src/test/java/`、管理端/小程序测试目录 | 服务与权限边界已有回归；本机 MySQL/Flyway、登录及七类只读 HTTP 冒烟已完成 | 后续补正式管理端 router/store 与浏览器端到端测试基础 |

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
| 部门管理 | `system:dept:view/create/edit/delete` | `SysDeptController.java` | V7 IDs 1140–1143，仅 PLATFORM_ADMIN | `views/permission/system/Department.vue` |
| 设备分类 | `device:category:view/create/edit/delete` | `DeviceCategoryController.java` | V7 IDs 1330–1333，PROPERTY_ADMIN 仅 `view` | `views/permission/device-category/Index.vue` |
| 模拟器写操作 | `simulator:manage` | `SimulatorController.java` | V7 ID 1340 | `views/simulator/Index.vue` |

历史的 V6 权限数量不再作为当前结论。V7 增加了菜单和居民报修隐藏权限；具体角色权限应在 V7 执行后以 `/api/auth/permissions` 和 `sys_role_menu` 实测为准。RESIDENT 不应拥有 `statistics:bigscreen:view` 或后台管理权限。

当前共有 18 个 Controller；端点数量不作为稳定契约。下表按模块归并，精确请求 DTO/VO、路径和权限以 Controller 方法签名与运行时 OpenAPI 为准。

| 模块 | Controller / 路径 | 端点数 | 权限方式 | Service / Mapper | 主要表 | 管理端 | 小程序 |
| --- | --- | ---: | --- | --- | --- | --- | --- |
| 认证 | `AuthController` `/api/auth` | 动态 | 登录公开，其余依赖上下文 | `AuthService`、User/Role/Menu Mapper | sys_user、sys_role、sys_menu | 登录、动态菜单 | 用户名/密码登录与后端一致；不提供短信/注册 |
| 用户 | `UserController` `/api/users` | 动态 | `system:user:*` | `UserService` | sys_user、sys_user_role、sys_dept | 用户管理 | 无独立用户资料编辑接口 |
| 角色 | `RoleController` `/api/roles` | 6 | `system:role:*` | `RoleService` | sys_role、sys_role_menu | 角色管理 | 无 |
| 菜单 | `MenuController` `/api/menus` | 5 | `system:menu:*` | `MenuService` | sys_menu | 菜单管理 | 无 |
| 社区 | `CommunityController` `/api/communities` | 动态 | `community:list:view` | `CommunityService` | community | 社区管理 | 居民首页通过受限 dashboard 读取所属社区 |
| 车棚 | `ParkingAreaController` `/api/parking-areas` | 动态 | `parking-area:view` | `ParkingAreaService` | parking_area | 车棚管理 | 居民通过受限 dashboard / 端口接口读取 |
| 设备/分类 | `DeviceController`、`DeviceCategoryController` `/api/devices*` | 动态 | 设备 `device:list:view`；分类 V7 细粒度权限 | `DeviceService`、`DeviceCategoryService` | charging_device、device_category | 设备/分类管理 | 居民仅读取车棚内端口，不直接编辑设备 |
| 端口 | `PortController` `/api/ports`、`ResidentDashboardController` | 动态 | `port:view` 与居民专用权限 | `PortService`、`ResidentPortService` | charging_port | 端口管理 | 详情使用真实 `portId` |
| 预约/订单 | `ChargingController` `/api` | 动态 | 业务归属与权限注解 | `ChargingService` | charging_reservation、charging_order | 预约/订单只读监控 | 创建、取消、开始、结束、本人列表和详情接真实 API |
| 报修/工单 | `RepairController` `/api` | 动态 | 居民专用权限与工单权限 | `RepairService` | repair_report、repair_ticket、repair_ticket_log | 报修、派单、维修、验收 | 本人创建、列表、详情接真实 API；图片上传暂未开放并明确提示 |
| 巡检/隐患 | `InspectionController` `/api` | 13 | 流程动作注解 | `InspectionService` | inspection_task、inspection_record、safety_hazard | 任务与隐患 | 无 |
| 通知 | `NotificationController` `/api/notifications` | 动态 | 用户上下文 | `NotificationService` | notification | 消息页 | 本人列表、已读与未读数接真实 API |
| 日志 | `LogController` `/api/logs` | 2 | 未见方法注解 | `OperationLogService` | operation_log | 日志页 | 无 |
| 统计 | `StatisticsController` `/api/statistics` | 7 | 管理聚合为 `statistics:bigscreen:view`；共享 dashboard/port 为 `dashboard:view`；分析接口为 `statistics:analysis:view` | `StatisticsService` | 多表聚合 | 仪表盘、统计、管理大屏 | 无 |
| 居民可用性 | `ResidentDashboardController` `/api/resident/dashboard` | 1 | `resident:dashboard:view` | `ResidentDashboardService` | community、parking_area、charging_device、charging_port | 居民页面 | 无 |
| 部门 | `SysDeptController` `/api/system/depts` | 4 | `system:dept:*` | `SysDeptService` | sys_dept、sys_user | 部门管理 | 无 |
| 设备分类 | `DeviceCategoryController` `/api/devices/categories` | 4 | `device:category:*` | `DeviceCategoryService` | device_category、charging_device | 分类管理 | 无 |
| 模拟器 | `SimulatorController` `/api/simulator` | 4 | `simulator:manage` | 组合多个 Service | port/order/reservation | 模拟器页 | 无 |

### 2.1 当前接口边界

- 不提供短信登录、注册、找回密码、头像上传或个人资料编辑；小程序不再展示这些入口。
- 居民车棚、端口、预约、订单、报修和通知必须使用本人/所属社区受限接口，客户端不得传入可越权的 `communityId`。
- 部门删除会校验子部门与用户引用；设备分类删除会校验设备引用；分类编码在服务端视为稳定标识。
- 统计接口与大屏字段以 `StatisticsController` 和对应 VO 为准，不能按旧计划文档臆造路径。

## 3. 管理端页面映射

| 路由/页面 | 功能/API | 权限 | 数据状态 | 文档覆盖与问题 |
| --- | --- | --- | --- | --- |
| `/login` | 登录 `/api/auth/login` | 公开 | 真实 | 视觉普通 |
| `/permission/dashboard` | 用户、角色、社区、设备、日志、统计聚合 | 平台管理员路由 | 真实 | 快捷入口含演示页 |
| `/permission/system/user` | 用户 CRUD、状态、角色 | 使用 V3/Controller 的 `system:user:create/update/delete/status/assign-role/reset-password` | 真实 | 权限码已统一；页面设计文档仍过时 |
| `/permission/system/role` | 角色 CRUD、菜单分配 | 使用 V3/Controller 的 `system:role:create/update/delete/assign-menu` | 真实 | 权限码已统一；页面设计文档仍过时 |
| `/permission/system/menu` | 菜单树 CRUD | 使用 V3/Controller 的 `system:menu:create/update/delete` | 真实 | 权限码已统一；`permission`/`permissionCode` 表单字段契约仍需独立联调 |
| `/permission/system/dept` | 部门树 CRUD | `system:dept:*` | 真实 API | 服务端防循环、子部门/用户引用删除，前端按精确按钮权限展示 |
| `/permission/device-category` | 分类 CRUD | `device:category:*` | 真实 API | 字段使用 `description`，删除前校验设备引用，PROPERTY_ADMIN 仅只读 |
| `/permission/simulator` | 状态/故障/恢复/完成模拟 | `simulator:manage` | 真实演示接口 | 仅用于演示，禁止表述为硬件实时接入 |
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
| `/bigscreen` | 二维管理看板 | `requiresAuth:true` + `statistics:bigscreen:view`；权限恢复后判断 | 管理聚合与真实工单/隐患列表；未接入指标有来源声明 | 不属于三级三维大屏 |
| `/bigscreen3d` | 三级三维管理大屏 | `statistics:bigscreen:view` | 真实业务 ID 映射、空/错误/数据来源状态明确 | MVP，不伪装为数字孪生或硬件实时数据 |
| `/resident/dashboard` | 居民寻找可用充电位置 | `requiresAuth:true` + `resident:dashboard:view`；RESIDENT 登录默认进入 | 登录社区内真实车棚/端口状态，含加载、空、错误和无社区状态 | 最小真实二维页，不是后台菜单或管理大屏 |

## 4. 小程序页面映射

| 页面路径 | 用户场景 | API/状态流转 | 当前状态 | 主要缺口 |
| --- | --- | --- | --- | --- |
| `pages/login` | 用户名/密码登录 | 真实 `/api/auth/login`；开发 Mock 仅显式开启 | 已实现 | 不提供短信、注册、找回入口 |
| `pages/index/index` | 所属社区、快捷入口、未读、近期订单 | resident dashboard、通知、订单 API | 已实现 | 需真实后端冒烟 |
| `pages/parking/index` | 找车棚 | resident dashboard | 已实现 | 无地理导航/最近距离功能 |
| `pages/parking/detail` | 看端口、预约/报修 | 所属社区端口 API，真实 ID 跳转 | 已实现 | 需真实后端冒烟 |
| `pages/reservation/confirm` | 确认预约 | `createReservation` | 已实现 | 预计费用为前端提示，不替代后端计费 |
| `pages/reservation/index` | 预约记录/取消/开始 | 本人预约 API | 已实现 | 需真实后端冒烟 |
| `pages/charging/charging` | 充电计时/结束 | 本人订单详情、结束充电 API | 已实现 | 展示费用为前端预估，最终以订单为准 |
| `pages/order/list`、`pages/order/detail` | 本人订单 | 本人订单 API | 已实现 | 需真实后端冒烟 |
| `pages/repair/submit`、`pages/repair/list` | 本人报修 | resident repair API | 已实现 | 图片上传未开放，页面明确提示 |
| `pages/notification/list` | 本人消息 | notification API | 已实现 | 需真实后端冒烟 |
| `pages/profile/index` | 个人中心 | 当前用户状态与未读数 | 已实现 | 资料、头像和密码编辑入口已移除 |

核心页面已接真实 API；本机已完成 MySQL/Flyway、管理员登录和居民/物业等核心只读接口运行态验收，但“构建成功”仍不能替代浏览器/真机端到端及写操作状态机验收。

## 5. 数据库与 Flyway 证据

### 5.1 唯一正式迁移源

- 配置：`charge-safe-backend/src/main/resources/application.yml` 中 `spring.flyway.locations: classpath:db/migration`。
- 文件：`V1__init_schema.sql` 至 `V7__add_dept_and_device_category.sql`。
- V6 不新增表或字段；只创建隐藏权限节点 `resident:dashboard:view` 并以稳定角色码、权限码和 `NOT EXISTS` 建立幂等角色映射。
- 根目录 `sql/schema.sql`、`sql/data.sql` 未被当前 Spring/脚本自动引用，不符合 Flyway 版本命名，不得与 Flyway 同时执行。
- 不提供 H2 Profile；本地验证统一使用 MySQL 与 Flyway。

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
| 告警 | alarm_record | Entity/Mapper/Service 已存在；模拟器故障写入 `SIMULATOR_FAULT`，统计大屏读取。没有真实硬件采集模块 |

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
| `mvn clean test`（2026-07-14，历史） | 失败 | 16 项中 1 通过，15 项因 JDK 21 下 Mockito/Byte Buddy agent 自附加失败；无业务断言失败证据 |
| `mvn clean test`（2026-07-15，Java 21） | 通过 | 47/47；`javac [release 21]`，无动态 Agent 自附加警告 |
| `mvn clean package`（2026-07-15，Java 21） | 通过 | 47/47，Spring Boot 可执行 jar 已生成 |
| `npm run build`（admin） | 通过 | Vite 8.1.2；存在 >500 kB chunk、Lottie `eval` 等警告 |
| `npx -y npm@10.9.8 run build:mp-weixin` | 通过 | 生成 `dist/build/mp-weixin`，目录被忽略 |
| local Profile（2026-07-18） | 通过 | MySQL `charge_safe_dev` 的 V1–V7 全部成功；`admin` 登录和 `/api/auth/me` 通过；平台权限、物业统计/大屏、居民看板/预约、运维修单、巡检任务等七类只读接口均返回业务码 200 |

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

## 9. 2026-07-15 P0 收尾重新取证

| 取证主题 | 当前源码事实 | 结论 |
| --- | --- | --- |
| 管理/居民访问 | 管理大屏路由和聚合接口均受 `statistics:bigscreen:view` 保护；居民路由使用独立 `resident:dashboard:view`，服务端从 `UserContext` 取社区 | 已完成，无需重复处理 |
| 小程序 Mock | `main.js` 无条件导入 `src/api/mock`；`request.js` 的 `USE_MOCK` 缺省为真但没有控制安装 | 仍存在，阻塞 P0 |
| 小程序网络配置 | `request.js`、`user.js` 与 Mock handlers 包含 `http://localhost:8080`；无 `.env.example`；登录请求发送 `phone/password`，后端 DTO 要求 `username/password` | 仍存在，阻塞真机与真实登录 |
| 预约/充电持久化 | V1 已有 reservation/order/port 表和 `charging_order.reservation_id`；Service 有事务，但居民页面仍静态，且用户活动订单、跨社区和本人详情边界不完整 | 可用现有结构补齐，不需要 V7 |
| 大屏端口 | `ManagementBigscreenVO` 只有聚合；管理页按 `totalPorts` 展开循环 ID，并混用静态告警/工单/隐患/电参 | 需要真实层级只读 DTO，不需要迁移 |
| 测试 | Java 21 下 47/47；管理端与小程序构建通过；核心闭环异常和跨端契约覆盖不足 | 基线稳定，但收尾验证未完成 |

重新审计执行计划见 `docs/superpowers/plans/2026-07-15-p0-closeout.md`，过程证据见 `docs/process/logs/2026-07-15-p0-closeout-revalidation.md`。计划不属于已实现事实；只有后续源码、测试与运行结果可改变本索引的“数据状态”。

### 9.1 Mock 显式开关证据

| 证据 | 当前事实 |
| --- | --- |
| `charge-safe-miniapp/src/config/runtime.mjs` | `USE_MOCK = isDev === true && VITE_USE_MOCK === 'true'`，默认安全关闭 |
| `charge-safe-miniapp/src/main.js` | 只有 `USE_MOCK` 为真才调用 `installMock()` |
| `charge-safe-miniapp/src/api/mock/index.js` | Mock 安装为显式、幂等函数，不再在模块加载时替换 `uni.request` |
| `charge-safe-miniapp/src/api/mock/runtime.mjs` | 未命中 handler 委托真实请求；命中响应带 `X-ChargeSafe-Mock` 标识 |
| `charge-safe-miniapp/tests/` | 覆盖缺省关闭、开发显式开启、生产强制关闭、未命中真实请求和 Mock 响应标识 |
| `charge-safe-miniapp/src/App.vue`、`pages/login.vue` | Mock 开启时向开发者/用户明确显示“模拟数据” |

生产构建仍可能包含可供开发模式使用的 Mock 模块代码，但入口条件恒为关闭且不会安装请求拦截器；这与“生产不无条件 Mock”是同一可验证边界，不等于移除开发 Mock。

### 9.2 API 配置与登录契约证据

| 证据 | 当前事实 |
| --- | --- |
| `charge-safe-miniapp/.env.example` | 声明必填 `VITE_API_BASE_URL` 和默认关闭的 `VITE_USE_MOCK`，不含个人 IP/凭据 |
| `src/config/api.mjs` | 统一规范化请求/上传 URL，缺失报错，处理尾斜杠与重复 `/api` |
| `src/api/request.js`、`src/api/parking.js`、`src/api/notification.js` | 普通请求使用统一配置来源；HTTP 与 body code 共同决定成功 |
| `src/pages/login.vue` | 真实密码表单使用用户名；不展示尚无后端实现的短信/注册/找回入口 |
| `src/stores/user.js`、`src/api/auth.js` | 登录保存 `username/roleCode/roleName/communityId`；当前用户读取 `/api/auth/me` |
| `tests/config/api.test.mjs`、`tests/api/response.test.mjs` | 覆盖缺失配置、URL 拼接、主机无关路径、业务错误和 401 |

后端当前仍没有头像上传能力；小程序已移除该入口，因此不会把环境化地址误表述为已实现业务。

### 9.3 预约与充电状态机证据

| 证据 | 当前事实 |
| --- | --- |
| `ChargingPortMapper.transitionStatus` | 仅当端口仍为期望状态时原子迁移，防止同端口并发覆盖 |
| `ChargingReservationMapper.transitionStatus` | 预约状态与取消原因条件更新，重复开始/取消不能二次生效 |
| `ChargingOrderMapper.finishOrder` | 仅 `CHARGING` 订单可一次写入结束时间、时长、金额和 `COMPLETED` |
| `ChargingServiceImpl` | 居民角色/本人/社区校验、用户行锁、活动预约/订单互斥、关键写入计数与事务 |
| `ReservationExpiredException` | 开始已过期预约时保留 `EXPIRED + IDLE` 清理并返回 6003 |
| `ChargingController` | 管理列表/详情复用 `reservation:view`、`order:view`；新增本人预约分页和本人订单详情 |
| `ChargingServiceTest` | 36 项覆盖正常、冲突、故障/维修、非本人、跨社区、活动订单、重复/并发状态、写入失败、系统/管理社区边界与过期事务语义 |
| `ChargingAccessHttpTest` | 4 项覆盖匿名 401、居民本人端点和管理 `order:view` 的既有 body 403 语义 |

该段的 104/106 映射计数是 2026-07-15 历史取证。当前数据库迁移为 V1–V7，端点和 Controller 以本文前述“当前接口边界”及源码为准。
