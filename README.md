# ChargeSafe 社区充电安全运维平台

> 《面向社区场景的电动自行车充电停放与安全运维管理系统设计与实现》

[![GitHub](https://img.shields.io/badge/GitHub-wangsf134%2Fcharge--safe-blue?logo=github)](https://github.com/wangsf134/charge-safe)

## 项目简介

ChargeSafe 是一个面向小区、园区、学校宿舍区、写字楼等场景的电动自行车充电停放与安全运维管理系统。系统围绕社区公共充电设施的资源管理、居民预约充电、设备故障报修、维修工单处理、安全巡检、隐患整改、消息通知和统计分析等业务进行设计。

**系统重点**：多角色权限、状态流转、业务闭环、数据库关系、异常流程和可落地实现，不追求真实硬件接入和真实支付。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 · Spring Boot 3.2.5 · MyBatis-Plus 3.5.5 · MySQL 8.x · JWT |
| Web 管理端 | Vue 3 · Vite · Element Plus · Pinia · Axios · ECharts |
| 小程序端 | uni-app · Vue 3 |
| 接口文档 | Knife4j (Swagger) |

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户与角色管理 | RBAC 权限模型，5 种角色，菜单权限 + 数据权限 |
| 社区资源管理 | 社区 → 车棚 → 设备 → 端口 层级管理 |
| 预约充电 | 预约端口、扫码开始充电、结束充电、生成订单 |
| 报修工单 | 居民报修、工单派发、维修处理、验收关闭 |
| 巡检隐患 | 巡检任务、隐患上报、整改复查 |
| 消息通知 | 站内消息通知、已读未读 |
| 统计分析 | 仪表盘数据、ECharts 图表 |
| 设备模拟 | 模拟端口状态变化，触发告警和工单 |

## 项目结构

```
charge-safe/
├── charge-safe-backend/          # Spring Boot 后端
│   └── src/main/java/com/chargesafe/
│       ├── common/               # Result, PageResult, 异常处理
│       ├── config/               # MyBatis, CORS, Security
│       ├── security/             # JWT, UserContext
│       └── modules/              # 业务模块 (auth/community/device/charging/repair/inspection/notification/statistics/simulator)
├── charge-safe-admin/            # Vue3 Web 管理端
│   └── src/
│       ├── api/                  # API 请求封装
│       ├── layouts/              # 页面布局
│       ├── router/               # 路由配置
│       ├── stores/               # Pinia 状态管理
│       └── views/                # 页面组件
├── charge-safe-miniapp/          # uni-app 小程序端
│   └── src/pages/                # 页面
├── sql/                          # 数据库脚本
│   ├── schema.sql                # 建表语句 (19 张表)
│   └── data.sql                  # 初始数据
├── docs/
│   └── summary.md                # 项目详细总结
├── README.md
└── .gitignore
```

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/wangsf134/charge-safe.git
cd charge-safe
```

### 2. 初始化数据库

确保 MySQL 8.x 已启动：

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS charge_safe DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 导入表结构和初始数据
mysql -u root -p charge_safe < sql/schema.sql
mysql -u root -p charge_safe < sql/data.sql
```

### 3. 启动后端

```bash
cd charge-safe-backend

# 如果 MySQL 用户名密码不是 root/root，先修改 application.yml
# vim src/main/resources/application.yml

mvn spring-boot:run
```

后端运行在 `http://localhost:8080`，API 文档：`http://localhost:8080/doc.html`

### 4. 启动 Web 管理端

```bash
cd charge-safe-admin
npm install
npm run dev
```

浏览器打开 `http://localhost:3000`

### 5. 小程序端 (可选)

```bash
cd charge-safe-miniapp
npm install
npm run dev:mp-weixin
```

使用微信开发者工具打开 `dist/dev/mp-weixin` 目录。

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 平台管理员 | admin | 123456 |
| 物业管理员 | property01 | 123456 |
| 运维人员 | maintainer01 | 123456 |
| 巡检人员 | inspector01 | 123456 |
| 居民用户 | resident01 | 123456 |

## API 接口

共实现 **59 个 RESTful 接口**：

| 模块 | 路径前缀 | 接口数 |
|------|---------|-------|
| 认证 | `/api/auth` | 2 |
| 社区 | `/api/communities` | 5 |
| 车棚 | `/api/parking-areas` | 5 |
| 设备 | `/api/devices` | 5 |
| 端口 | `/api/ports` | 8 |
| 预约 | `/api/reservations` | 4 |
| 充电 | `/api/charging` | 2 |
| 订单 | `/api/orders` | 3 |
| 报修 | `/api/repair-reports` | 3 |
| 工单 | `/api/repair-tickets` | 8 |
| 巡检 | `/api/inspection-tasks` | 7 |
| 隐患 | `/api/hazards` | 6 |
| 通知 | `/api/notifications` | 4 |
| 统计 | `/api/statistics` | 1 |
| 模拟 | `/api/simulator` | 4 |

## 核心状态机

### 充电端口
```
IDLE(空闲) → RESERVED(已预约) → CHARGING(充电中) → IDLE(空闲)
IDLE(空闲) → FAULT(故障) → MAINTENANCE(维修中) → IDLE(空闲)
```

### 维修工单
```
PENDING → DISPATCHED → PROCESSING → WAITING_ACCEPTANCE → FINISHED → CLOSED
```

### 安全隐患
```
PENDING → RECTIFYING → WAITING_REVIEW → REVIEW_PASSED → CLOSED
```

## 项目边界

| 边界项 | 说明 |
|--------|------|
| 硬件接入 | 不接入真实充电硬件，通过模拟层完成业务闭环 |
| 支付功能 | 不做真实支付，只记录模拟费用 |
| 地图定位 | 不强依赖地图定位 |
| 身份验证 | 不采集身份证等高敏信息 |
| 物联网 | 不做复杂物联网长连接 |
| 电量采集 | 不做真实电量采集，只模拟充电时长和费用 |

## 数据库设计

共 **19 张核心表**，采用 InnoDB + utf8mb4，支持逻辑删除和多社区数据隔离：

| 线路 | 表 |
|------|-----|
| 用户权限 | sys_user, sys_role, sys_user_role, operation_log |
| 社区资源 | community, parking_area, charging_device, charging_port |
| 预约充电 | charging_reservation, charging_order |
| 报修工单 | repair_report, repair_ticket, repair_ticket_log |
| 巡检隐患 | inspection_task, inspection_record, safety_hazard |
| 消息统计 | notification, alarm_record, charging_fee_rule |

详细设计见 [docs/summary.md](docs/summary.md)

## 课程设计报告素材

本项目可直接用于软件工程课程设计报告，相关素材见 `docs/summary.md`，包含：
- 项目背景与目标
- 技术架构
- 功能模块说明
- 数据库设计
- 接口设计
- 状态机设计
- 部署方案
