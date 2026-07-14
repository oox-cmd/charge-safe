# ChargeSafe 项目源码事实核验报告

> 本报告基于项目源码、SQL 脚本、配置文件、已有文档的实际阅读结果，所有结论均附有证据。

---

## 一、项目基本信息

### 1. 项目名称
**ChargeSafe 社区充电安全运维平台**

证据来源：`README.md` 第 1 行 `# ChargeSafe 社区充电安全运维平台`

### 2. 项目类型
**Web 全栈系统**，包含三个子项目：
- 后端服务（Spring Boot REST API）
- Web 管理端（Vue3 单页应用）
- 小程序端（uni-app 跨端应用）

证据来源：`README.md` 第 1-3 行；目录结构中 `charge-safe-backend/`、`charge-safe-admin/`、`charge-safe-miniapp/` 三个子目录

### 3. 真实技术栈

| 层级 | 技术 | 证据文件 |
|------|------|----------|
| 后端语言 | Java 17 | `pom.xml:21` `<java.version>17</java.version>` |
| 后端框架 | Spring Boot 3.2.5 | `pom.xml:10` `<version>3.2.5</version>` |
| ORM | MyBatis-Plus 3.5.5 | `pom.xml:22` `<mybatis-plus.version>3.5.5</mybatis-plus.version>` |
| 数据库 | MySQL 8.x | `pom.xml:49-52` mysql-connector-j 依赖；`V1__init_schema.sql:2` 注释 `MySQL 8.x` |
| 数据库迁移 | Flyway | `pom.xml:55-63` flyway-core + flyway-mysql 依赖 |
| 认证 | JWT (jjwt 0.12.5) | `pom.xml:25` `<jjwt.version>0.12.5</jjwt.version>` |
| 密码加密 | BCrypt (spring-security-crypto) | `pom.xml:91-94` 依赖 |
| 接口文档 | Knife4j 4.4.0 (OpenAPI3) | `pom.xml:24` `<knife4j.version>4.4.0</knife4j.version>` |
| AOP | Spring AOP | `pom.xml:43-46` spring-boot-starter-aop 依赖 |
| 参数校验 | Spring Validation | `pom.xml:37-40` spring-boot-starter-validation 依赖 |
| 前端框架 | Vue 3 + Vite | `charge-safe-admin/vite.config.js:2` `import vue from '@vitejs/plugin-vue'` |
| UI 组件库 | Element Plus | `charge-safe-admin/src/views/auth/Login.vue:8` `<el-form>` 等组件 |
| 状态管理 | Pinia | `charge-safe-admin/src/stores/user.js:1` `import { defineStore } from 'pinia'` |
| HTTP 客户端 | Axios | `charge-safe-admin/src/api/request.js:1` `import axios from 'axios'` |
| 图表 | ECharts | `README.md:18` |
| 小程序框架 | uni-app + Vue 3 | `charge-safe-miniapp/src/manifest.json` |
| 构建工具 | Maven（后端）、Vite（前端） | `pom.xml`、`vite.config.js` |

### 4. 项目启动方式

**后端启动：**
- 入口文件：Spring Boot 标准入口（`ChargeSafeApplication`，位于 `com.chargesafe` 包下）
- 启动命令：`mvn spring-boot:run -Dspring-boot.run.profiles=local`
- 数据库初始化：Flyway 自动执行 `src/main/resources/db/migration/` 下的 V1-V5 迁移文件
- 证据：`README.md:131-134`

**Web 管理端启动：**
- 启动命令：`npm run dev`
- 证据：`README.md:143-146`

**小程序端启动：**
- H5 调试：`npm run dev:h5`，端口 5173
- 微信小程序：`npm run build:mp-weixin`
- 证据：`README.md:155-172`

### 5. 配置文件关键信息

| 配置项 | 值 | 证据文件 |
|--------|-----|----------|
| 后端端口 | 8080（默认，可通过 `SERVER_PORT` 环境变量覆盖） | `application.yml:2` `port: ${SERVER_PORT:8080}` |
| 数据库连接 | MySQL，库名 `charge_safe_dev`（local 环境） | `application-local.yml:6` |
| 数据库用户名 | 通过环境变量 `CHARGE_SAFE_DB_USERNAME` 注入，默认 `charge_safe_app` | `application-local.yml:7` |
| 数据库密码 | 通过环境变量 `CHARGE_SAFE_DB_PASSWORD` 注入 | `application-local.yml:8` |
| JWT 过期时间 | 86400000ms（24 小时） | `application.yml:34` |
| 逻辑删除字段 | `deleted`，0=未删除，1=已删除 | `application.yml:26-28` |
| 前端开发端口 | 3000 | `charge-safe-admin/vite.config.js:12` `port: 3000` |
| 前端 API 代理 | `/api` → `http://localhost:8080` | `charge-safe-admin/vite.config.js:14-18` |
| 小程序 H5 端口 | 5173 | `README.md:160` |
| Spring Profile | 默认 `local`，可通过 `SPRING_PROFILES_ACTIVE` 环境变量切换 | `application.yml:44` |

**多环境配置文件：**
- `application.yml` — 通用配置
- `application-local.yml` — 本地开发库（127.0.0.1:3306/charge_safe_dev）
- `application-dev.yml` — 云端共享开发库（SSH 隧道 127.0.0.1:13306）
- `application-prod.yml` — 云端正式演示库

---

## 二、项目目录结构

```
charge-safe/
├── charge-safe-backend/              # Spring Boot 后端服务
│   ├── pom.xml                       # Maven 项目配置
│   └── src/main/
│       ├── java/com/chargesafe/
│       │   ├── common/               # 公共类：Result, PageResult, 异常处理, AOP 日志
│       │   │   ├── Result.java       # 统一返回格式
│       │   │   ├── ResultCode.java   # 状态码枚举
│       │   │   ├── PageResult.java   # 分页返回
│       │   │   ├── PageQuery.java    # 分页查询参数
│       │   │   ├── BusinessException.java  # 业务异常
│       │   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│       │   │   ├── OperationLog.java       # 操作日志注解
│       │   │   ├── OperationLogAspect.java # 日志 AOP 切面
│       │   │   ├── OperationLogEntity.java # 日志实体
│       │   │   └── OperationLogMapper.java # 日志 Mapper
│       │   ├── config/               # 配置类
│       │   │   ├── SecurityConfig.java     # JWT 过滤器注册
│       │   │   ├── WebConfig.java          # Web 配置
│       │   │   └── MybatisPlusConfig.java  # MyBatis-Plus 配置
│       │   ├── security/             # 安全模块
│       │   │   ├── JwtUtils.java           # JWT 工具类
│       │   │   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│       │   │   ├── LoginUser.java          # 登录用户信息
│       │   │   ├── UserContext.java        # 用户上下文（ThreadLocal）
│       │   │   ├── RequiresPermission.java # 权限注解
│       │   │   └── PermissionAspect.java   # 权限校验切面
│       │   └── modules/              # 业务模块
│       │       ├── auth/             # 认证模块
│       │       ├── user/             # 用户/角色/菜单管理
│       │       ├── community/        # 社区/车棚管理
│       │       ├── device/           # 设备/端口管理
│       │       ├── charging/         # 预约/充电/订单
│       │       ├── repair/           # 报修/工单
│       │       ├── inspection/       # 巡检/隐患
│       │       ├── notification/     # 消息通知
│       │       ├── statistics/       # 统计分析
│       │       ├── simulator/        # 设备模拟
│       │       └── log/              # 操作日志查询
│       └── resources/
│           ├── application.yml           # 通用配置
│           ├── application-local.yml     # 本地开发配置
│           ├── application-dev.yml       # 云端开发配置
│           ├── application-prod.yml      # 云端生产配置
│           └── db/migration/             # Flyway 迁移文件
│               ├── V1__init_schema.sql   # 19 张业务表
│               ├── V2__init_data.sql     # 初始数据
│               ├── V3__add_menu_tables.sql  # RBAC 菜单权限表
│               ├── V4__add_community_coordinates.sql  # 社区经纬度
│               └── V5__adjust_property_admin_menus.sql  # 物业管理员菜单
├── charge-safe-admin/                # Vue3 Web 管理端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/                      # API 接口封装（12 个文件）
│       ├── views/                    # 页面组件
│       ├── router/                   # 路由配置
│       ├── layouts/                  # 布局组件
│       └── stores/                   # Pinia 状态管理
├── charge-safe-miniapp/              # uni-app 小程序端
│   ├── package.json
│   └── src/
│       ├── pages/                    # 页面
│       └── api/                      # 请求封装
├── sql/                              # 原始数据库脚本（仅供参考）
├── docs/                             # 项目文档
├── README.md
└── .gitignore
```

**各目录作用说明：**
- `common/`：统一返回格式、全局异常处理、分页组件、AOP 操作日志
- `config/`：Spring 配置类（安全、Web、MyBatis-Plus）
- `security/`：JWT 认证、用户上下文、权限注解与切面
- `modules/`：按业务领域划分的模块（每个含 entity、mapper、service、controller、dto、vo、enums）
- `db/migration/`：Flyway 版本化数据库迁移脚本
- `api/`（前端）：按模块封装的 API 请求函数
- `views/`（前端）：页面组件，按功能模块组织
- `router/`（前端）：路由配置，含角色权限控制

---

## 三、系统参与者与用户角色事实

### 1. 系统真实角色

系统定义了 **5 种角色**，存储在 `sys_role` 表中：

| ID | 角色名称 | 角色编码 | 描述 | 证据 |
|----|----------|----------|------|------|
| 1 | 平台管理员 | PLATFORM_ADMIN | 系统最高权限管理者 | `V2__init_data.sql:11` |
| 2 | 物业管理员 | PROPERTY_ADMIN | 社区级管理者 | `V2__init_data.sql:12` |
| 3 | 运维人员 | MAINTAINER | 设备维修执行者 | `V2__init_data.sql:13` |
| 4 | 巡检人员 | INSPECTOR | 安全巡检执行者 | `V2__init_data.sql:14` |
| 5 | 居民用户 | RESIDENT | 充电服务使用者 | `V2__init_data.sql:15` |

### 2. 每类角色的菜单权限

通过 `sys_role_menu` 表关联，定义在 `V3__add_menu_tables.sql:97-134`：

**平台管理员（PLATFORM_ADMIN）：** 拥有所有菜单权限（第 97-98 行：`SELECT 1, id FROM sys_menu WHERE deleted = 0`）

**物业管理员（PROPERTY_ADMIN）：** 工作台、社区列表、车棚管理、充电设备、充电端口、预约记录、订单监控、报修记录、维修工单、巡检任务、安全隐患、消息通知、统计分析、可视化大屏、操作日志

**运维人员（MAINTAINER）：** 工作台、维修工单、消息通知

**巡检人员（INSPECTOR）：** 工作台、巡检任务、安全隐患、消息通知

**居民用户（RESIDENT）：** 未分配后台管理菜单（主要使用小程序端）

### 3. 权限控制实现位置

**前端权限控制：**
- 路由守卫：`router/index.js:56-119`，`beforeEach` 钩子检查 token 和角色
- 路由 meta.roles：`ops.routes.js:5` 定义路由可见角色，如 `roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN']`
- 按钮级：前端有本地降级权限矩阵（`stores/user.js:41` 注释提到）

**后端权限控制：**
- JWT 过滤器：`JwtAuthenticationFilter.java`，拦截 `/api/*` 路径，解析 Token 设置用户上下文
- 权限注解：`RequiresPermission.java` + `PermissionAspect.java`，方法级权限校验（但当前**未被任何 Controller 使用**，见 `grep @RequiresPermission` 无结果）
- 社区数据隔离：部分 Service 方法通过 `UserContext.getCommunityId()` 获取当前用户所属社区

**数据库权限控制：**
- RBAC 模型：`sys_user` ↔ `sys_user_role` ↔ `sys_role` ↔ `sys_role_menu` ↔ `sys_menu`
- 菜单表含 `permission_code` 字段（`sys_menu.permission_code`）

### 4. 权限控制机制清单

| 机制 | 实现位置 | 证据 |
|------|----------|------|
| JWT 认证过滤器 | `JwtAuthenticationFilter.java` 拦截 `/api/*` | `SecurityConfig.java:22` |
| 前端路由守卫 | `router/index.js:56-119` `beforeEach` | 路由配置文件 |
| 前端路由角色限制 | `ops.routes.js:5` `meta.roles` | 路由配置文件 |
| 后端权限注解 | `@RequiresPermission` + `PermissionAspect` | `PermissionAspect.java` |
| AOP 操作日志 | `@OperationLog` + `OperationLogAspect` | `OperationLogAspect.java` |
| MyBatis-Plus 逻辑删除 | `deleted` 字段，全局配置 | `application.yml:26-28` |

### 5. 重要发现

**`@RequiresPermission` 注解已定义但未被使用**：`PermissionAspect` 中实现了方法级权限校验逻辑，但 grep 全项目后未发现任何 Controller 方法标注了 `@RequiresPermission`。这意味着后端接口级权限校验**实际上未生效**，当前权限控制主要依赖前端路由守卫和社区数据隔离。

---

## 四、已实现功能模块清单

### 模块 1：用户认证（Auth）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `POST /api/auth/login`（登录）、`GET /api/auth/me`（当前用户）、`GET /api/auth/menus`（当前用户菜单）、`GET /api/auth/permissions`（当前用户权限码） |
| 前端页面 | `views/auth/Login.vue`（登录页） |
| 数据表 | `sys_user`、`sys_role`、`sys_user_role` |
| 关键逻辑 | BCrypt 密码验证、JWT Token 生成与校验、登录成功写入操作日志 |
| 证据 | `AuthController.java`、`AuthServiceImpl.java`、`Login.vue` |

### 模块 2：用户管理（User）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `GET /api/users`（分页）、`POST /api/users`（新增）、`PUT /api/users/{id}`（编辑）、`DELETE /api/users/{id}`（删除）、`PUT /api/users/{id}/status`（启停）、`PUT /api/users/{id}/roles`（分配角色）、`POST /api/users/{id}/reset-password`（重置密码） |
| 前端页面 | `views/system/User.vue`（用户管理页面） |
| 数据表 | `sys_user`、`sys_user_role` |
| 关键逻辑 | 用户 CRUD、状态启停、角色分配、密码重置（BCrypt 加密） |
| 证据 | `UserController.java`、`system.js` API |

### 模块 3：角色管理（Role）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `GET /api/roles`（分页）、`GET /api/roles/list`（列表）、`POST /api/roles`（新增）、`PUT /api/roles/{id}`（编辑）、`DELETE /api/roles/{id}`（删除）、`PUT /api/roles/{id}/menus`（分配菜单） |
| 前端页面 | `views/system/Role.vue` |
| 数据表 | `sys_role`、`sys_role_menu` |
| 关键逻辑 | 角色 CRUD、菜单权限分配（el-tree 组件） |
| 证据 | `RoleController.java` |

### 模块 4：菜单管理（Menu）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `GET /api/menus`（列表）、`GET /api/menus/tree`（树形）、`POST /api/menus`（新增）、`PUT /api/menus/{id}`（编辑）、`DELETE /api/menus/{id}`（删除） |
| 前端页面 | `views/system/Menu.vue` |
| 数据表 | `sys_menu` |
| 关键逻辑 | 菜单树构建、目录/菜单/按钮三级类型、排序 |
| 证据 | `MenuController.java`、`V3__init_schema.sql:7-26` |

### 模块 5：社区管理（Community）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `GET /api/communities`（分页）、`POST /api/communities`（新增）、`PUT /api/communities/{id}`（编辑）、`DELETE /api/communities/{id}`（删除）、`PUT /api/communities/{id}/status`（启停） |
| 前端页面 | `views/community/Index.vue`（社区列表） |
| 数据表 | `community` |
| 关键逻辑 | 社区 CRUD、状态启停、经度纬度（V4 迁移添加） |
| 证据 | `CommunityController.java`、`V4__add_community_coordinates.sql` |

### 模块 6：车棚管理（ParkingArea）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `GET /api/parking-areas`（分页）、`GET /api/parking-areas/list`（列表）、`POST /api/parking-areas`（新增）、`PUT /api/parking-areas/{id}`（编辑）、`DELETE /api/parking-areas/{id}`（删除） |
| 前端页面 | `views/community/ParkingArea.vue` |
| 数据表 | `parking_area` |
| 关键逻辑 | 车棚 CRUD、关联社区 |
| 证据 | `ParkingAreaController.java` |

### 模块 7：设备管理（Device）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `GET /api/devices`（分页）、`GET /api/devices/list`（列表）、`POST /api/devices`（新增）、`PUT /api/devices/{id}`（编辑）、`DELETE /api/devices/{id}`（删除） |
| 前端页面 | `views/device/Index.vue`（充电设备）、`views/device/Port.vue`（充电端口） |
| 数据表 | `charging_device`、`charging_port` |
| 关键逻辑 | 设备 CRUD、端口状态管理（IDLE/RESERVED/CHARGING/FAULT/MAINTENANCE/DISABLED/OFFLINE） |
| 证据 | `DeviceController.java`、`PortController.java`、`PortStatus.java` |

### 模块 8：预约充电（Charging）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `POST /api/reservations`（预约）、`POST /api/reservations/{id}/cancel`（取消）、`POST /api/reservations/{id}/expire`（过期取消）、`POST /api/reservations/{id}/fault-cancel`（故障取消）、`GET /api/reservations/my`（我的预约）、`GET /api/reservations`（分页查询）、`POST /api/charging/start`（开始充电）、`POST /api/charging/start-direct`（无预约直接充电）、`POST /api/charging/{orderId}/finish`（结束充电）、`GET /api/orders`（分页）、`GET /api/orders/my`（我的订单）、`GET /api/orders/{id}`（订单详情） |
| 前端页面 | `views/charging/Reservation.vue`（预约记录）、`views/charging/Order.vue`（订单监控） |
| 数据表 | `charging_reservation`、`charging_order`、`charging_fee_rule` |
| 关键逻辑 | 预约冲突检测（同一端口/同一用户）、30 分钟有效期、端口状态流转、费用计算（1.5 元/小时，最低 0.5 元）、订单号生成、通知推送 |
| 证据 | `ChargingController.java`、`ChargingServiceImpl.java`（426 行完整实现） |

### 模块 9：报修工单（Repair）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `POST /api/repair-reports`（提交报修）、`GET /api/repair-reports`（分页）、`GET /api/repair-reports/{id}`（详情）、`POST /api/repair-tickets/dispatch`（派单）、`POST /api/repair-tickets/{id}/accept`（接单）、`POST /api/repair-tickets/{id}/start`（开始处理）、`POST /api/repair-tickets/{id}/finish`（维修完成）、`POST /api/repair-tickets/{id}/accept-repair`（验收）、`POST /api/repair-tickets/{id}/reject`（驳回）、`POST /api/repair-tickets/{id}/close`（关闭）、`GET /api/repair-tickets`（分页）、`GET /api/repair-tickets/{id}`（详情） |
| 前端页面 | `views/repair/Report.vue`（报修记录）、`views/repair/Ticket.vue`（维修工单） |
| 数据表 | `repair_report`、`repair_ticket`、`repair_ticket_log` |
| 关键逻辑 | 报修→派单→接单→处理→完成→验收→关闭 全流程，工单日志记录，状态机流转 |
| 证据 | `RepairController.java`、`RepairServiceImpl.java` |

### 模块 10：巡检隐患（Inspection）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `POST /api/inspection-tasks`（创建任务）、`GET /api/inspection-tasks`（分页）、`GET /api/inspection-tasks/my`（我的任务）、`GET /api/inspection-tasks/{id}`（详情）、`POST /api/inspection-tasks/{id}/start`（开始巡检）、`POST /api/inspection-tasks/{id}/submit`（提交巡检结果）、`POST /api/inspection-tasks/{id}/complete`（完成任务）、`POST /api/hazards`（上报隐患）、`GET /api/hazards`（分页）、`GET /api/hazards/{id}`（详情）、`POST /api/hazards/{id}/rectify`（整改）、`POST /api/hazards/{id}/review`（复查）、`POST /api/hazards/{id}/close`（关闭） |
| 前端页面 | `views/inspection/Task.vue`（巡检任务）、`views/inspection/Hazard.vue`（隐患整改） |
| 数据表 | `inspection_task`、`inspection_record`、`safety_hazard` |
| 关键逻辑 | 巡检任务状态机（PENDING→IN_PROGRESS→COMPLETED/ABNORMAL）、隐患上报→整改→复查→关闭 |
| 证据 | `InspectionController.java`、`InspectionServiceImpl.java` |

### 模块 11：消息通知（Notification）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `GET /api/notifications`（分页）、`GET /api/notifications/my`（我的通知）、`PUT /api/notifications/{id}/read`（标记已读）、`PUT /api/notifications/read-all`（全部已读） |
| 前端页面 | `views/notification/Index.vue` |
| 数据表 | `notification` |
| 关键逻辑 | 通知推送（预约、充电、报修、巡检、隐患、系统）、已读/未读状态管理 |
| 证据 | `NotificationController.java` |

### 模块 12：统计分析（Statistics）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `GET /api/statistics/dashboard`（仪表盘）、`GET /api/statistics/port-status`（端口状态分布）、`GET /api/statistics/order-trend`（订单趋势）、`GET /api/statistics/repair-summary`（工单汇总）、`GET /api/statistics/hazard-summary`（隐患汇总）、`GET /api/statistics/parking-area-usage`（车棚使用率） |
| 前端页面 | `views/statistics/Index.vue`（统计分析页） |
| VO 类 | `DashboardVO`、`PortStatusStatisticsVO`、`OrderTrendVO`、`RepairSummaryVO`、`HazardSummaryVO`、`ParkingAreaUsageVO` |
| 关键逻辑 | 多维度统计查询，支持按社区过滤 |
| 证据 | `StatisticsController.java` |

### 模块 13：可视化大屏（BigScreen）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 前端页面 | `views/bigscreen/Index.vue`（全屏可视化大屏） |
| 路由 | `/bigscreen`，无需登录即可访问（`meta: { requiresAuth: false }`） |
| 关键逻辑 | 端口状态矩阵、告警滚动列表、趋势图表，复用统计接口 |
| 证据 | `router/index.js:22-26`、`README.md:344-351` |

### 模块 14：设备模拟（Simulator）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `POST /api/simulator/ports/{id}/status`（模拟状态修改）、`POST /api/simulator/ports/{id}/fault`（模拟故障）、`POST /api/simulator/ports/{id}/recover`（模拟恢复）、`POST /api/simulator/charging/{orderId}/complete`（模拟充电完成） |
| 前端页面 | `views/permission/simulator/Index.vue` |
| 数据表 | 操作 `charging_port`、`charging_order` 表 |
| 关键逻辑 | 模拟端口状态变化、故障告警触发 |
| 证据 | `SimulatorController.java`、`SimulatorServiceImpl.java` |

### 模块 15：操作日志（Log）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是 |
| 后端接口 | `GET /api/logs`（分页查询）、`GET /api/logs/my`（我的日志） |
| 前端页面 | `views/log/Index.vue` |
| 数据表 | `operation_log` |
| 关键逻辑 | AOP 自动记录（`@OperationLog` 注解）、支持按用户名/模块筛选 |
| 证据 | `LogController.java`、`OperationLogAspect.java` |

### 模块 16：小程序端（MiniApp）

| 项目 | 详情 |
|------|------|
| 是否已实现 | ✅ 是（基础原型） |
| 页面 | 登录、首页、车棚列表/详情、预约确认/记录、充电中、订单列表/详情、报修提交/记录、消息列表、个人中心 |
| API 封装 | `api/request.js`（请求拦截器，Token 注入） |
| 关键逻辑 | 对接后端 API，降级模拟登录 |
| 证据 | `charge-safe-miniapp/src/pages/` 目录 |

---

## 五、核心业务流程事实

### 流程 1：用户登录

**触发者：** 任何用户

**处理过程：**
1. 前端 `Login.vue` 收集用户名和密码，调用 `POST /api/auth/login`
2. 后端 `AuthServiceImpl.login()` 查询 `sys_user` 表验证用户名
3. 使用 BCrypt 验证密码
4. 生成 JWT Token（含 userId、username、roleCode），有效期 24 小时
5. 写入操作日志（`@OperationLog`）
6. 返回 Token + 用户信息（userId、username、nickname、roleCode、roleName、communityId）
7. 前端存储 Token 和用户信息到 localStorage，异步获取权限列表

**异常情况：**
- 用户名不存在 → 返回 `USERNAME_OR_PASSWORD_ERROR(4004)`
- 密码错误 → 返回 `USERNAME_OR_PASSWORD_ERROR(4004)`
- 账号被禁用 → 返回 `ACCOUNT_DISABLED(4003)`

**证据：** `AuthController.java:28-30`、`AuthServiceImpl.java`

### 流程 2：预约充电

**触发者：** 居民用户（小程序端）或管理员（管理端）

**处理过程：**
1. 用户选择端口，调用 `POST /api/reservations`
2. 后端校验：
   - 用户是否已有有效预约（`PENDING` 状态）→ 有则抛出 `RESERVATION_ALREADY_EXIST(6001)`
   - 端口是否已有有效预约 → 有则抛出 "该端口已有有效预约"
   - 端口状态是否为 `IDLE` → 不是则抛出 `PORT_NOT_IDLE(6002)`
3. 获取端口所属社区 ID（端口→设备→车棚→社区）
4. 创建预约记录（status=`PENDING`，expire_time=当前+30分钟）
5. 更新端口状态为 `RESERVED`
6. 发送预约成功通知

**异常情况：**
- 端口不可用 → `PORT_NOT_IDLE(6002)`
- 用户已有预约 → `RESERVATION_ALREADY_EXIST(6001)`
- 端口不存在 → `DATA_NOT_EXIST(5001)`

**证据：** `ChargingServiceImpl.java:53-120`（`@Transactional` 事务控制）

### 流程 3：开始充电

**触发者：** 居民用户

**处理过程：**
1. 用户扫码或手动触发，调用 `POST /api/charging/start?reservationId=xxx`
2. 后端校验：
   - 预约是否存在且属于当前用户
   - 预约状态是否为 `PENDING`
   - 预约是否已过期（超过 expire_time）→ 是则自动设为 `EXPIRED`
   - 端口状态是否为 `RESERVED`
3. 更新预约状态为 `CONVERTED`
4. 更新端口状态为 `CHARGING`
5. 创建充电订单（order_no 生成规则：`ORD` + 时间戳 + 序号）

**无预约直接充电：** `POST /api/charging/start-direct?portId=xxx`，跳过预约校验，直接创建订单

**证据：** `ChargingServiceImpl.java:242-290`（startCharging）、`293-335`（startChargingDirectly）

### 流程 4：结束充电

**触发者：** 居民用户

**处理过程：**
1. 调用 `POST /api/charging/{orderId}/finish`
2. 后端校验订单存在性、归属、状态
3. 计算充电时长（分钟）和费用：
   - 费率：1.50 元/小时
   - 最低消费：0.50 元
   - 计算公式：`时长(分钟)/60 × 1.50`，结果保留 2 位小数
4. 更新订单状态为 `COMPLETED`，记录 end_time、duration_minutes、amount
5. 恢复端口状态为 `IDLE`
6. 发送充电完成通知

**证据：** `ChargingServiceImpl.java:338-385`

### 流程 5：报修→工单全流程

**触发者：** 居民用户（报修）→ 物业管理员（派单）→ 运维人员（接单/处理）→ 物业管理员（验收）

**处理过程：**
1. **提交报修**：居民调用 `POST /api/repair-reports`，创建报修记录（status=`PENDING`）
2. **派发工单**：物业调用 `POST /api/repair-tickets/dispatch`，创建工单（status=`DISPATCHED`），记录派单人、维修人员
3. **接单**：运维调用 `POST /api/repair-tickets/{id}/accept`，状态→`PROCESSING`
4. **开始处理**：运维调用 `POST /api/repair-tickets/{id}/start`
5. **维修完成**：运维调用 `POST /api/repair-tickets/{id}/finish`，提交维修结果和照片，状态→`WAITING_ACCEPTANCE`
6. **验收**：物业调用 `POST /api/repair-tickets/{id}/accept-repair`，状态→`FINISHED`→`CLOSED`
7. **驳回**（可选）：物业调用 `POST /api/repair-tickets/{id}/reject`，状态→`REJECTED`
8. **关闭**：物业调用 `POST /api/repair-tickets/{id}/close`，状态→`CLOSED`

每次状态变更都记录到 `repair_ticket_log` 表。

**工单状态流转：** `PENDING → DISPATCHED → PROCESSING → WAITING_ACCEPTANCE → FINISHED → CLOSED`（或 `REJECTED`）

**证据：** `RepairController.java`、`RepairServiceImpl.java`、`TicketStatus.java`

### 流程 6：巡检→隐患全流程

**触发者：** 物业管理员（创建任务）→ 巡检人员（执行巡检）→ 巡检/物业（上报隐患）→ 整改人（整改）→ 复查人（复查）

**处理过程：**
1. **创建巡检任务**：物业调用 `POST /api/inspection-tasks`
2. **开始巡检**：巡检人员调用 `POST /api/inspection-tasks/{id}/start`，状态→`IN_PROGRESS`
3. **提交巡检结果**：巡检人员调用 `POST /api/inspection-tasks/{id}/submit`，记录检查项和结果
4. **完成任务**：调用 `POST /api/inspection-tasks/{id}/complete`，状态→`COMPLETED`（或发现异常→`ABNORMAL`）
5. **上报隐患**：调用 `POST /api/hazards`
6. **隐患整改**：整改人调用 `POST /api/hazards/{id}/rectify`，状态→`WAITING_REVIEW`
7. **隐患复查**：复查人调用 `POST /api/hazards/{id}/review`，通过→`REVIEW_PASSED`→`CLOSED`，不通过→`REVIEW_FAILED`
8. **关闭隐患**：调用 `POST /api/hazards/{id}/close`

**隐患状态流转：** `PENDING → RECTIFYING → WAITING_REVIEW → REVIEW_PASSED/CLOSED`（或 `REVIEW_FAILED` 循环整改）

**证据：** `InspectionController.java`、`InspectionServiceImpl.java`、`HazardStatus.java`、`TaskStatus.java`

### 流程 7：消息通知

**触发者：** 系统自动触发

**处理过程：**
- 预约成功/取消/过期/故障取消 → 发送 `RESERVATION` 类型通知
- 充电完成 → 发送 `CHARGING` 类型通知
- 工单状态变更 → 发送 `REPAIR` 类型通知
- 隐患状态变更 → 发送 `HAZARD` 类型通知
- 巡检任务分配 → 发送 `INSPECTION` 类型通知

**证据：** `ChargingServiceImpl.java:114-118`（预约通知）、`379-382`（充电完成通知）、`notification.type` 枚举值

---

## 六、数据结构与数据管理事实

### 1. 持久化方式
**MySQL 8.x 关系型数据库**，使用 Flyway 进行版本化迁移管理。

证据：`application.yml:10-14` Flyway 配置、`db/migration/` 目录下 V1-V5 迁移文件

### 2. 数据表清单（共 22 张表）

| 序号 | 表名 | 用途 | 证据 |
|------|------|------|------|
| 1 | `sys_user` | 用户表 | `V1__init_schema.sql:13-28` |
| 2 | `sys_role` | 角色表 | `V1__init_schema.sql:31-42` |
| 3 | `sys_user_role` | 用户角色关联表 | `V1__init_schema.sql:45-53` |
| 4 | `operation_log` | 操作日志表 | `V1__init_schema.sql:56-74` |
| 5 | `community` | 社区表 | `V1__init_schema.sql:81-92` |
| 6 | `parking_area` | 车棚/停放区表 | `V1__init_schema.sql:95-107` |
| 7 | `charging_device` | 充电设备表 | `V1__init_schema.sql:110-124` |
| 8 | `charging_port` | 充电端口表 | `V1__init_schema.sql:127-140` |
| 9 | `resident_profile` | 居民档案表 | `V1__init_schema.sql:147-161` |
| 10 | `vehicle` | 车辆表 | `V1__init_schema.sql:164-176` |
| 11 | `charging_reservation` | 充电预约表 | `V1__init_schema.sql:183-199` |
| 12 | `charging_order` | 充电订单表 | `V1__init_schema.sql:202-223` |
| 13 | `repair_report` | 报修记录表 | `V1__init_schema.sql:230-247` |
| 14 | `repair_ticket` | 维修工单表 | `V1__init_schema.sql:250-277` |
| 15 | `repair_ticket_log` | 工单日志表 | `V1__init_schema.sql:280-291` |
| 16 | `inspection_task` | 巡检任务表 | `V1__init_schema.sql:298-317` |
| 17 | `inspection_record` | 巡检记录表 | `V1__init_schema.sql:320-331` |
| 18 | `safety_hazard` | 安全隐患表 | `V1__init_schema.sql:334-359` |
| 19 | `notification` | 消息通知表 | `V1__init_schema.sql:366-380` |
| 20 | `alarm_record` | 告警记录表 | `V1__init_schema.sql:383-398` |
| 21 | `charging_fee_rule` | 充电费率规则表 | `V1__init_schema.sql:401-413` |
| 22 | `sys_menu` | 系统菜单权限表 | `V3__add_menu_tables.sql:7-26` |
| 23 | `sys_role_menu` | 角色菜单关联表 | `V3__add_menu_tables.sql:28-37` |

### 3. 主要数据对象字段详情

**sys_user（用户表）：**
- 主键：`id`（bigint，自增）
- 唯一键：`uk_username`（username）
- 状态字段：`status`（0=禁用，1=启用）
- 时间字段：`created_time`、`updated_time`
- 逻辑删除：`deleted`
- 关联：`community_id` → `community.id`

**charging_port（充电端口表）：**
- 主键：`id`（bigint，自增）
- 唯一键：`uk_device_port`（device_id, port_number）
- 状态字段：`status`（varchar），枚举值见下文
- 时间字段：`created_time`、`updated_time`
- 逻辑删除：`deleted`
- 关联：`device_id` → `charging_device.id`

**charging_reservation（充电预约表）：**
- 主键：`id`（bigint，自增）
- 状态字段：`status`（PENDING/CONVERTED/CANCELLED/EXPIRED/FAULT_CANCELLED）
- 时间字段：`reserve_time`、`expire_time`、`created_time`、`updated_time`
- 逻辑删除：`deleted`
- 关联：`user_id` → `sys_user.id`、`port_id` → `charging_port.id`、`community_id` → `community.id`

**repair_ticket（维修工单表）：**
- 主键：`id`（bigint，自增）
- 唯一键：`uk_ticket_no`（ticket_no）
- 状态字段：`status`（PENDING/DISPATCHED/PROCESSING/WAITING_ACCEPTANCE/FINISHED/CLOSED/REJECTED）
- 时间字段：`dispatch_time`、`accept_time`、`finish_time`、`close_time`、`created_time`、`updated_time`
- 逻辑删除：`deleted`
- 关联：`report_id`、`community_id`、`device_id`、`port_id`、`dispatcher_id`、`assignee_id`

**safety_hazard（安全隐患表）：**
- 主键：`id`（bigint，自增）
- 状态字段：`status`（PENDING/RECTIFYING/WAITING_REVIEW/REVIEW_PASSED/REVIEW_FAILED/CLOSED）
- 严重程度：`severity`（GENERAL/SERIOUS/URGENT）
- 时间字段：`rectify_time`、`review_time`、`created_time`、`updated_time`
- 逻辑删除：`deleted`

### 4. 对象关联关系

| 关联 | 关系类型 | 证据 |
|------|----------|------|
| `community` → `parking_area` | 一对多（community.id → parking_area.community_id） | `V1__init_schema.sql:97` |
| `parking_area` → `charging_device` | 一对多（parking_area.id → charging_device.parking_area_id） | `V1__init_schema.sql:112` |
| `charging_device` → `charging_port` | 一对多（charging_device.id → charging_port.device_id） | `V1__init_schema.sql:129` |
| `sys_user` ↔ `sys_role` | 多对多（通过 `sys_user_role`） | `V1__init_schema.sql:45-53` |
| `sys_role` ↔ `sys_menu` | 多对多（通过 `sys_role_menu`） | `V3__add_menu_tables.sql:28-37` |
| `charging_reservation` → `charging_order` | 一对一（reservation_id） | `V1__init_schema.sql:207` |
| `repair_report` → `repair_ticket` | 一对一（report_id） | `V1__init_schema.sql:253` |
| `inspection_task` → `inspection_record` | 一对多（task_id） | `V1__init_schema.sql:322` |
| `inspection_task` → `safety_hazard` | 一对多（task_id） | `V1__init_schema.sql:337` |

**注意：** 数据库未使用物理外键约束（`SET FOREIGN_KEY_CHECKS = 0`），关联关系通过应用层代码维护。

### 5. 删除策略

**全部使用逻辑删除**，通过 `deleted` 字段（0=未删除，1=已删除）。

MyBatis-Plus 全局配置：
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

证据：`application.yml:25-28`

### 6. 枚举值清单

**端口状态（PortStatus）：**
| 枚举值 | 说明 |
|--------|------|
| IDLE | 空闲 |
| RESERVED | 已预约 |
| CHARGING | 充电中 |
| COMPLETED_WAITING_PICKUP | 已完成待取车 |
| OVERTIME_OCCUPIED | 超时占用 |
| FAULT | 故障 |
| MAINTENANCE | 维修中 |
| DISABLED | 停用 |
| OFFLINE | 离线 |

证据：`V1__init_schema.sql:132` 状态注释、`PortStatus.java`

**预约状态（ReservationStatus）：**
| 枚举值 | 说明 |
|--------|------|
| PENDING | 待生效 |
| CONVERTED | 已转订单 |
| CANCELLED | 用户取消 |
| EXPIRED | 超时取消 |
| FAULT_CANCELLED | 设备故障取消 |

证据：`V1__init_schema.sql:190`

**订单状态（OrderStatus）：**
| 枚举值 | 说明 |
|--------|------|
| CHARGING | 充电中 |
| COMPLETED | 已完成 |
| SETTLED | 已结算 |
| CANCELLED | 已取消 |
| ABNORMAL | 异常结束 |

证据：`V1__init_schema.sql:214`

**工单状态（TicketStatus）：**
| 枚举值 | 说明 |
|--------|------|
| PENDING | 待受理 |
| DISPATCHED | 已派单 |
| PROCESSING | 处理中 |
| WAITING_ACCEPTANCE | 待验收 |
| FINISHED | 已完成 |
| CLOSED | 已关闭 |
| REJECTED | 已驳回 |

证据：`V1__init_schema.sql:263`

**报修状态（ReportStatus）：**
| 枚举值 | 说明 |
|--------|------|
| PENDING | 待处理 |
| CONFIRMED | 已确认 |
| DISPATCHED | 已派单 |
| REJECTED | 已驳回 |

证据：`V1__init_schema.sql:239`

**巡检任务状态（TaskStatus）：**
| 枚举值 | 说明 |
|--------|------|
| PENDING | 待执行 |
| IN_PROGRESS | 执行中 |
| COMPLETED | 已完成 |
| ABNORMAL | 发现异常 |
| OVERDUE | 已逾期 |
| CANCELLED | 已取消 |

证据：`V1__init_schema.sql:308`

**隐患状态（HazardStatus）：**
| 枚举值 | 说明 |
|--------|------|
| PENDING | 待处理 |
| RECTIFYING | 整改中 |
| WAITING_REVIEW | 待复查 |
| REVIEW_PASSED | 复查通过 |
| REVIEW_FAILED | 复查不通过 |
| CLOSED | 已关闭 |

证据：`V1__init_schema.sql:343`

**通知类型（notification.type）：**
| 值 | 说明 |
|----|------|
| RESERVATION | 预约相关 |
| CHARGING | 充电相关 |
| REPAIR | 报修工单 |
| INSPECTION | 巡检相关 |
| HAZARD | 隐患相关 |
| SYSTEM | 系统通知 |

证据：`V1__init_schema.sql:371`

### 7. 初始数据

V2 迁移文件初始化了以下数据（`V2__init_data.sql`）：
- 5 种角色
- 6 个用户（admin、property01、maintainer01、inspector01、resident01、resident02），密码均为 123456
- 1 个社区（阳光花园小区）→ V4 补充至 5 个社区
- 2 个车棚 → V4 补充至 6 个
- 4 个充电设备
- 14 个充电端口
- 2 个居民档案
- 2 辆车辆
- 1 条充电费率规则（标准费率 1.50 元/小时）
- 1 条已完成订单、1 条待处理报修、1 条待执行巡检任务、1 条未读通知

---

## 七、接口与交互事实

### 后端 API 接口总览

共 **15 个 Controller**，实现约 **95+ 个 RESTful 接口**：

| Controller | 路径前缀 | 接口数 | 证据文件 |
|------------|----------|--------|----------|
| AuthController | `/api/auth` | 4 | `AuthController.java` |
| UserController | `/api/users` | 7 | `UserController.java` |
| RoleController | `/api/roles` | 6 | `RoleController.java` |
| MenuController | `/api/menus` | 5 | `MenuController.java` |
| CommunityController | `/api/communities` | 5 | `CommunityController.java` |
| ParkingAreaController | `/api/parking-areas` | 5 | `ParkingAreaController.java` |
| DeviceController | `/api/devices` | 5 | `DeviceController.java` |
| PortController | `/api/ports` | 8 | `PortController.java` |
| ChargingController | `/api/reservations`, `/api/charging`, `/api/orders` | 12 | `ChargingController.java` |
| RepairController | `/api/repair-reports`, `/api/repair-tickets` | 12 | `RepairController.java` |
| InspectionController | `/api/inspection-tasks`, `/api/hazards` | 13 | `InspectionController.java` |
| NotificationController | `/api/notifications` | 4 | `NotificationController.java` |
| StatisticsController | `/api/statistics` | 6 | `StatisticsController.java` |
| SimulatorController | `/api/simulator` | 4 | `SimulatorController.java` |
| LogController | `/api/logs` | 2 | `LogController.java` |

### 统一返回格式

所有接口返回 `Result<T>` 格式：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

证据：`Result.java`

### 分页查询格式

请求参数：`page`（页码）、`size`（每页条数）

返回 `PageResult<T>`：
```json
{
  "total": 100,
  "page": 1,
  "size": 10,
  "records": [...]
}
```

证据：`PageResult.java`、`PageQuery.java`

---

## 八、输入输出与数据校验事实

### 1. 前端表单校验

**登录页（Login.vue）：**
- 用户名：必填（`required: true`，触发方式 `blur`）
- 密码：必填（`required: true`，触发方式 `blur`）

证据：`Login.vue:51-54`

### 2. 后端参数校验

使用 Spring Validation（`@Valid` + `@RequestBody`）：
- `AuthController.login()` → `@Valid @RequestBody LoginDTO`
- `UserController.create()` → `@Valid @RequestBody`（推测，基于项目规范）
- `RepairController.createReport()` → `@Valid @RequestBody RepairReportDTO`
- `RepairController.dispatchTicket()` → `@Valid @RequestBody TicketDispatchDTO`
- `RepairController.finishTicket()` → `@Valid @RequestBody TicketFinishDTO`
- `InspectionController.createTask()` → `@Valid @RequestBody InspectionTaskDTO`
- `InspectionController.submitInspection()` → `@Valid @RequestBody InspectionSubmitDTO`
- `InspectionController.reportHazard()` → `@Valid @RequestBody HazardReportDTO`
- `InspectionController.rectifyHazard()` → `@Valid @RequestBody HazardRectifyDTO`
- `InspectionController.reviewHazard()` → `@Valid @RequestBody HazardReviewDTO`

证据：各 Controller 方法签名中的 `@Valid` 注解

### 3. 异常处理机制

**全局异常处理器（GlobalExceptionHandler.java）：**

| 异常类型 | HTTP 状态码 | 返回格式 | 证据行号 |
|----------|-------------|----------|----------|
| `BusinessException` | 200 | `Result.error(code, message)` | 27-30 |
| `MethodArgumentNotValidException` | 400 | `Result.error(400, field: message)` | 36-44 |
| `BindException` | 400 | `Result.error(400, field: message)` | 49-56 |
| `ConstraintViolationException` | 400 | `Result.error(400, message)` | 62-67 |
| `NoResourceFoundException` | 404 | 自定义 JSON（含 docs 等链接） | 72-84 |
| `Exception`（兜底） | 500 | `Result.error("系统异常，请联系管理员")` | 89-93 |

证据：`GlobalExceptionHandler.java` 完整文件

### 4. 业务规则校验

| 校验规则 | 位置 | 证据 |
|----------|------|------|
| 同一用户不能同时有多个有效预约 | `ChargingServiceImpl.java:62-69` | `selectCount` 查询 `PENDING` 状态 |
| 同一端口不能同时被预约 | `ChargingServiceImpl.java:72-79` | `selectCount` 查询端口的 `PENDING` 预约 |
| 端口必须处于 IDLE 状态才能预约 | `ChargingServiceImpl.java:82-88` | 检查 `PortStatus.IDLE` |
| 预约 30 分钟有效期 | `ChargingServiceImpl.java:106` | `plusMinutes(30)` |
| 预约过期后不能开始充电 | `ChargingServiceImpl.java:257-261` | `LocalDateTime.now().isAfter(reservation.getExpireTime())` |
| 充电最少计费 1 分钟 | `ChargingServiceImpl.java:355` | `if (minutes < 1) minutes = 1` |
| 最低消费 0.50 元 | `ChargingServiceImpl.java:361-362` | `if (amount.compareTo(new BigDecimal("0.50")) < 0)` |
| 只有 PENDING 状态预约才能取消 | `ChargingServiceImpl.java:134-136` | 状态校验 |

### 5. 未发现校验证据的输入

- 充电设备创建/编辑的字段格式校验（未见 `@NotBlank` 等注解在 DTO 中）
- 社区名称唯一性校验（数据库无唯一索引，代码中也未发现）
- 用户名创建时的唯一性依赖数据库唯一索引 `uk_username`

---

## 九、性能、安全、故障处理事实

| 机制 | 是否实现 | 证据 |
|------|----------|------|
| **分页查询** | ✅ 是 | 所有列表接口使用 `PageQuery` + `PageResult`，MyBatis-Plus `selectPage` |
| **缓存** | ❌ 未发现证据 | 未引入 Redis 或 Spring Cache 依赖，未发现 `@Cacheable` 注解 |
| **事务控制** | ✅ 是 | 核心业务方法使用 `@Transactional`，如 `ChargingServiceImpl` 的 `createReservation`、`startCharging`、`finishCharging` 等 |
| **并发控制** | ⚠️ 部分 | `ChargingServiceImpl` 使用 `AtomicLong ORDER_SEQ` 生成订单号；但预约冲突检测使用 `selectCount` + `insert`，非原子操作，高并发下可能存在竞态条件 |
| **异常处理** | ✅ 是 | `GlobalExceptionHandler` 统一处理 6 类异常 |
| **登录失败处理** | ✅ 是 | 返回 `USERNAME_OR_PASSWORD_ERROR(4004)`，`ACCOUNT_DISABLED(4003)` |
| **权限不足处理** | ⚠️ 部分 | `PermissionAspect` 已实现但未被使用；前端路由守卫会提示 "无权访问该页面" |
| **数据重复处理** | ✅ 是 | 数据库唯一索引（`uk_username`、`uk_role_code`、`uk_device_code`、`uk_device_port`、`uk_order_no`、`uk_ticket_no`、`uk_permission_code`、`uk_role_menu`、`uk_user_role`、`uk_user_id`） |
| **文件/数据库异常处理** | ✅ 是 | `GlobalExceptionHandler` 兜底 `Exception`，返回 "系统异常，请联系管理员" |
| **日志记录** | ✅ 是 | AOP 操作日志（`@OperationLog`），覆盖登录、用户管理、角色管理、菜单管理、社区管理、预约充电、报修工单、巡检隐患等操作 |
| **参数校验失败处理** | ✅ 是 | `GlobalExceptionHandler` 处理 `MethodArgumentNotValidException`、`BindException`、`ConstraintViolationException` |
| **统一返回格式** | ✅ 是 | 所有接口返回 `Result<T>` 格式 |
| **JWT Token 认证** | ✅ 是 | `JwtAuthenticationFilter` 拦截 `/api/*`，从 `Authorization: Bearer xxx` 解析 Token |
| **密码加密** | ✅ 是 | BCrypt 加密存储（`V2__init_data.sql:20`） |
| **逻辑删除** | ✅ 是 | 全表使用 `deleted` 字段，MyBatis-Plus 全局配置 |
| **MyBatis-Plus 驼峰映射** | ✅ 是 | `application.yml:22` `map-underscore-to-camel-case: true` |

---

## 十、适合中期文档绘制的图

### 1. 系统边界图 ✅ 可绘制

**图中应包含：**
- 外部实体：居民用户（小程序）、物业管理员（Web 管理端）、运维人员（Web 管理端）、巡检人员（Web 管理端）、平台管理员（Web 管理端）
- 系统边界：ChargeSafe 社区充电安全运维平台
- 外部系统：MySQL 数据库、微信小程序平台

**依据：** 系统包含两个客户端（管理端 + 小程序端）、一个后端服务、一个数据库

### 2. 系统用例图 ✅ 可绘制

**图中应包含：**
- 参与者：平台管理员、物业管理员、运维人员、巡检人员、居民用户
- 用例：登录、用户管理、角色管理、菜单管理、社区管理、车棚管理、设备管理、端口管理、预约充电、开始充电、结束充电、查看订单、提交报修、派发工单、接单处理、验收工单、创建巡检任务、执行巡检、上报隐患、整改隐患、复查隐患、查看统计、消息通知

**依据：** 所有 Controller 接口均已在源码中实现

### 3. 系统功能结构图 ✅ 可绘制

**图中应包含：**
- 一级模块：用户认证、用户管理、角色管理、菜单管理、社区管理、设备管理、充电业务、运维管理、巡检管理、消息中心、统计分析、系统监控
- 二级功能：每个模块下的具体功能点

**依据：** `V3__add_menu_tables.sql` 中的菜单树结构、`ops.routes.js` 和 `permission.routes.js` 中的路由结构

### 4. 系统总体架构图 ✅ 可绘制

**图中应包含：**
- 表现层：Vue3 Web 管理端（Element Plus + ECharts）、uni-app 小程序端
- 接口层：Spring Boot REST API（JWT 认证、Knife4j 文档）
- 业务层：各业务模块 Service
- 数据层：MyBatis-Plus + MySQL 8.x
- 基础设施：Flyway 数据库迁移、AOP 操作日志

**依据：** 项目实际技术栈和分层结构

### 5. 核心类图 ✅ 可绘制

**图中应包含：**
- 实体类：`SysUser`、`SysRole`、`Community`、`ParkingArea`、`ChargingDevice`、`ChargingPort`、`ChargingReservation`、`ChargingOrder`、`RepairReport`、`RepairTicket`、`InspectionTask`、`SafetyHazard`、`Notification`
- 枚举类：`PortStatus`、`ReservationStatus`、`OrderStatus`、`TicketStatus`、`HazardStatus`
- 关键关联关系

**依据：** 所有实体类和枚举类均已在源码中定义

### 6. 核心时序图 ✅ 可绘制

**建议绘制以下时序图：**
- 预约充电时序图：用户→小程序→Controller→Service→Mapper→DB→返回
- 开始充电时序图
- 报修工单流程时序图
- 巡检隐患流程时序图

**依据：** `ChargingServiceImpl.java`、`RepairServiceImpl.java`、`InspectionServiceImpl.java` 中的完整业务逻辑

### 7. 概念模型图（E-R 图）✅ 可绘制

**图中应包含：**
- 实体：用户、角色、社区、车棚、设备、端口、预约、订单、报修、工单、巡检任务、巡检记录、隐患、通知、日志
- 关系：用户-角色（多对多）、社区-车棚（一对多）、车棚-设备（一对多）、设备-端口（一对多）等

**依据：** `V1__init_schema.sql` + `V3__add_menu_tables.sql` 中的完整表结构

### 8. 逻辑模型图 ✅ 可绘制

**依据：** 同概念模型，加上字段名、主键、外键关系

### 9. 顶层数据流图 ✅ 可绘制

**图中应包含：**
- 外部实体：居民用户、物业管理员、运维人员、巡检人员、平台管理员
- 处理过程：ChargeSafe 系统
- 数据流：登录信息、预约请求、充电指令、报修信息、巡检数据、统计查询
- 数据存储：用户数据、设备数据、预约数据、订单数据、工单数据、巡检数据

### 10. 运行环境图 ✅ 可绘制

**图中应包含：**
- 开发环境：本地 MySQL、Node.js、JDK 17
- 部署环境：服务器（Nginx + Spring Boot JAR + MySQL）
- 访问方式：浏览器（Web 管理端）、微信小程序

**依据：** `README.md` 中的部署说明

### 11. 业务流程图 ✅ 可绘制

**建议绘制：**
- 预约充电业务流程图
- 报修工单全流程图
- 巡检隐患全流程图
- 端口状态流转图

**依据：** 源码中完整实现的状态机和业务逻辑

### 12. 一层数据流图 ✅ 可绘制

**依据：** 各模块的输入输出数据流

---

## 十一、适合小范围改进的建议

### 建议 1：启用后端接口权限校验

| 项目 | 内容 |
|------|------|
| 建议名称 | 在 Controller 方法上添加 `@RequiresPermission` 注解 |
| 当前基础 | `RequiresPermission` 注解和 `PermissionAspect` 切面已完整实现 |
| 需改动文件 | 各 Controller 方法（`UserController`、`RoleController`、`MenuController` 等） |
| 风险等级 | 低（注解和切面已就绪，只需添加注解） |
| 提升效果 | 后端接口级权限控制生效，防止越权访问；中期文档可描述完整的 RBAC 权限模型 |

### 建议 2：补充 `LoginUser.hasPermission()` 方法

| 项目 | 内容 |
|------|------|
| 建议名称 | 在 `LoginUser` 类中添加 `hasPermission` 方法 |
| 当前基础 | `PermissionAspect` 调用 `user.hasPermission()` 但该方法不存在 |
| 需改动文件 | `LoginUser.java` |
| 风险等级 | 低（添加一个方法） |
| 提升效果 | 修复编译/运行时错误，使权限校验功能完整可用 |

### 建议 3：添加数据库物理外键或文档化关联关系

| 项目 | 内容 |
|------|------|
| 建议名称 | 在数据库设计文档中明确标注表间关联关系 |
| 当前基础 | 表结构中有逻辑关联字段（如 `community_id`、`device_id`），但无物理外键 |
| 需改动文件 | `docs/design/database-design.md` |
| 风险等级 | 无（仅文档） |
| 提升效果 | 中期文档中可准确描述数据模型的关联关系 |

### 建议 4：补充单元测试覆盖率

| 项目 | 内容 |
|------|------|
| 建议名称 | 为核心 Service 添加更多单元测试 |
| 当前基础 | 已有 `ChargingServiceTest`（15 个用例）和 `MenuTreeBuilderTest` |
| 需改动文件 | `src/test/java/` 下新建测试类 |
| 风险等级 | 低（仅添加测试） |
| 提升效果 | 中期文档可描述测试策略和覆盖率 |

### 建议 5：小程序端增加预约流程页面

| 项目 | 内容 |
|------|------|
| 建议名称 | 完善小程序端的预约→充电→结束完整流程页面 |
| 当前基础 | 小程序已有车棚详情、预约确认、充电中等页面 |
| 需改动文件 | `charge-safe-miniapp/src/pages/` 下相关 `.vue` 文件 |
| 风险等级 | 中（涉及前端页面交互） |
| 提升效果 | 展示完整的用户侧充电流程，增强答辩演示效果 |

---

## 十二、证据索引表

| 事实点 | 证据文件 | 证据位置 | 说明 |
|--------|----------|----------|------|
| 项目名称 | `README.md` | 第 1 行 | `# ChargeSafe 社区充电安全运维平台` |
| 后端框架 Spring Boot 3.2.5 | `pom.xml` | 第 10 行 | `<version>3.2.5</version>` |
| Java 版本 17 | `pom.xml` | 第 21 行 | `<java.version>17</java.version>` |
| MyBatis-Plus 3.5.5 | `pom.xml` | 第 22 行 | `<mybatis-plus.version>3.5.5</mybatis-plus.version>` |
| MySQL 数据库 | `pom.xml` | 第 49-52 行 | mysql-connector-j 依赖 |
| Flyway 迁移 | `pom.xml` | 第 55-63 行 | flyway-core + flyway-mysql 依赖 |
| JWT 认证 | `pom.xml` | 第 25 行 | `<jjwt.version>0.12.5</jjwt.version>` |
| BCrypt 密码加密 | `pom.xml` | 第 91-94 行 | spring-security-crypto 依赖 |
| Knife4j 接口文档 | `pom.xml` | 第 24 行 | `<knife4j.version>4.4.0</knife4j.version>` |
| AOP 支持 | `pom.xml` | 第 43-46 行 | spring-boot-starter-aop 依赖 |
| 参数校验 | `pom.xml` | 第 37-40 行 | spring-boot-starter-validation 依赖 |
| Vue3 前端 | `vite.config.js` | 第 2 行 | `import vue from '@vitejs/plugin-vue'` |
| Element Plus UI | `Login.vue` | 第 8 行 | `<el-form>` 等组件 |
| Pinia 状态管理 | `stores/user.js` | 第 1 行 | `import { defineStore } from 'pinia'` |
| Axios HTTP 客户端 | `api/request.js` | 第 1 行 | `import axios from 'axios'` |
| uni-app 小程序 | `charge-safe-miniapp/src/manifest.json` | — | uni-app 项目配置 |
| 后端端口 8080 | `application.yml` | 第 2 行 | `port: ${SERVER_PORT:8080}` |
| 前端端口 3000 | `vite.config.js` | 第 12 行 | `port: 3000` |
| API 代理配置 | `vite.config.js` | 第 14-18 行 | `/api` → `http://localhost:8080` |
| JWT 过期 24h | `application.yml` | 第 34 行 | `expiration: 86400000` |
| 逻辑删除配置 | `application.yml` | 第 25-28 行 | `logic-delete-field: deleted` |
| 5 种角色 | `V2__init_data.sql` | 第 10-15 行 | PLATFORM_ADMIN 到 RESIDENT |
| 6 个初始用户 | `V2__init_data.sql` | 第 22-28 行 | admin 到 resident02 |
| 用户角色关联 | `V2__init_data.sql` | 第 33-39 行 | 5+1 个关联 |
| RBAC 菜单表 | `V3__add_menu_tables.sql` | 第 7-26 行 | `sys_menu` 表定义 |
| 角色菜单关联 | `V3__add_menu_tables.sql` | 第 28-37 行 | `sys_role_menu` 表定义 |
| 平台管理员全菜单 | `V3__add_menu_tables.sql` | 第 97-98 行 | `SELECT 1, id FROM sys_menu` |
| 物业管理员菜单 | `V3__add_menu_tables.sql` | 第 101-112 行 | 指定菜单 ID 列表 |
| 运维人员菜单 | `V3__add_menu_tables.sql` | 第 115-121 行 | 工作台+维修工单+消息 |
| 巡检人员菜单 | `V3__add_menu_tables.sql` | 第 124-130 行 | 工作台+巡检+隐患+消息 |
| 社区经纬度 | `V4__add_community_coordinates.sql` | 第 9-11 行 | `ALTER TABLE community ADD COLUMN lat/lng` |
| 补充 4 个社区 | `V4__add_community_coordinates.sql` | 第 21-25 行 | 翠苑新城等 4 个 |
| 端口状态枚举 | `V1__init_schema.sql` | 第 132 行 | IDLE/RESERVED/CHARGING 等 9 种 |
| 预约状态枚举 | `V1__init_schema.sql` | 第 190 行 | PENDING/CONVERTED 等 5 种 |
| 订单状态枚举 | `V1__init_schema.sql` | 第 214 行 | CHARGING/COMPLETED 等 5 种 |
| 工单状态枚举 | `V1__init_schema.sql` | 第 263 行 | PENDING 到 CLOSED 共 7 种 |
| 隐患状态枚举 | `V1__init_schema.sql` | 第 343 行 | PENDING 到 CLOSED 共 6 种 |
| 巡检任务状态 | `V1__init_schema.sql` | 第 308 行 | PENDING 到 CANCELLED 共 6 种 |
| 统一返回 Result | `Result.java` | 全文 | 泛型返回类 |
| 状态码枚举 | `ResultCode.java` | 全文 | 20+ 个状态码 |
| 全局异常处理 | `GlobalExceptionHandler.java` | 全文 | 6 类异常处理 |
| JWT 过滤器 | `JwtAuthenticationFilter.java` | 全文 | 拦截 `/api/*` |
| 安全配置 | `SecurityConfig.java` | 全文 | 注册 JWT 过滤器 |
| AOP 操作日志 | `OperationLogAspect.java` | 全文 | `@OperationLog` 切面 |
| 权限注解 | `RequiresPermission.java` | 全文 | 方法级权限注解 |
| 权限切面 | `PermissionAspect.java` | 全文 | `@Before` 拦截 |
| 预约冲突检测 | `ChargingServiceImpl.java` | 第 62-79 行 | 用户+端口双重校验 |
| 30 分钟预约有效期 | `ChargingServiceImpl.java` | 第 106 行 | `plusMinutes(30)` |
| 费用计算 | `ChargingServiceImpl.java` | 第 357-363 行 | 1.5 元/小时，最低 0.5 元 |
| 事务控制 | `ChargingServiceImpl.java` | 第 54 行 | `@Transactional` |
| 路由守卫 | `router/index.js` | 第 56-119 行 | `beforeEach` 钩子 |
| 角色路由限制 | `ops.routes.js` | 第 5 行 | `roles: ['PLATFORM_ADMIN', ...]` |
| 大屏无需登录 | `router/index.js` | 第 25 行 | `requiresAuth: false` |
| Token 持久化 | `stores/user.js` | 第 26-31 行 | localStorage 存储 |
| API 响应拦截 | `api/request.js` | 第 26-73 行 | Token 过期自动跳登录 |
| 前端登录表单校验 | `Login.vue` | 第 51-54 行 | 必填校验 |
| 测试文件 | `ChargingServiceTest.java` | — | 15 个测试用例 |
| 测试文件 | `MenuTreeBuilderTest.java` | — | 菜单树构建测试 |
| `@RequiresPermission` 未使用 | 全项目 grep | — | 无 Controller 使用该注解 |
| `hasPermission` 方法缺失 | `LoginUser.java` | 全文 | 仅有字段，无该方法 |
| 无物理外键 | `V1__init_schema.sql` | 第 6 行 | `SET FOREIGN_KEY_CHECKS = 0` |
| 无 Redis/缓存 | `pom.xml` | 全文 | 无 Redis/Cache 依赖 |
