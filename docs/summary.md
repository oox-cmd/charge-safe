# ChargeSafe 社区充电安全运维平台 — 项目技术文档

> 《面向社区场景的电动自行车充电停放与安全运维管理系统设计与实现》
> 当前版本：v0.3.0（中期开发状态）
>
> 最后同步：2026-07-18。源码、运行配置、Flyway 迁移和新鲜测试结果优先于本文的历史过程记录；本机 Windows MySQL `local` Profile 已完成 Flyway、核心只读 HTTP 冒烟以及两条状态写入联调，但项目仍处于中期开发，不能表述为最终交付。

> 文档分工：启动与环境见 [`../README.md`](../README.md)，开发约束见 [`development-guide.md`](development-guide.md)，未完成事项见 [`current-backlog.md`](current-backlog.md)，历史材料见 [`archive/`](archive/)。

## 当前状态（2026-07-18）

| 主题 | 当前事实 | 验证边界 |
| --- | --- | --- |
| 数据库迁移 | 正式迁移为 V1–V7；已发布 V1–V5 不可修改，V7 新增部门、设备分类、关联字段和权限数据 | 2026-07-18 本机 MySQL `charge_safe_dev` 已确认 V1–V7 全部成功 |
| 部门与设备分类 | 后端 CRUD 已接入；服务端校验名称/编码/状态，禁止部门循环、删除含子部门/绑定用户的部门，以及删除仍被设备引用的分类 | `ReferenceDataIntegrityServiceTest` 覆盖引用完整性和循环关系 |
| 管理端 | 部门与设备分类页面使用真实 API、精确按钮权限；分类字段统一为 `description` | 管理端生产构建通过 |
| 居民小程序 | 预约、充电、订单、报修和通知接真实 API；Mock 只允许开发环境显式开启，并有醒目标识 | H5/微信小程序构建通过；本机已验证 H5 登录、订单列表和报修完成展示，真机仍待验收 |
| 大屏 | 2D 页保留数据来源声明；3D 页按真实 `communityId`、`stationId`、`deviceId`、`portId` 渲染，合成数据不得伪装为实时数据 | 管理端单测和生产构建通过；本机已验证物业统计/大屏数据接口，浏览器渲染仍待验收 |
| 告警 | `alarm_record` 已有 Entity/Mapper/Service，模拟器故障会写入明确标识的模拟告警；统计大屏读取该表 | 真实硬件告警采集尚不在项目范围 |
| 构建 | 后端 JDK 21 `mvn clean package`（89/89 测试）、管理端/小程序构建均已通过 | 本机已完成 MySQL/Flyway、登录及七类只读接口冒烟；预约充电与报修验收闭环已做真实 HTTP 联调，仍需补浏览器端端到端与真实硬件/支付链路 |

## 一、技术架构

### 1.1 技术选型

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 | 主框架 |
| ORM | MyBatis-Plus | 3.5.5 | 数据库操作 |
| 数据库 | MySQL | 8.x | 数据存储 |
| 数据库迁移 | Flyway | - | 版本化数据库变更管理 |
| 认证 | JWT | 0.12.5 | Token 认证 |
| AOP | Spring AOP | - | 操作日志切面 |
| 前端框架 | Vue | 3.x | 管理端 |
| UI 组件库 | Element Plus | - | 管理端界面 |
| 图表 | ECharts | - | 数据可视化 |
| 构建工具 | Vite | - | 前端构建 |
| 小程序 | uni-app | - | 居民端 |

### 1.2 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      前端展示层                              │
├─────────────────────┬──────────────────────┬────────────────┤
│   Web 管理端        │   小程序居民端        │   API 文档     │
│   Vue3 + Element    │   uni-app + Vue3     │   Knife4j      │
│   Port: 3000        │   微信开发者工具      │   /doc.html    │
└─────────┬───────────┴──────────┬───────────┴───────┬────────┘
          │                      │                   │
          └──────────────────────┼───────────────────┘
                                 │
┌────────────────────────────────┴────────────────────────────┐
│                      后端服务层                              │
├─────────────────────────────────────────────────────────────┤
│   Spring Boot 3.2.5 + JWT + MyBatis-Plus + Flyway          │
│                                                             │
│   ┌──────────┬──────────┬──────────┬──────────┐            │
│   │  认证    │  社区    │  充电    │  报修    │            │
│   │  模块    │  资源    │  预约    │  工单    │            │
│   ├──────────┼──────────┼──────────┼──────────┤            │
│   │  巡检    │  消息    │  统计    │  设备    │            │
│   │  隐患    │  通知    │  分析    │  模拟    │            │
│   ├──────────┼──────────┼──────────┼──────────┤            │
│   │  操作日志 (AOP)                           │            │
│   └──────────┴──────────┴──────────┴──────────┘            │
│                                                             │
│   Port: 8080 (local/dev) | 8081 (prod)                     │
└─────────────────────────────────┬───────────────────────────┘
                                  │
┌─────────────────────────────────┴───────────────────────────┐
│                      数据存储层                              │
├─────────────────────────────────────────────────────────────┤
│   MySQL 8.x  |  InnoDB  |  utf8mb4                         │
│   Flyway 迁移文件管理数据库结构变更                           │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 多环境配置

| 环境 | Profile | 用途 | 端口 | 数据库 |
|------|---------|------|------|--------|
| 本地开发库 | `local` | 个人开发调试 | 8080 | `127.0.0.1:3306/charge_safe_dev` |
| 云端共享开发库 | `dev` | 多人联调测试 | 8080 | `127.0.0.1:13306/charge_safe_dev` |
| 云端正式演示库 | `prod` | 正式部署演示 | 8081 | `127.0.0.1:3306/charge_safe` |

### 1.4 项目包结构

```
com.chargesafe
├── ChargeSafeApplication.java          # 启动类
├── common/                              # 公共组件
│   ├── Result.java                      # 统一返回格式
│   ├── ResultCode.java                  # 错误码枚举
│   ├── PageResult.java                  # 分页返回
│   ├── PageQuery.java                   # 分页查询参数
│   ├── BusinessException.java           # 业务异常
│   ├── GlobalExceptionHandler.java      # 全局异常处理
│   ├── OperationLog.java               # 操作日志注解
│   ├── OperationLogAspect.java         # 日志 AOP 切面
│   ├── OperationLogEntity.java         # 日志实体
│   └── OperationLogMapper.java         # 日志 Mapper
├── config/                              # 配置类
│   ├── MybatisPlusConfig.java           # MyBatis-Plus 配置
│   ├── SecurityConfig.java             # 安全配置
│   └── WebConfig.java                   # CORS 配置
├── security/                            # 安全组件
│   ├── JwtUtils.java                    # JWT 工具类
│   ├── JwtAuthenticationFilter.java     # JWT 过滤器
│   ├── LoginUser.java                   # 登录用户信息
│   └── UserContext.java                 # 用户上下文 (ThreadLocal)
└── modules/                             # 业务模块
    ├── auth/                            # 认证模块
    ├── user/                            # 用户模块
    ├── community/                       # 社区资源模块
    ├── device/                          # 设备模块
    ├── charging/                        # 预约充电模块
    ├── repair/                          # 报修工单模块
    ├── inspection/                      # 巡检隐患模块
    ├── notification/                    # 消息通知模块
    ├── statistics/                      # 统计分析模块
    ├── simulator/                       # 设备模拟模块
    └── log/                             # 操作日志模块
```

## 二、数据库设计

### 2.1 数据库表清单

当前迁移定义共 **25 张核心表**（V1 的 21 张、V3 的 2 张 RBAC 表、V7 的部门和设备分类表），采用 InnoDB + utf8mb4，支持逻辑删除和多社区数据隔离。

| 线路 | 表名 | 说明 | 核心字段 |
|------|------|------|---------|
| 用户权限 | sys_dept | 部门表 | id, parent_id, name, code |
| 用户权限 | sys_user | 用户表 | id, username, password, community_id, dept_id |
| 用户权限 | sys_role | 角色表 | id, role_name, role_code |
| 用户权限 | sys_user_role | 用户角色关联 | user_id, role_id |
| 用户权限 | sys_menu | 菜单/权限表 | id, parent_id, permission_code |
| 用户权限 | sys_role_menu | 角色菜单关联 | role_id, menu_id |
| 用户权限 | operation_log | 操作日志 | user_id, module, operation, duration |
| 社区资源 | community | 社区表 | id, name, address |
| 社区资源 | parking_area | 车棚表 | id, community_id, name |
| 社区资源 | device_category | 设备分类表 | id, name, code, power |
| 社区资源 | charging_device | 充电设备表 | id, parking_area_id, category_id, device_code |
| 社区资源 | charging_port | 充电端口表 | id, device_id, status |
| 居民车辆 | resident_profile | 居民档案 | user_id, community_id, building |
| 居民车辆 | vehicle | 车辆表 | user_id, plate_number |
| 预约充电 | charging_reservation | 充电预约 | user_id, port_id, status, expire_time |
| 预约充电 | charging_order | 充电订单 | order_no, user_id, port_id, amount, status |
| 报修工单 | repair_report | 报修记录 | reporter_id, device_id, fault_desc, status |
| 报修工单 | repair_ticket | 维修工单 | ticket_no, assignee_id, status |
| 报修工单 | repair_ticket_log | 工单日志 | ticket_id, action, from_status, to_status |
| 巡检隐患 | inspection_task | 巡检任务 | community_id, inspector_id, status |
| 巡检隐患 | inspection_record | 巡检记录 | task_id, check_item, check_result |
| 巡检隐患 | safety_hazard | 安全隐患 | community_id, hazard_type, severity, status |
| 消息统计 | notification | 消息通知 | user_id, type, is_read |
| 消息统计 | alarm_record | 告警记录（模拟器写入、统计大屏读取） | device_id, alarm_type, status |
| 消息统计 | charging_fee_rule | 费率规则 | community_id, fee_per_hour |

### 2.2 ER 关系图

```
┌─────────┐     ┌─────────────┐     ┌─────────┐
│ sys_user │────<│sys_user_role│>────│ sys_role │
└────┬─────┘     └─────────────┘     └─────────┘
     │
     ├──N:1── sys_dept
     ├──1:1── resident_profile ──N:1── community
     ├──1:N── vehicle
     ├──1:N── charging_reservation ──N:1── charging_port
     ├──1:N── charging_order
     ├──1:N── repair_report ──1:1── repair_ticket
     └──1:N── notification

┌─────────┐     ┌─────────────┐     ┌─────────────────┐     ┌───────────────┐
│community │────<│ parking_area│────<│ charging_device │────<│ charging_port │
└─────────┘     └─────────────┘     └─────────────────┘     └───────────────┘
                                            │
                                            └──N:1── device_category

┌────────────────┐     ┌──────────────────┐
│inspection_task │────<│inspection_record │
└────────────────┘     └──────────────────┘

┌───────────────┐
│ safety_hazard │
└───────────────┘
```

### 2.3 核心状态枚举

**充电端口状态 (charging_port.status)**
| 状态 | 含义 | 可流转到 |
|------|------|---------|
| IDLE | 空闲 | RESERVED, FAULT, DISABLED, OFFLINE |
| RESERVED | 已预约 | CHARGING, IDLE(超时取消) |
| CHARGING | 充电中 | IDLE(结束充电) |
| COMPLETED_WAITING_PICKUP | 充电完成待取车 | IDLE |
| OVERTIME_OCCUPIED | 超时占位 | IDLE |
| FAULT | 故障 | MAINTENANCE |
| MAINTENANCE | 维修中 | IDLE |
| DISABLED | 停用 | IDLE |
| OFFLINE | 离线 | IDLE |

**维修工单状态 (repair_ticket.status)**
| 状态 | 含义 | 可流转到 |
|------|------|---------|
| PENDING | 待受理 | DISPATCHED |
| DISPATCHED | 已派单 | PROCESSING |
| PROCESSING | 处理中 | WAITING_ACCEPTANCE |
| WAITING_ACCEPTANCE | 待验收 | FINISHED, REJECTED |
| FINISHED | 已完成 | CLOSED |
| CLOSED | 已关闭 | - |
| REJECTED | 已驳回 | DISPATCHED |

**居民报修状态 (repair_report.status)**
| 状态 | 含义 |
|------|------|
| PENDING | 待受理 |
| DISPATCHED | 已派单 |
| PROCESSING | 处理中 |
| WAITING_ACCEPTANCE | 待验收 |
| FINISHED | 已完成 |
| REJECTED | 已驳回 |

**安全隐患状态 (safety_hazard.status)**
| 状态 | 含义 | 可流转到 |
|------|------|---------|
| PENDING | 待处理 | RECTIFYING |
| RECTIFYING | 整改中 | WAITING_REVIEW |
| WAITING_REVIEW | 待复查 | REVIEW_PASSED, REVIEW_FAILED |
| REVIEW_PASSED | 复查通过 | CLOSED |
| REVIEW_FAILED | 复查不通过 | RECTIFYING |
| CLOSED | 已关闭 | - |

## 三、接口设计

### 3.1 接口规范

- 统一返回格式：`{ "code": 200, "message": "success", "data": {} }`
- 认证方式：JWT Token，Header `Authorization: Bearer {token}`
- 分页参数：`page` (页码), `size` (每页条数)
- 分页返回：`{ "total": N, "page": N, "size": N, "pages": N, "records": [] }`

### 3.2 接口清单

| 模块 | 路径前缀 | 接口数 | 说明 |
|------|---------|-------|------|
| 认证 | `/api/auth` | 2 | 登录、获取当前用户 |
| 社区 | `/api/communities` | 5 | CRUD + 启停 |
| 车棚 | `/api/parking-areas` | 5 | CRUD + 列表 |
| 设备 | `/api/devices` | 5 | CRUD + 启停 |
| 设备分类 | `/api/devices/categories` | 4 | 列表、创建、更新、删除（删除时校验设备引用） |
| 部门 | `/api/system/depts` | 4 | 列表、创建、更新、删除（维护树与用户引用完整性） |
| 端口 | `/api/ports` | 8 | CRUD + 批量创建 + 统计 |
| 预约 | `/api/reservations` | 4 | 创建、取消、查询 |
| 充电 | `/api/charging` | 2 | 开始、结束 |
| 订单 | `/api/orders` | 3 | 列表、详情 |
| 报修（管理） | `/api/repair-reports` | 2 | 社区范围列表、详情 |
| 报修（居民） | `/api/resident/repair-reports` | 3 | 本人创建、列表、详情 |
| 工单 | `/api/repair-tickets` | 8 | 派单、接单、完成、验收等 |
| 巡检 | `/api/inspection-tasks` | 7 | 创建、开始、提交、完成 |
| 隐患 | `/api/hazards` | 6 | 上报、整改、复查、关闭 |
| 通知 | `/api/notifications` | 4 | 列表、已读、未读数 |
| 统计 | `/api/statistics` | 1 | 仪表盘数据 |
| 模拟 | `/api/simulator` | 4 | 端口状态变更、故障模拟 |
| 日志 | `/api/logs` | 2 | 列表、详情 |

> 接口数量会随版本演进，不以本文的汇总数字作为运行契约；精确路径、参数和权限以各 Controller 的方法签名与 OpenAPI 文档为准。

## 四、核心业务流程

### 4.1 预约充电流程

```
居民查看空闲端口 → 选择端口 → 提交预约 → 系统校验
                                              │
                                              ▼
                                    ┌─── 校验通过 ───┐
                                    │                 │
                                    ▼                 ▼
                              创建预约记录      提示"预约失败"
                              端口状态→RESERVED
                              发送通知
                                    │
                                    ▼
                              居民扫码开始充电
                              预约状态→CONVERTED
                              端口状态→CHARGING
                              创建充电订单
                                    │
                                    ▼
                              结束充电 / 充满自动结束
                              端口状态→IDLE
                              计算费用
                              订单状态→COMPLETED
                              发送完成通知
```

### 4.2 报修工单流程

```
居民/物业/巡检提交报修 → 创建报修记录 → 通知物业
                                          │
                                          ▼
                                    物业审核确认
                                    生成维修工单
                                    派发给运维人员
                                    通知运维
                                          │
                                          ▼
                                    运维接单 → 开始处理
                                    工单状态→PROCESSING
                                          │
                                          ▼
                                    维修完成
                                    上传维修照片
                                    工单状态→WAITING_ACCEPTANCE
                                    通知物业验收
                                          │
                                          ▼
                                    物业验收
                                    ├── 通过 → 报修单→FINISHED、工单关闭、端口恢复IDLE
                                    └── 驳回 → 重新派单
```

### 4.3 巡检隐患流程

```
物业创建巡检任务 → 分配给巡检人员 → 通知巡检人员
                                      │
                                      ▼
                                巡检人员开始巡检
                                填写巡检记录
                                      │
                                      ▼
                                发现异常 → 上报安全隐患
                                通知物业处理
                                      │
                                      ▼
                                物业执行整改
                                提交整改结果
                                通知巡检人员复查
                                      │
                                      ▼
                                巡检人员复查
                                ├── 通过 → 隐患关闭
                                └── 不通过 → 重新整改
```

## 五、安全设计

### 5.1 认证授权

- 使用 JWT Token 认证，有效期 24 小时
- Token 包含 userId、username、roleCode
- 通过 ThreadLocal 存储当前登录用户信息

### 5.2 数据权限

- 平台管理员：可访问所有社区数据
- 物业管理员：只能访问所属社区数据
- 运维人员：只能访问分配给自己的工单
- 巡检人员：只能访问分配给自己的任务
- 居民用户：只能访问自己的数据

### 5.3 操作日志

- 基于 AOP + 自定义注解 `@OperationLog`
- 自动记录操作人、操作模块、操作描述、请求参数、IP、耗时
- 支持按用户名、模块筛选查询

## 六、部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                     用户浏览器                               │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     Nginx (80/443)                          │
│                     charge.sfwang.xyz                       │
├───────────────────────────┬─────────────────────────────────┤
│  /                        │  /api                           │
│  静态文件 (Vue3 dist)      │  反向代理                       │
│                           │  → http://127.0.0.1:8081/api   │
└───────────────────────────┴─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│               Spring Boot (127.0.0.1:8081)                  │
│               Profile: prod                                 │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│               MySQL 8.x (127.0.0.1:3306)                    │
│               Database: charge_safe                         │
└─────────────────────────────────────────────────────────────┘
```

### 6.1 服务器文件位置

| 文件 | 位置 |
|------|------|
| 后端 JAR | `/opt/charge-safe/charge-safe-backend.jar` |
| 环境变量 | `/etc/charge-safe/charge-safe.env` |
| Nginx 配置 | `/etc/nginx/conf.d/charge-safe.conf` |
| 前端静态文件 | `/opt/charge-safe/admin/` |
