# ChargeSafe 社区充电安全运维平台

> 《面向社区场景的电动自行车充电停放与安全运维管理系统设计与实现》

[![GitHub](https://img.shields.io/badge/GitHub-wangsf134%2Fcharge--safe-blue?logo=github)](https://github.com/wangsf134/charge-safe)

## 项目简介

ChargeSafe 是一个面向小区、园区、学校宿舍区、写字楼等场景的电动自行车充电停放与安全运维管理系统。系统围绕社区公共充电设施的资源管理、居民预约充电、设备故障报修、维修工单处理、安全巡检、隐患整改、消息通知和统计分析等业务进行设计。

**系统重点**：多角色权限、状态流转、业务闭环、数据库关系、异常流程和可落地实现，不追求真实硬件接入和真实支付。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 · Spring Boot 3.2.5 · MyBatis-Plus 3.5.5 · MySQL 8.x · JWT · Flyway |
| Web 管理端 | Vue 3 · Vite · Element Plus · Pinia · Axios · ECharts |
| 小程序端 | uni-app · Vue 3 |
| 接口文档 | Knife4j (Swagger) |

## 项目结构

```
charge-safe/
├── charge-safe-backend/              # Spring Boot 后端
│   └── src/main/
│       ├── java/com/chargesafe/
│       │   ├── common/               # Result, PageResult, 异常处理, 日志注解
│       │   ├── config/               # MyBatis, CORS, Security
│       │   ├── security/             # JWT, UserContext
│       │   └── modules/              # 业务模块
│       │       ├── auth/             # 认证
│       │       ├── user/             # 用户/角色/菜单管理
│       │       ├── community/        # 社区/车棚
│       │       ├── device/           # 设备/端口
│       │       ├── charging/         # 预约/充电/订单
│       │       ├── repair/           # 报修/工单
│       │       ├── inspection/       # 巡检/隐患
│       │       ├── notification/     # 消息通知
│       │       ├── statistics/       # 统计分析
│       │       ├── simulator/        # 设备模拟
│       │       └── log/              # 操作日志
│       └── resources/
│           ├── application.yml       # 通用配置
│           ├── application-local.yml # 本地开发库
│           ├── application-dev.yml   # 云端共享开发库
│           ├── application-prod.yml  # 云端正式演示库
│           └── db/migration/         # Flyway 迁移文件
│               ├── V1__init_schema.sql
│               ├── V2__init_data.sql
│               └── V3__add_menu_tables.sql
├── charge-safe-admin/                # Vue3 Web 管理端
│   └── src/
│       ├── api/                      # API 接口封装
│       ├── views/                    # 页面组件
│       │   ├── auth/                 # 登录
│       │   ├── dashboard/            # 工作台
│       │   ├── community/            # 社区/车棚管理
│       │   ├── device/               # 设备/端口管理
│       │   ├── charging/             # 预约/订单管理
│       │   ├── repair/               # 报修/工单管理
│       │   ├── inspection/           # 巡检/隐患管理
│       │   ├── notification/         # 消息通知
│       │   ├── log/                  # 操作日志
│       │   ├── system/               # 系统管理(用户/角色/菜单)
│       │   ├── statistics/           # 统计分析
│       │   └── bigscreen/            # 2D 可视化大屏
│       ├── router/                   # 路由配置
│       ├── layouts/                  # 布局组件
│       └── stores/                   # Pinia 状态管理
├── charge-safe-miniapp/              # uni-app 小程序端
│   └── src/
│       ├── pages/                    # 页面
│       │   ├── login.vue             # 登录
│       │   ├── index/                # 首页
│       │   ├── parking/              # 车棚列表/详情
│       │   ├── reservation/          # 预约确认/记录
│       │   ├── charging/             # 充电中
│       │   ├── order/                # 订单列表/详情
│       │   ├── repair/               # 报修提交/记录
│       │   ├── notification/         # 消息列表
│       │   └── profile/              # 个人中心
│       └── api/                      # 请求封装
├── sql/                              # 原始数据库脚本 (仅供参考)
├── docs/                             # 项目文档
├── README.md
└── .gitignore
```

## 多环境配置

本项目支持三套数据库环境，通过 Spring Profile 切换：

| 环境 | Profile | 用途 | 数据库 |
|------|---------|------|--------|
| 本地开发库 | `local` | 个人开发调试 | `127.0.0.1:3306/charge_safe_dev` |
| 云端共享开发库 | `dev` | 多人联调测试 | `127.0.0.1:13306/charge_safe_dev` (SSH 隧道) |
| 云端正式演示库 | `prod` | 正式部署演示 | `127.0.0.1:3306/charge_safe` (服务器本机) |

### 数据库连接信息

| 环境 | Host | Port | Database | Username | Password |
|------|------|------|----------|----------|----------|
| 本地开发库 | 127.0.0.1 | 3306 | charge_safe_dev | charge_safe_app | cugb666 |
| 云端开发库 (隧道) | 127.0.0.1 | 13306 | charge_safe_dev | charge_safe_app | cugb666 |
| 云端正式库 | 127.0.0.1 | 3306 | charge_safe | charge_safe_app | cugb666 |

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/wangsf134/charge-safe.git
cd charge-safe
```

### 2. 创建本地数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS charge_safe_dev DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql -u root -p -e "CREATE USER IF NOT EXISTS 'charge_safe_app'@'%' IDENTIFIED BY 'cugb666';"
mysql -u root -p -e "GRANT ALL PRIVILEGES ON charge_safe_dev.* TO 'charge_safe_app'@'%';"
mysql -u root -p -e "FLUSH PRIVILEGES;"
```

### 3. 启动后端 (本地开发)

```bash
cd charge-safe-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Flyway 会自动初始化数据库表结构和初始数据（V1 + V2 + V3）。

后端运行在 `http://localhost:8080`，API 文档：`http://localhost:8080/doc.html`

### 4. 启动 Web 管理端

```bash
cd charge-safe-admin
npm install
npm run dev
```

浏览器打开 `http://localhost:3000`

### 5. 可视化大屏

管理端启动后，访问 `http://localhost:3000/bigscreen` 进入全屏可视化大屏。

### 6. 小程序端

**方式一：H5 浏览器预览 (推荐调试)**

```bash
cd charge-safe-miniapp
npm install
npm run dev:h5
```

浏览器打开 `http://localhost:5173`

**方式二：微信小程序**

```bash
cd charge-safe-miniapp
npm install
npm run build:mp-weixin
```

用微信开发者工具导入 `charge-safe-miniapp/dist/build/mp-weixin` 目录。

## 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 平台管理员 | admin | 123456 | 全部权限，含系统管理 |
| 物业管理员 | property01 | 123456 | 业务管理、统计、大屏 |
| 运维人员 | maintainer01 | 123456 | 工作台、维修工单 |
| 巡检人员 | inspector01 | 123456 | 巡检任务、安全隐患 |
| 居民用户 | resident01 | 123456 | 小程序端使用 |

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户与角色管理 | RBAC 权限模型，5 种角色，菜单权限 + 数据权限，用户/角色/菜单 CRUD |
| 社区资源管理 | 社区 → 车棚 → 设备 → 端口 层级管理 |
| 预约充电 | 预约端口、冲突检测（端口/用户维度）、扫码开始充电、结束充电、生成订单 |
| 报修工单 | 居民报修、工单派发、维修处理、验收关闭 |
| 巡检隐患 | 巡检任务、隐患上报、整改复查 |
| 消息通知 | 站内消息通知、已读未读 |
| 统计分析 | 仪表盘数据、端口状态分布、订单趋势、工单/隐患汇总、车棚利用率 |
| 可视化大屏 | 2D 态势大屏，端口状态矩阵、告警滚动列表、趋势图表 |
| 系统管理 | 用户管理、角色管理、菜单管理（平台管理员专属） |
| 设备模拟 | 模拟端口状态变化，触发告警和工单 |
| 操作日志 | AOP 自动记录关键操作日志 |

## API 接口

共实现 **95+ 个 RESTful 接口**：

| 模块 | 路径前缀 | 接口数 |
|------|---------|-------|
| 认证 | `/api/auth` | 3 |
| 用户管理 | `/api/users` | 7 |
| 角色管理 | `/api/roles` | 6 |
| 菜单管理 | `/api/menus` | 5 |
| 社区 | `/api/communities` | 5 |
| 车棚 | `/api/parking-areas` | 5 |
| 设备 | `/api/devices` | 5 |
| 端口 | `/api/ports` | 8 |
| 预约 | `/api/reservations` | 6 |
| 充电 | `/api/charging` | 3 |
| 订单 | `/api/orders` | 3 |
| 报修 | `/api/repair-reports` | 3 |
| 工单 | `/api/repair-tickets` | 8 |
| 巡检 | `/api/inspection-tasks` | 7 |
| 隐患 | `/api/hazards` | 6 |
| 通知 | `/api/notifications` | 4 |
| 统计 | `/api/statistics` | 6 |
| 模拟 | `/api/simulator` | 4 |
| 日志 | `/api/logs` | 2 |

## 数据库迁移

项目使用 Flyway 管理数据库迁移，迁移文件位于：

```
charge-safe-backend/src/main/resources/db/migration/
├── V1__init_schema.sql        # 19 张业务表
├── V2__init_data.sql           # 初始数据（角色、菜单、测试数据）
└── V3__add_menu_tables.sql     # RBAC 菜单权限表 + 菜单数据
```

### 规则

1. 所有表结构变更必须通过新的迁移文件提交到 Git
2. 不允许手动改库后只口头同步
3. 不允许业务代码启动时自动 DROP 表
4. 本地库、云端开发库、云端正式库的表结构由同一套迁移文件生成

## 特别注意

1. 本地 `charge_safe_dev` 和云端 `charge_safe_dev` 名字相同，但不是同一个数据库
2. 云端 `charge_safe_dev` 是共享开发库，不要做破坏性测试
3. 正式库 `charge_safe` 不允许随意清空
4. 所有数据库结构修改必须提交 Flyway 迁移文件
5. 前端开发默认请求本地后端：`http://localhost:8080/api`
6. 服务器部署时，前端通过 Nginx 访问 `/api`，反向代理到 `http://127.0.0.1:8081/api`

## 项目边界

| 边界项 | 说明 |
|--------|------|
| 硬件接入 | 不接入真实充电硬件，通过模拟层完成业务闭环 |
| 支付功能 | 不做真实支付，只记录模拟费用 |
| 地图定位 | 不强依赖地图定位 |
| 身份验证 | 不采集身份证等高敏信息 |
| 物联网 | 不做复杂物联网长连接 |
| 电量采集 | 不做真实电量采集，只模拟充电时长和费用 |

## 多人联调

### 1. 打开 SSH 隧道

```powershell
ssh -L 13306:127.0.0.1:3306 root@43.108.40.12
```

### 2. 使用 dev profile 启动

```bash
cd charge-safe-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 服务器部署

### 环境变量配置

服务器环境变量文件位置：`/etc/charge-safe/charge-safe.env`

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8081
CHARGE_SAFE_DB_HOST=127.0.0.1
CHARGE_SAFE_DB_PORT=3306
CHARGE_SAFE_DB_NAME=charge_safe
CHARGE_SAFE_DB_USERNAME=charge_safe_app
CHARGE_SAFE_DB_PASSWORD=cugb666
```

### 启动命令

```bash
java -jar /opt/charge-safe/charge-safe-backend.jar
```

### Nginx 配置

前端通过 Nginx 访问 `/api`，反向代理到 `http://127.0.0.1:8081/api`

域名：`http://charge.sfwang.xyz`

## 更新日志

### v0.8.0 (2026-07-04) — 小程序核心原型

**小程序端**
- 新增车棚详情页：端口列表、状态颜色、预约入口
- 新增预约确认页：端口信息、确认预约
- 新增预约记录页：预约列表、取消预约
- 新增充电中页面：实时计时、当前费用、结束充电
- 新增我的订单页：状态筛选、订单列表
- 新增订单详情页：订单信息、费用展示
- 新增报修提交页：设备选择、故障类型、描述
- 新增报修记录页：工单列表、状态标签
- 新增消息列表页：通知展示、已读标记
- 新增个人中心页：用户信息、退出登录
- 新增 API 请求封装（request.js）
- 更新登录页：对接后端 API，失败时降级为模拟登录
- 更新 pages.json：配置全部页面路由

### v0.7.0 (2026-07-04) — 2D 可视化大屏

**管理端**
- 新增 2D 可视化大屏页面（`/bigscreen`）
  - 顶部标题区：系统名称、当前时间、社区、角色
  - 左侧：端口状态环形图、车棚利用率柱状图、核心指标卡片
  - 中间：端口状态矩阵（社区 → 车棚 → 设备 → 端口）
  - 右侧：实时告警、待处理工单、待整改隐患滚动列表
  - 底部：订单趋势折线图
  - 深色背景、科技风、适配 1920×1080
- 路由新增 `/bigscreen`（全屏，不使用主布局）

### v0.6.0 (2026-07-04) — 管理端系统管理 + 统计分析

**管理端**
- 新增系统管理页面
  - 用户管理：查询、新增、编辑、删除、启停、重置密码、分配角色
  - 角色管理：查询、新增、编辑、删除、分配菜单（el-tree）
  - 菜单管理：树形表格、目录/菜单/按钮三类、新增子菜单
- 新增统计分析页面
  - 顶部 8 个统计卡片
  - 端口状态环形图
  - 订单趋势折线图（7 天）
  - 车棚利用率柱状图
  - 工单状态柱状图
  - 隐患状态饼图
- 新增 `system.js` API 封装
- 更新 `statistics.js`：新增 5 个统计接口
- 路由新增 `/statistics`、`/system/user`、`/system/role`、`/system/menu`
- 侧边栏新增菜单项

### v0.5.0 (2026-07-04) — 统计模块增强

**后端**
- 新增统计接口：
  - `GET /api/statistics/port-status` — 端口状态分布
  - `GET /api/statistics/order-trend` — 订单趋势（近 7 天）
  - `GET /api/statistics/repair-summary` — 工单状态汇总
  - `GET /api/statistics/hazard-summary` — 隐患状态汇总
  - `GET /api/statistics/parking-area-usage` — 车棚使用率
- DashboardVO 扩展：新增 reservedPorts、maintenancePorts、disabledPorts、offlinePorts、todayCompletedOrders、totalCommunities、totalParkingAreas、totalDevices
- 新增 5 个 VO 类：PortStatusStatisticsVO、OrderTrendVO、RepairSummaryVO、HazardSummaryVO、ParkingAreaUsageVO

### v0.4.0 (2026-07-04) — 预约冲突检测增强

**后端**
- 预约冲突检测增强
  - 同一端口不能同时存在有效预约
  - 同一用户不能同时存在多个有效预约
- 新增接口：
  - `POST /api/reservations/{id}/expire` — 过期取消预约
  - `POST /api/reservations/{id}/fault-cancel` — 设备故障取消预约
  - `POST /api/charging/start-direct` — 无预约直接开始充电
- 新增 `ChargingServiceTest` 单元测试（15 个测试用例）
  - 重复预约同一端口失败
  - 同一用户重复预约失败
  - 预约成功端口变 RESERVED
  - 取消预约端口恢复 IDLE
  - 过期取消端口恢复 IDLE
  - 故障取消端口变 FAULT
  - 开始充电端口变 CHARGING
  - 结束充电端口恢复 IDLE

### v0.3.0 (2026-07-04) — RBAC 权限体系

**数据库**
- 新增 V3 迁移文件：`V3__add_menu_tables.sql`
  - 新增 `sys_menu` 表（菜单管理）
  - 新增 `sys_role_menu` 表（角色菜单关联）
  - 初始化菜单数据（后台菜单、按钮权限）
  - 初始化角色菜单绑定（5 种角色各分配不同菜单）

**后端**
- 新增用户管理接口（7 个）
  - `GET /api/users` — 分页查询
  - `POST /api/users` — 新增用户
  - `PUT /api/users/{id}` — 编辑用户
  - `DELETE /api/users/{id}` — 删除用户
  - `PUT /api/users/{id}/status` — 启停用户
  - `PUT /api/users/{id}/roles` — 分配角色
  - `POST /api/users/{id}/reset-password` — 重置密码
- 新增角色管理接口（6 个）
  - `GET /api/roles` — 分页查询
  - `GET /api/roles/list` — 角色列表
  - `POST /api/roles` — 新增角色
  - `PUT /api/roles/{id}` — 编辑角色
  - `DELETE /api/roles/{id}` — 删除角色
  - `PUT /api/roles/{id}/menus` — 分配菜单
- 新增菜单管理接口（5 个）
  - `GET /api/menus` — 菜单列表
  - `GET /api/menus/tree` — 菜单树
  - `POST /api/menus` — 新增菜单
  - `PUT /api/menus/{id}` — 编辑菜单
  - `DELETE /api/menus/{id}` — 删除菜单
- 新增当前用户菜单接口：`GET /api/auth/menus`
- 新增 MenuTreeBuilderTest 单元测试

### v0.2.0 (2026-07-01) — 日志模块 + 多环境配置

**新增功能**
- 操作日志模块：基于 AOP + `@OperationLog` 注解，自动记录关键业务操作
- 新增日志查询接口 `GET /api/logs`，支持按用户名、模块筛选
- 新增前端操作日志页面，仅平台管理员可见

**架构优化**
- 支持三套数据库环境配置（local / dev / prod），通过 Spring Profile 切换
- 集成 Flyway 数据库迁移工具，实现版本化数据库结构管理
- 新增 `V1__init_schema.sql` 和 `V2__init_data.sql` 迁移文件

**配置变更**
- 新增 `application-local.yml`：本地开发库配置
- 新增 `application-dev.yml`：云端共享开发库配置（SSH 隧道）
- 新增 `application-prod.yml`：云端正式演示库配置（环境变量）

**数据库变更**
- `operation_log` 表新增 `module`、`url`、`duration` 字段

### v0.1.0 (2026-07-01) — 初始版本

**核心功能**
- 完成 8 个核心业务模块开发：用户认证、社区资源、预约充电、报修工单、巡检隐患、消息通知、统计分析、设备模拟
- 实现 5 种用户角色：平台管理员、物业管理员、运维人员、巡检人员、居民用户
- 实现 61 个 RESTful API 接口
- 设计 19 张数据库表
- 完成 Vue3 Web 管理端开发，包含 15 个页面
- 完成 uni-app 小程序端基础骨架
- 实现核心状态机：充电端口、维修工单、安全隐患
