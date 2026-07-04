# ChargeSafe 重构与架构调整建议

> 生成时间：2026-07-04

---

## 1. 当前项目整体质量判断

### 结论：基础较好，建议保留并局部优化

**依据**：

1. **后端结构清晰**：采用标准的 Spring Boot 分层架构，模块划分合理
2. **数据库设计规范**：表结构符合第三范式，状态字段完整，索引设计合理
3. **业务流程完整**：预约、充电、报修、工单、巡检、隐患的核心流程已实现
4. **代码质量可接受**：使用了 AOP 日志、全局异常处理、统一返回格式等最佳实践

**需要优化的地方**：

1. **user 模块不完整**：缺少 Controller、DTO、VO
2. **前端页面不完整**：缺少用户管理、统计分析、可视化大屏页面
3. **小程序端缺失**：只有基础骨架
4. **大屏和 3D 未实现**：需要新增

---

## 2. 后端重构建议

| 模块 | 当前问题 | 建议方式 | 是否第一阶段 | 原因 |
|------|----------|----------|--------------|------|
| auth | 无明显问题 | 保留 | - | 已基本完成 |
| user | 缺少 Controller、DTO、VO | 小修 | ✅ 是 | 需要用户管理功能 |
| community | 无明显问题 | 保留 | - | 已基本完成 |
| device | 无明显问题 | 保留 | - | 已基本完成 |
| charging | 预约冲突检测需加强 | 小修 | ✅ 是 | 核心业务流程 |
| repair | 无明显问题 | 保留 | - | 已基本完成 |
| inspection | 无明显问题 | 保留 | - | 已基本完成 |
| notification | 缺少 DTO/VO | 小修 | ✅ 是 | 消息通知功能 |
| statistics | 统计逻辑简单 | 局部重构 | ✅ 是 | 需要更丰富的统计 |
| simulator | 缺少 Entity | 小修 | ✅ 是 | 设备模拟功能 |
| log | 无明显问题 | 保留 | - | 已基本完成 |

### 2.1 user 模块补充

需要新增：

1. **UserController** - 用户 CRUD 接口
2. **RoleController** - 角色 CRUD 接口
3. **UserDTO / UserVO** - 请求参数和响应对象
4. **RoleDTO / RoleVO** - 请求参数和响应对象

### 2.2 statistics 模块增强

需要新增：

1. **StatisticsMapper** - 统计数据查询
2. 更丰富的统计指标（故障率、维修时长、巡检完成率等）

### 2.3 需要新增的表

| 表名 | 用途 | 优先级 |
|------|------|--------|
| sys_menu | 菜单权限表 | P0 |
| sys_role_menu | 角色菜单关联表 | P0 |

---

## 3. 前端管理端重构建议

| 页面 | 当前问题 | 建议方式 | 参考项目 | 是否第一阶段 |
|------|----------|----------|----------|--------------|
| 登录页 | 无明显问题 | 保留 | - | - |
| 工作台首页 | 图表较少 | 小修 | vue3-element-admin | ✅ 是 |
| 社区管理 | 无明显问题 | 保留 | - | - |
| 车棚管理 | 无明显问题 | 保留 | - | - |
| 设备管理 | 无明显问题 | 保留 | - | - |
| 端口管理 | 无明显问题 | 保留 | - | - |
| 预约记录 | 无明显问题 | 保留 | - | - |
| 充电订单 | 无明显问题 | 保留 | - | - |
| 报修记录 | 无明显问题 | 保留 | - | - |
| 维修工单 | 无明显问题 | 保留 | - | - |
| 巡检任务 | 无明显问题 | 保留 | - | - |
| 安全隐患 | 无明显问题 | 保留 | - | - |
| 消息通知 | 无明显问题 | 保留 | - | - |
| 操作日志 | 无明显问题 | 保留 | - | - |
| 用户管理 | 未实现 | 新增 | RuoYi-Vue | ✅ 是 |
| 角色管理 | 未实现 | 新增 | RuoYi-Vue | ✅ 是 |
| 统计分析 | 未实现 | 新增 | vue3-element-admin | ✅ 是 |
| 可视化大屏 | 未实现 | 新增 | Vue3Echarts | ✅ 是 |

---

## 4. 数据库重构建议

### 4.1 保留的表

所有现有表都保留，无需删除。

### 4.2 需要新增的表

| 表名 | 用途 | 优先级 |
|------|------|--------|
| sys_menu | 菜单权限表 | P0 |
| sys_role_menu | 角色菜单关联表 | P0 |

### 4.3 需要补充的字段

| 表名 | 字段 | 说明 |
|------|------|------|
| sys_user | avatar | 头像URL（已有） |
| notification | business_url | 业务链接（可选） |

### 4.4 Flyway 迁移文件

需要新增 V3__add_menu_tables.sql：

```sql
-- 菜单权限表
CREATE TABLE sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT DEFAULT 0,
  menu_name VARCHAR(50) NOT NULL,
  menu_type TINYINT DEFAULT 1,
  path VARCHAR(200),
  component VARCHAR(200),
  icon VARCHAR(50),
  sort_order INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
);

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_menu (role_id, menu_id)
);
```

---

## 5. 小程序端建议

### 结论：从零搭建核心流程原型

**原因**：
1. 当前只有基础骨架，没有实际功能
2. 小程序端是居民用户的主要入口
3. 需要实现预约、充电、报修等核心流程

**第一阶段最小实现**：
1. 登录页
2. 首页（车棚列表）
3. 车棚详情（端口状态）
4. 预约确认
5. 我的订单
6. 报修页

---

## 6. 大屏和 3D 建议

### 第一阶段：2D 大屏

- 使用 Vue3 + ECharts
- 实现端口状态环形图、订单趋势折线图、告警滚动列表
- 参考 Vue3Echarts、es-big-screen

### 第二阶段：简化 3D

- 使用 Three.js 或 TresJS
- 实现车棚、充电桩的简单 3D 场景
- 参考 smartPark-3D、3d-datacenter

### 第三阶段：模型增强

- 导入 GLB/GLTF 模型
- 添加动效和交互
- 参考图扑智慧充电桩

---

## 7. 风险说明

### 如果执行局部优化

1. **影响范围**：主要影响 user 模块和前端新增页面
2. **数据库影响**：新增 2 张表（sys_menu, sys_role_menu）
3. **接口影响**：新增用户管理、角色管理接口
4. **页面影响**：新增用户管理、统计分析、大屏页面

### 如何控制风险

1. 使用 Git 分支开发
2. 先完成数据库迁移，再开发后端，最后开发前端
3. 保持现有功能不被破坏

---

## 8. 推荐执行路线

### 第一阶段（2周）

1. **数据库**：新增 sys_menu、sys_role_menu 表
2. **后端**：补充 user 模块 Controller、DTO、VO
3. **前端**：新增用户管理、角色管理页面
4. **前端**：优化工作台首页，增加统计图表
5. **小程序**：搭建核心流程骨架

### 第二阶段（1周）

1. **前端**：新增可视化大屏页面
2. **后端**：增强统计模块
3. **小程序**：完善核心流程

### 第三阶段（1周）

1. **3D 场景**：实现简化 3D 车棚
2. **优化**：性能优化、bug 修复
3. **文档**：完善项目文档
