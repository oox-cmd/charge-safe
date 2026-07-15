# 居民与管理大屏访问模型拆分记录

- 日期：2026-07-15
- 类型：P1 Bug 修复
- 范围：认证后的默认入口、路由授权、管理统计接口边界、居民社区范围数据、V6 权限数据
- 不在范围：三级 Three.js 大屏、管理大屏视觉整改、小程序、预约/充电流程、JWT 实现、V1-V5

## 1. 原问题与数据安全判断

历史 `/bigscreen` 配置为匿名路由，RESIDENT 登录后仅凭 `roleCode` 被导向该页。页面请求全局仪表盘、端口状态、订单趋势和车棚使用率，并混有工单、隐患与设备运行展示，因此不能直接作为居民页面，也不能把 `statistics:bigscreen:view` 授予居民。

采用确认后的方案 B：保留 `/bigscreen` 作为管理运维大屏，新增 `/resident/dashboard`、`resident:dashboard:view`、居民专用接口和精简 DTO。居民页只回答“当前社区哪里有可预约端口”，不返回全平台统计、金额、工单/隐患详情、操作日志或设备内部参数。

## 2. 修改前事实矩阵

| 环节 | 修改前事实 | 风险 |
| --- | --- | --- |
| 登录跳转 | RESIDENT 直接跳 `/bigscreen` | 角色码绕过页面权限语义 |
| 管理路由 | `/bigscreen` 为 `requiresAuth:false` | 匿名加载管理页面 |
| 管理数据 | 页面组合 4 个无方法级权限的统计 GET | 任意已登录用户可请求管理统计 |
| 居民权限 | V3 明确不分配后台菜单，权限数为 0 | 没有合法居民 Web 入口 |
| 社区范围 | 统计接口可不传 `communityId`，表示全局 | 居民存在跨社区风险 |

## 3. TDD 红测证据

先新增居民服务、HTTP 权限、V6 和前端路由契约测试，再编写实现。

- 后端首次定向编译出现 7 个缺失类型/方法错误：居民 Controller、Service、VO 与管理聚合尚不存在。
- 前端路由契约首次运行 2/2 失败：RESIDENT 仍指向 `/bigscreen`，管理页仍调用旧的分散统计 API。
- 测试只针对当前缺失访问模型，没有通过扩大白名单、Mock 整条安全链或修改 JWT 制造结果。

## 4. 修复方式

### 4.1 V6

`V6__add_resident_dashboard_permission.sql`：

- 新增隐藏权限节点 `resident:dashboard:view`；
- 通过稳定 `permission_code` 和 `role_code='RESIDENT'` 查找；
- 两次插入均带 `NOT EXISTS`；
- 只建立 RESIDENT 角色映射，不挂管理父菜单，不修改其他角色，不改表结构。

### 4.2 居民后端

新增 `GET /api/resident/dashboard` 与 `@RequiresPermission("resident:dashboard:view")`。`ResidentDashboardServiceImpl` 不接收客户端 `communityId`，只读取 `UserContext.getCommunityId()`；无社区时返回明确空结果且不查全库。查询顺序从社区开始，依次限定车棚、设备和端口，再生成安全 DTO。

九种原始端口状态保持不变，居民汇总为：IDLE、RESERVED、CHARGING、FAULT、MAINTENANCE 和其他不可用；`COMPLETED_WAITING_PICKUP`、`OVERTIME_OCCUPIED`、`DISABLED`、`OFFLINE` 进入其他不可用，不伪造成空闲端口。

### 4.3 管理大屏

- `/bigscreen` 改为 `requiresAuth:true` 与 `statistics:bigscreen:view`；
- 新增管理聚合 `GET /api/statistics/bigscreen`，使用同一权限；
- 管理页只请求该聚合接口；
- 共享 dashboard/port GET 使用原有 `dashboard:view`，分析 GET 使用 `statistics:analysis:view`，避免破坏维护/巡检工作台；
- 未改变统计计算口径，也未整改旧大屏的合成端口和静态告警展示。

### 4.4 管理端居民页与路由

- RESIDENT 登录后等待用户和后端权限加载，再进入 `/resident/dashboard`；
- 刷新时恢复身份与权限后再判断路由；
- 权限精确匹配后端返回值，不增加本地角色矩阵或 `:*`；
- 新页面显示社区、可预约端口、车棚和真实状态汇总，并覆盖加载、错误、重试、无社区和空车棚状态；
- 页面采用中性绿色可用性强调，状态同时使用文字、颜色和形状，不复制管理大屏视觉；路由与后端同时校验 RESIDENT 角色，避免平台管理员既有“枚举全部启用权限码”语义扩大居民专页入口。

## 5. 修改范围

- 后端：resident Controller/Service/VO、management bigscreen VO、Statistics Controller/Service、V6。
- 管理端：resident API/页面、403 页面、router、management bigscreen API 与页面取数。
- 测试：居民服务、访问 HTTP、V6、路由静态契约，既有权限契约扩展到 V6。
- 文档：中期审查、证据索引、路线图、数据库/前端设计、场景建模、README 与本记录。

未修改 JWT 过滤器、依赖文件、小程序、V1-V5、数据库表结构或三级三维大屏代码。

## 6. 验证结果

| 验证 | 结果 |
| --- | --- |
| 定向测试 | 18/18 通过 |
| `mvn clean test` | 47/47 通过，0 failure/error |
| `mvn clean package` | 47/47 再次通过并成功打包 |
| 管理端 `npm ci` / `npm run build` | 通过；0 vulnerabilities；保留既有大 chunk/Lottie 警告 |
| Flyway | local Profile 校验 V1-V5 后执行 V6；V0-V6 全部 success=1，无 pending |
| checksum | V1-V5 与迁移前完全一致 |
| 角色权限 | RESIDENT 含居民权限且无后台权限；PROPERTY_ADMIN 保留管理大屏；MAINTAINER/INSPECTOR 未获两个新页面权限 |
| API | 匿名/无效 JWT 为 HTTP 401；居民访问管理统计为既有业务 403；平台/物业管理聚合成功 |
| 社区隔离 | 居民 DTO communityId 等于登录上下文；追加其他 `communityId` 参数不改变结果 |
| 浏览器 | 匿名居民路由转登录；居民默认入口、刷新恢复、管理大屏 403、平台管理大屏均通过 Edge headless 实测 |
| UI 复核 | 居民真实社区与端口状态清晰；无管理敏感区块、随机数据、三维或模板化科技装饰 |

运行验证只调用登录和只读 GET。V6 由后端正常 Flyway 流程执行；没有手工编辑 Windows MySQL，没有 repair、clean 或 baseline。

## 7. 未处理事项

- 管理二维大屏仍包含合成端口对象、静态告警/工单/隐患和固定电气展示；访问边界修复不等于数据已全部真实化。
- 三级 Three.js 管理大屏仍未实现。
- 管理端当前无正式单元测试框架，本次以 Java 静态契约、生产构建、Vite 代理和真实浏览器冒烟覆盖路由链路。
- PLATFORM_ADMIN 沿用既有“枚举全部启用权限码”语义，因此权限接口会包含新居民权限；V6 数据映射本身只授予 RESIDENT，且居民路由与接口另有角色校验，平台管理员不能据此进入居民专页。本次没有重构该既有平台管理员权限枚举语义。
- local Profile 登录验证暴露了既有日志风险：`OperationLogAspect` 会把 `LoginDTO` 参数写入操作日志，当前调试日志也会显示该参数，其中包含明文开发密码。本次按边界未修改日志/AOP；后续应单独立项做登录参数脱敏，并在处理前避免公开运行日志。
- 下一项仍是“小程序 Mock 显式开发开关”，本次没有开始。
