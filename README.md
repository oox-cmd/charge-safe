# ChargeSafe 社区充电安全运维平台

> 《面向社区场景的电动自行车充电停放与安全运维管理系统设计与实现》

[![GitHub](https://img.shields.io/badge/GitHub-wangsf134%2Fcharge--safe-blue?logo=github)](https://github.com/wangsf134/charge-safe)

## 项目简介

ChargeSafe 是一个面向小区、园区、学校宿舍区、写字楼等场景的电动自行车充电停放与安全运维管理系统。系统围绕社区公共充电设施的资源管理、居民预约充电、设备故障报修、维修工单处理、安全巡检、隐患整改、消息通知和统计分析等业务进行设计。

**系统重点**：多角色权限、状态流转、业务闭环、数据库关系、异常流程和可落地实现，不追求真实硬件接入和真实支付。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 · Spring Boot 3.2.5 · MyBatis-Plus 3.5.5 · MySQL 8.x · JWT · Flyway · Caffeine Cache |
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
│               ├── V3__add_menu_tables.sql
│               ├── V4__add_community_coordinates.sql
│               ├── V5__adjust_property_admin_menus.sql
│               └── V6__add_resident_dashboard_permission.sql
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

### 1. 环境要求

- Git
- JDK 17
- Maven 3.8+
- Node.js 20.19+（同时满足管理端 Vite 8 和小程序端 Vite 5）
- npm
- MySQL 8.x；当前开发机器实际使用 MySQL 9.7

本地开发使用 Maven、Node.js、npm 和原生 MySQL，不要求 Docker。

### 2. 克隆项目

```bash
git clone https://github.com/wangsf134/charge-safe.git
cd charge-safe
```

### 3. 准备本地数据库

本地开发统一使用 Windows 原生 MySQL。推荐 MySQL 8.x；当前开发机器实际使用
MySQL 9.7。本阶段按当前本地开发环境配置，不对其他 MySQL 版本作兼容性保证。

以下数据库账号和密码仅限本地开发和课程演示，不得用于生产或云端数据库。应用
必须使用专用账号，不要使用 MySQL root 账号运行 ChargeSafe。

```bash
CREATE DATABASE IF NOT EXISTS charge_safe_dev
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'charge_safe_app'@'localhost'
  IDENTIFIED BY 'cugb666';

GRANT ALL PRIVILEGES ON charge_safe_dev.*
  TO 'charge_safe_app'@'localhost';
```

只创建空数据库和专用账号。不要手动执行 Flyway V1 至 V6，也不要执行根目录
`sql/schema.sql` 或 `sql/data.sql`。后端启动时会由 Flyway 自动校验已执行迁移并
执行 pending migration；存在 pending migration 是正常升级状态，不代表迁移异常。

### 4. 构建和启动后端

Windows 直接运行：

```bash
cd charge-safe-backend
mvn clean test
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

WSL2 NAT 模式下直接运行：

```bash
bash ./scripts/dev-backend-wsl.sh
```

该脚本动态读取 WSL 默认网关作为 Windows 主机地址，不写死个人 IP。Windows
防火墙应仅允许当前 WSL 虚拟子网访问 TCP 3306，MySQL 应为该子网配置
`charge_safe_app` Host 权限，不要向所有公网来源开放。

补充生产包构建：

```bash
cd charge-safe-backend
mvn clean package
```

Flyway 会自动初始化或升级数据库结构和初始数据。启动后可只读检查：

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

后端运行在 `http://127.0.0.1:8080`，API 文档为
`http://127.0.0.1:8080/doc.html`。

### 5. 构建和启动 Web 管理端

```bash
cd charge-safe-admin
npm ci
npm run dev
```

开发服务器使用 `http://127.0.0.1:3000`。管理端请求保持为相对 `/api`，Vite
开发代理将其转发到 `http://127.0.0.1:8080`。如确有需要，可在管理端本地环境
文件中设置可选的 `VITE_API_BASE_URL`；生产地址不得写死为 localhost。

```bash
cd charge-safe-admin
npm run build
```

### 6. 可视化大屏

管理端启动后，具有 `statistics:bigscreen:view` 的管理角色可访问 `http://127.0.0.1:3000/bigscreen` 进入二维管理运维大屏；该路由和专用数据接口均要求登录与权限。居民登录后默认进入 `http://127.0.0.1:3000/resident/dashboard`，页面只展示其所属社区内的真实车棚与端口可用性，不进入管理大屏。教师演示若需要免登录，后续另行设计受限演示模式，不得绕过认证过滤器或权限体系。

### 7. 小程序端

小程序当前 Vue/uni-app 依赖链固定使用 npm 10.9.8。无需卸载或降级电脑中的
全局 npm；即使默认安装的是 npm 11，也通过 `npx` 调用项目指定的 npm 10。
不要使用 `--force` 或 `--legacy-peer-deps`。此限制只适用于小程序，管理端继续
使用其自身已验证的 npm 流程。

PowerShell 或 WSL：

```bash
cd charge-safe-miniapp
npx npm@10.9.8 ci
npx npm@10.9.8 run build:mp-weixin
```

构建产物位于 `charge-safe-miniapp/dist/build/mp-weixin`，可使用微信开发者工具
导入。以上命令已在无 Docker 的 WSL 环境实际验证。

H5 调试使用真实存在的脚本：

```bash
cd charge-safe-miniapp
npx npm@10.9.8 ci
npx npm@10.9.8 run dev:h5
```

浏览器打开开发服务器输出的实际地址。其他平台脚本以
`charge-safe-miniapp/package.json` 中现有的 `dev:*` 和 `build:*` 为准。

## 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 平台管理员 | admin | 123456 | 全部权限，含系统管理 |
| 物业管理员 | property01 | 123456 | 业务管理、统计、大屏 |
| 运维人员 | maintainer01 | 123456 | 工作台、维修工单 |
| 巡检人员 | inspector01 | 123456 | 巡检任务、安全隐患 |
| 居民用户 | resident01 | 123456 | 登录后进入居民充电可用性页；小程序端也使用该角色 |

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户与角色管理 | RBAC 权限模型，5 种角色，JWT 认证 + 前端路由控制 + 后端 `@RequiresPermission` 注解，用户/角色/菜单 CRUD |
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

共实现 **100 个 RESTful 接口**：

| 模块 | 路径前缀 | 接口数 |
|------|---------|-------|
| 认证 | `/api/auth` | 4 |
| 用户管理 | `/api/users` | 7 |
| 角色管理 | `/api/roles` | 6 |
| 菜单管理 | `/api/menus` | 5 |
| 社区 | `/api/communities` | 6 |
| 车棚 | `/api/parking-areas` | 6 |
| 设备 | `/api/devices` | 6 |
| 端口 | `/api/ports` | 9 |
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

项目使用 Flyway 管理数据库迁移。`application.yml` 显式配置
`classpath:db/migration`，对应的唯一正式迁移目录为：

```
charge-safe-backend/src/main/resources/db/migration/
├── V1__init_schema.sql        # 21 张业务表
├── V2__init_data.sql           # 初始数据（角色、菜单、测试数据）
├── V3__add_menu_tables.sql     # RBAC 菜单权限表 + 菜单数据（+2 张表）
├── V4__add_community_coordinates.sql  # 社区经纬度 + 补充演示数据
├── V5__adjust_property_admin_menus.sql # 物业管理员菜单权限补充
└── V6__add_resident_dashboard_permission.sql # 居民页面权限及角色映射
```

### 规则

1. 所有表结构变更必须通过新的迁移文件提交到 Git
2. 不允许手动改库后只口头同步
3. 不允许业务代码启动时自动 DROP 表
4. 本地库、云端开发库、云端正式库的表结构由同一套迁移文件生成
5. 不要求团队成员手动逐个执行 V1 至 V6
6. 不得同时执行 `sql/schema.sql`、`sql/data.sql` 和 Flyway
7. 根目录 `sql/schema.sql`、`sql/data.sql` 不符合 Flyway 命名规则，也未被当前
   MySQL Profile 或启动脚本自动引用；它们仅保留作参考，默认流程不得执行
8. 已发布的 V1 至 V6 不得移动、重命名、修改内容或版本号
9. Flyway 在后端启动时自动校验历史并执行 pending migration；pending 不等于异常
10. 不得手工修改 `flyway_schema_history`，也不得随意执行 repair、baseline 或 clean

H2 Profile 是独立机制：`application-h2.yml` 会关闭 Flyway，并使用
`classpath:db/h2/schema.sql` 和 `classpath:db/h2/data.sql`。Windows MySQL 和
WSL 本地开发均不使用该 H2 初始化流程。

### 团队复现流程

1. 安装并启动 MySQL；推荐 8.x，当前开发机器实际使用 9.7
2. 使用本文记录的本地开发专用账号和密码
3. 创建空数据库 `charge_safe_dev`
4. 确保 `charge_safe_app` 对该数据库具有所需权限
5. 启动后端，不手动执行根目录 `sql/` 下的脚本
6. 等待 Flyway 从 `charge-safe-backend/src/main/resources/db/migration/` 自动执行所有 pending migration
7. 检查 `flyway_schema_history`，确认所有迁移成功

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

### v0.8.1 (2026-07-04) — Bug 修复 + 清理

**修复**
- 修复访问 `localhost:8080` 根路径返回 500 的问题，新增 `NoResourceFoundException` 处理器，返回友好提示
- 修复通知列表接口 `NullPointerException`（`isRead` 为 null 时拆箱报错）
- 修复车棚列表 `/api/parking-areas/list` 缺少 `communityId` 参数返回 500 的问题
- 修复设备列表 `/api/devices/list` 缺少 `parkingAreaId` 参数返回 500 的问题
- 修复小程序登录后无法跳转首页的问题（`uni.switchTab` 改为 `uni.reLaunch`）
- 修复 `operation_log` 表缺少 `module`、`url`、`duration` 列的问题
- 修复 `ChargeSafeApplication` MapperScan 未包含 `common` 包导致 `OperationLogMapper` 注入失败

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

---

## 开发说明

> **注意：本节内容需要随开发过程持续更新。**

### 当前开发阶段

**项目处于中期开发阶段：后端 P0 业务基础基本完成，跨端真实闭环、前端体验和三级三维大屏仍在规划与开发中；当前不是最终交付状态。**

根据审查，当前前端整体 AI 味过重，需要进行 UI/UX 整改。详见：
- `docs/audit/overall-function-and-experience-audit.md` — 功能与体验总审查
- `docs/audit/frontend-ui-ai-flavor-audit.md` — AI 味问题清单
- `docs/audit/new-frontend-design-guidelines.md` — 新设计规范
- `docs/tasks/frontend-polish-and-fix-execution-plan.md` — 整改执行计划
- `docs/audit/midterm-development-baseline-audit.md` — 当前源码事实基线
- `docs/research/three-level-dashboard-open-source-research.md` — 三级大屏技术与免费资产调研
- `docs/tasks/next-stage-development-roadmap.md` — 下一阶段开发顺序

### Skills 使用

前端和大屏工作优先使用当前开发者用户级 Codex Skills；项目仓库不重复保存这些工具：

| Skill | 用途 | 用户级路径 |
|-------|------|------|
| ui-ux-pro-max | UI/UX 信息层级、可用性和设计系统 | `~/.codex/skills/ui-ux-pro-max/` |
| frontend-design | 避免模板化和无意义装饰 | `~/.codex/skills/frontend-design/` |
| systematic-debugging | 源码、接口、数据库和文档不一致调查 | `~/.codex/skills/systematic-debugging/` |
| verification-before-completion | 完成前执行新鲜验证 | `~/.codex/skills/verification-before-completion/` |

Skills 按任务触发，不要求团队成员把全局 Skills 复制进仓库，也不存在必须加载的 `skills/superpowers/SKILL.md`。

### 下一步开发重点

1. **阶段 1**：认证、权限和小程序核心业务闭环补全
2. **阶段 2**：建立真实站点/端口统计口径和只读接口
3. **阶段 3**：三级大屏 MVP（地球 → 小区 → 单站端口）
4. **阶段 4**：管理端 UI/UX 系统整改
5. **阶段 5**：三级大屏推荐演示版本
6. **阶段 6**：小程序体验重构和真机验收
7. **阶段 7**：测试、性能、文档和演示准备

详见 `docs/tasks/next-stage-development-roadmap.md`。三级大屏是下一阶段展示重点，模型优先采用免费、合法、许可证清晰的 CC0/开源资源；未经许可证确认不得下载后提交到仓库。当前尚未开始最终交付文档定稿，相关文档必须随代码和验证结果持续更新。

### 三级大屏已确认技术边界

- MVP 使用原生 Three.js + `three-globe`；Vue 负责页面、状态和生命周期，Three.js 封装在独立组件、模块和 composable 中。
- 当前不采用 Globe.GL、TresJS、MapLibre 或 Cesium，也不并行维护两套 Three.js 实现；只有真实 GIS 需求成立后才重新评估 MapLibre。
- 管理运维大屏位于 `/bigscreen`，使用现有认证与 `statistics:bigscreen:view`；居民使用独立 `/resident/dashboard` 与 `resident:dashboard:view`，两者不得混用数据边界。
- 小区采用非测绘级低多边形示意场景，程序化生成道路、建筑、地面和站点布局；场景坐标保存在前端配置中，不伪装成经纬度。
- 站点特写不以购买或找到完整模型为前置条件，优先程序化搭建棚体、柜体、充电位、端口和状态锚点，再组合少量免费模块化资产。
- MVP 只使用许可证清晰的 CC0 资产；CC BY 仅作后备，必须逐项确认作者、来源、许可证和署名方式。许可证未知、仅限个人用途、禁止再分发、All Rights Reserved、盗提、带水印、CC BY-NC 和 CC BY-ND 资产不得采用。
- 后端提供真实业务 ID 与状态，三维坐标暂存前端场景配置；当前不新增数据库字段或 Flyway 迁移。模型节点名不得作为唯一业务 ID。
- 端口实际有 9 种后端状态，前端先建立原始状态到大屏分组、颜色、图标和告警语义的完整映射，不修改后端状态迎合视觉规范。
- MVP 使用可配置的 10 至 15 秒轮询，页面隐藏时暂停轮询和渲染；暂不引入 WebSocket 或 SSE。

### 设计规范摘要

**管理端风格：** 成熟、干净、稳定的运维后台，以表格和状态为核心

**大屏风格：** 克制的深色运维风，围绕社区充电安全，不要科技蓝模板

**小程序风格：** 轻量、清楚、易用，突出"找车棚→预约→充电"主路径

**端口状态颜色（必须遵守）：**
| 状态 | 颜色 | 色值 |
|------|------|------|
| IDLE (空闲) | 绿色 | #22c55e |
| RESERVED (已预约) | 蓝色 | #3b82f6 |
| CHARGING (充电中) | 橙色 | #f59e0b |
| COMPLETED_WAITING_PICKUP (待取车) | 紫色 | #8b5cf6 |
| OVERTIME_OCCUPIED (超时占位) | 深橙色 | #f97316 |
| FAULT (故障) | 红色 | #ef4444 |
| MAINTENANCE (维修中) | 灰色 | #94a3b8 |
| DISABLED (停用) | 深灰色 | #64748b |
| OFFLINE (离线) | 暗灰色 | #475569 |

详见 `docs/audit/new-frontend-design-guidelines.md`

### 文档索引

| 文档 | 路径 | 用途 |
|------|------|------|
| 功能审查 | `docs/audit/overall-function-and-experience-audit.md` | 当前问题总览 |
| AI 味审查 | `docs/audit/frontend-ui-ai-flavor-audit.md` | 前端问题清单 |
| 设计规范 | `docs/audit/new-frontend-design-guidelines.md` | 改代码必须遵守 |
| 执行计划 | `docs/tasks/frontend-polish-and-fix-execution-plan.md` | 下一步任务书 |
| 数据库设计 | `docs/design/database-design.md` | 数据表结构 |
| 前端页面设计 | `docs/design/frontend-page-design.md` | 页面设计说明 |
| 大屏设计 | `docs/design/dashboard-visual-design.md` | 大屏可视化设计 |
| 小程序设计 | `docs/design/miniapp-page-design.md` | 小程序页面设计 |
| 中期事实基线 | `docs/audit/midterm-development-baseline-audit.md` | 当前完成度与风险 |
| 源码证据索引 | `docs/audit/source-of-truth-evidence-index.md` | 接口、页面、数据库和构建证据 |
| 三级大屏调研 | `docs/research/three-level-dashboard-open-source-research.md` | 技术、项目、资产与许可证候选 |
| 大屏数据建模 | `docs/research/dashboard-data-and-scene-modeling.md` | 状态机、DTO 和场景映射 |
| 大屏实施计划 | `docs/tasks/three-level-dashboard-implementation-plan.md` | MVP、演示版、增强版 |
| 下一阶段路线 | `docs/tasks/next-stage-development-roadmap.md` | 中期后续开发顺序 |
| 过程文档计划 | `docs/process/development-process-documentation-plan.md` | 日志、ADR、资产与验收模板 |
