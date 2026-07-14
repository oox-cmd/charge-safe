# ChargeSafe 中期开发基线审查

> 审查日期：2026-07-14
> 代码基线：`d0c624975fb33fb8ebda50b7d3f1ab0847b12ff4`（`main` 与 `origin/main` 一致）
> 性质：中期事实基线，不是最终交付结论。
> 状态更新：2026-07-15 已关闭 JWT 认证过滤器 fail-open 路径；修复提交待本次提交生成。其余审查结论除明确标记外不变。

## 1. 审查范围与方法

本轮遍历 329 个有效文件，排除了 `.git/`、`node_modules/`、`target/`、`dist/`、`unpackage/`、日志和缓存。实质核对了 144 个后端主源码文件、2 个后端测试文件、65 个管理端源码文件、36 个小程序源码文件、9 个 SQL 文件、22 个配置文件、5 个脚本和 24 份 Markdown 文档。

判断优先级为：运行配置与迁移文件 > Controller/Service/Entity/前端调用 > 测试 > README > 设计与计划文档。计划中出现的功能不计为已实现。

## 2. 当前真实技术基线

| 范围 | 当前事实 | 证据 |
| --- | --- | --- |
| 后端 | Spring Boot 3.2.5、Java 17 编译目标、MyBatis-Plus 3.5.5、Flyway、MySQL、JWT | `charge-safe-backend/pom.xml` |
| 管理端 | Vue 3、Vite 8、Element Plus、Pinia 3、ECharts 6、Axios、Lottie | `charge-safe-admin/package.json` |
| 小程序 | uni-app、Vue 3.4.21、Pinia 2.2.4、Vite 5；固定 npm 10.9.8 | `charge-safe-miniapp/package.json`、`package-lock.json` |
| 数据库 | Windows 原生 MySQL 为本地开发主方案；WSL 脚本动态解析 Windows 网关 | `README.md`、`application-local.yml`、`scripts/dev-backend-wsl.sh` |
| 迁移 | `classpath:db/migration`，真实目录含 V1 至 V5 | `application.yml`、`src/main/resources/db/migration/` |
| 容器 | 当前仓库没有 Compose、Dockerfile 或 Docker Profile；本地开发不要求 Docker | 全仓库文件清单、`README.md` |
| 测试 | 3 个测试类、28 个测试；2026-07-15 在 OpenJDK 21.0.11 下 `mvn clean test` 与 `mvn clean package` 均通过，仍有 Mockito/Byte Buddy 动态 agent 的未来兼容性警告 | `src/test/java/`、`JwtAuthenticationFilterHttpTest.java`、2026-07-15 验证记录 |

`application-h2.yml` 和 H2 SQL 存在，但 `pom.xml` 没有 H2 依赖，不能据此声称 H2 测试环境可运行。

## 3. 模块完成度

本表以跨端闭环为单位，共 20 个审查单元：基本实现 9 个、部分实现 8 个、未实现 3 个。

| 审查单元 | 状态 | 事实摘要 |
| --- | --- | --- |
| 用户管理 | 基本实现 | 后端 CRUD、状态、角色分配和管理端页面存在 |
| 社区管理 | 基本实现 | 后端 CRUD/状态与管理端页面存在，社区有经纬度 |
| 车棚管理 | 基本实现 | 后端 CRUD 与管理端页面存在 |
| 设备与端口 | 基本实现 | 设备 CRUD、端口查询/批量创建/状态修改存在 |
| 巡检与隐患 | 基本实现 | 任务、记录、隐患整改复查主流程存在 |
| 操作日志 | 基本实现 | AOP 记录和查询页面存在 |
| 统计分析 | 基本实现 | 6 个聚合接口和统计页面存在，但缺少三维专用数据 |
| 设备模拟 | 基本实现 | 4 个模拟接口和管理端页面存在，仅用于演示 |
| 管理端业务 CRUD | 基本实现 | 主要业务页面已接后端，仍有权限与体验缺口 |
| 登录认证 | 基本实现（fail-open 已修复） | JWT 生成/解析存在；除 `POST /api/auth/login` 外，缺失、格式错误或无效 Token 均由过滤器直接返回统一 HTTP 401；HTTP 安全测试覆盖用户缺失、禁用、权限不足与上下文清理 |
| 角色、菜单与权限 | 部分实现 | RBAC 结构存在；前端权限码与后端/迁移有 8 组不一致 |
| 预约与充电 | 部分实现 | 后端主流程较完整；小程序仍以本地定时器和静态数据演示 |
| 报修与工单 | 部分实现 | 后端流程存在；小程序提交与列表未真实接入 |
| 通知 | 部分实现 | 后端与管理端存在；小程序调用的是 Mock 专用接口 |
| 小程序 | 部分实现 | 17 个页面可导航，但核心认证、预约、充电、订单、报修多为 Mock/静态 |
| 2D 大屏 | 部分实现 | ECharts/Lottie 页面存在；端口明细合成，告警/电气指标含静态值 |
| 自动化测试 | 部分实现 | 覆盖集中在充电和菜单树，当前 WSL/JDK 21 验证未通过 |
| 部门管理 | 未实现 | 管理端明确标记为前端演示数据，无后端模块 |
| 设备分类 | 未实现 | 管理端明确标记为前端演示数据，无后端模块 |
| 三级三维大屏 | 未实现 | 无 Three.js/MapLibre/Cesium 依赖、场景状态机、模型或三维接口 |

## 4. 关键业务闭环

- 后端预约：创建预约、冲突检查、过期、取消、转充电订单均有实现。
- 后端报修：报修、确认、派单、维修、验收、驳回和关闭具备流程。
- 后端巡检：任务、开始/完成、记录、隐患整改与复查具备流程。
- 管理端可操作以上流程，但按钮权限码并非全部与后端一致。
- 小程序“找车棚 → 预约 → 充电 → 结束 → 记录/报修”只有页面路径，数据没有形成真实后端闭环。

## 5. 安全与一致性发现

1. 历史问题（2026-07-15 已修复）：`JwtAuthenticationFilter` 曾只在 Token 有效时填充上下文，对缺失/无效 Token 继续放行。当前过滤器将 `POST /api/auth/login` 作为唯一 `/api/*` 匿名白名单，其余请求缺失、格式错误或无效 Token 时清理 `UserContext`、返回统一 HTTP 401，并停止 filter chain；证据为 `JwtAuthenticationFilter.java` 与 `JwtAuthenticationFilterHttpTest.java`。
2. `SecurityConfig` 仍使用项目既有的自定义 Servlet 过滤器注册方式；本次未引入 Spring Security Web，也未改变权限码体系。认证失败由过滤器返回 HTTP 401，已认证但无业务权限仍由 `PermissionAspect` 和既有响应机制处理。
3. `WebConfig` 使用宽泛的跨域来源模式并允许凭据，正式部署前必须收紧。
4. `application.yml` 含可用的 JWT 默认值；本地示例可保留占位符，但运行配置应要求环境变量。
5. 管理端 `system:*:add/edit/reset-pwd/toggle-status` 与后端/迁移的 `create/update/reset-password/status` 不一致。
6. 大屏路由当前无需登录，它调用的统计接口也没有方法级权限标记；已确认目标是纳入管理端登录、菜单和权限体系，现状属于下一阶段 P1，不得通过绕过认证实现演示。
7. 小程序请求层无条件引入 Mock 拦截器，`USE_MOCK` 常量没有控制安装；真实设备还无法使用硬编码 `localhost` 访问开发机。

## 6. 大屏真实状态与数据能力

当前 `views/bigscreen/Index.vue` 是 987 行单文件二维页面。真实接入包括仪表盘、状态汇总、订单趋势和车棚使用率；非真实部分包括：

- 按聚合数量在前端生成端口对象，`id` 只是循环序号，并非真实 `charging_port.id`；
- 告警、待处理工单、待整改隐患为静态数组；
- 运行状态、负载、能耗、电压、电流、充电时长为固定展示值；
- 端口状态映射遗漏 `COMPLETED_WAITING_PICKUP` 和 `OVERTIME_OCCUPIED`；
- 数据库只有社区 `lat/lng`，没有建筑、站点、设备或端口场景坐标；
- `alarm_record` 只有表，没有对应 Entity/Service/Controller；
- 没有端口状态历史，不能支持真实时间轴回放。

因此当前大屏是“可运行的二维演示页”，不是数字孪生，也不能声称数据全部实时。

## 7. UI/UX 审查

结合 `ui-ux-pro-max` 与 `frontend-design`，当前主要问题是信息架构而非单纯配色：

- 管理端 CRUD 页高度同构，搜索卡、表格卡、弹窗重复，功能层级弱；
- 大屏使用通用深蓝、发光面板和 Lottie 主视觉，但关键端口状态仍是合成数据；
- 多处用 emoji/文本符号承担结构图标，缺少一致图标体系和可访问标签；
- 点击元素大量使用 `div`，键盘焦点与语义不足；
- 小程序首页并未把“找车棚并判断能否预约”作为第一视觉任务；
- 空、加载、失败状态覆盖不完整，若请求失败常静默保留空图表。

推荐方向是深蓝灰/近黑底、中性建筑材质、状态色专用、少量稳定面板、可读文本与形状双编码，并尊重 `prefers-reduced-motion`。

## 8. 现有文档可信度

| 文档 | 判定 | 主要原因 | 处理建议 |
| --- | --- | --- | --- |
| `README.md` | 基本有效 | 环境与无 Docker 流程较新；小程序“核心原型”容易被误读为真实闭环 | 保留，持续更新阶段说明 |
| `overall-function-and-experience-audit.md` | 基本有效 | UI 与 Mock 判断仍成立 | 作为专项审查保留 |
| `frontend-ui-ai-flavor-audit.md` | 基本有效 | 模板化、emoji、假数据判断与源码一致 | 保留并在实施后复核 |
| `new-frontend-design-guidelines.md` | 当前有效 | 设计约束可用，但端口状态需补齐 9 状态 | 修订状态矩阵 |
| `frontend-polish-and-fix-execution-plan.md` | 基本有效但需重排 | 仍以二维大屏为主，部分接口路径写法不精确 | 合并进新路线图 |
| `database-design.md` | 严重过时 | 写作 V2、把 V3 已创建菜单表标为“需新增” | 重写为 V5 事实版 |
| `frontend-page-design.md` | 严重过时 | 把用户、角色、统计、大屏等现有页面写成未实现 | 归档或重写 |
| `dashboard-visual-design.md` | 部分过时 | 只规划二维与简化三维，接口如 `/api/ports/statistics` 不存在 | 由专项研究文档取代事实部分 |
| `miniapp-page-design.md` | 严重过时 | 仍称只有骨架，页面清单已变化；也没有揭示 Mock 强依赖 | 作为早期计划归档 |
| `source-code-analysis.md` | 严重过时 | 把现有页面和大屏写成未实现 | 作为历史基线保留 |
| `fact-verification-report.md` | 部分事实错误 | 声称 `@RequiresPermission` 未使用，实际多个 Controller 已使用 | 不再作为唯一事实源 |
| `docs/summary.md` | 严重过时 | 版本 V0.2、61 接口、旧 dev 数据库与部署结构 | 后续重写，不在本轮扩写 |

## 9. 问题分级

### P0（0 项）

当前未发现必须在本轮立即破坏性处理的 P0；这不代表已可最终交付。

### P1（8 项：1 项已完成，7 项待处理）

1. ~~后端认证过滤器 fail-open。~~ **已于 2026-07-15 修复并通过 12 项 HTTP 安全测试、28 项完整后端测试及本地运行态冒烟；提交哈希待本次提交生成。**
2. 前端与后端权限码不一致。
3. 小程序 Mock 无条件启用。
4. 小程序 `localhost` 地址不可用于真机。
5. 小程序核心业务链路没有真实闭环。
6. 大屏端口、告警和电气指标混入合成/静态数据。
7. 多个业务端点权限边界不一致，大屏当前匿名路由与已确认的登录后访问策略冲突。
8. JDK 17 要求与当前 JDK 21 测试环境不一致，自动化测试未形成稳定绿线。

### P2（10 项，建议本阶段完善）

1. 建立站点和端口级真实统计接口。
2. 明确 `alarm_record` 是否进入业务实现。
3. 补齐 9 种端口状态的跨端语义。
4. 建立大屏场景状态机与业务 ID 映射。
5. 为模型、纹理、HDRI 建立资产许可证台账。
6. 扩充测试到权限、统计、维修、巡检和 Controller 安全边界。
7. 处理 H2 配置与依赖不一致。
8. 收紧 CORS 和 JWT 默认配置。
9. 管理端拆分大屏巨型单文件并按层级懒加载。
10. 更新数据库、页面和接口事实文档。

### P3（6 项，长期优化）

1. 统一图标并移除结构性 emoji。
2. 完善键盘、ARIA、减弱动效与色盲冗余编码。
3. 优化管理端大包：当前统计相关 chunk 超过 1 MB。
4. 对 Lottie `eval` 构建警告和替代方案做安全评估。
5. 增加端口状态历史后再考虑时间轴回放。
6. 统一历史文档归档标识与版本元数据。

## 10. 当前阶段结论

ChargeSafe 处于“后端 P0 业务基础较完整、管理端可演示、小程序和大屏仍需真实化”的中期开发阶段。三级三维大屏可以进入原型设计，但必须先落实已确认的登录后访问边界、真实数据口径和 9 状态映射；不能用更精细模型掩盖端口级数据缺失。
