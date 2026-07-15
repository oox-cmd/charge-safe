# ChargeSafe 项目工作规则

- 当前项目处于中期开发阶段，不得把计划、原型或构建通过表述为最终交付。
- 项目事实以源码、运行配置、Flyway 迁移和测试结果为准；README、设计文档和计划与代码冲突时，先调查并记录差异。
- 已发布的 `charge-safe-backend/src/main/resources/db/migration/V1__*.sql` 至 `V5__*.sql` 禁止修改、移动、复制、重命名或替换，也不得修改 Flyway checksum 或历史表。
- pending migration 表示尚待执行，不等于迁移异常；未经明确批准不得新增数据库迁移。
- 前端创建、整改、视觉审查和大屏任务必须加载用户级 `ui-ux-pro-max` 与 `frontend-design`。
- 调试失败、回归或跨端不一致时，先加载 `systematic-debugging`；宣称完成、修复或可交付前加载 `verification-before-completion` 并执行新鲜验证。
- 三级大屏 MVP 固定采用原生 Three.js + `three-globe`；Vue 只负责页面、状态和生命周期，渲染引擎放入独立组件、模块和 composable。
- 大屏默认属于管理端认证、菜单和权限体系，不得为大屏绕过认证过滤器或权限控制。
- 外部代码、模型、纹理和 HDRI 进入仓库前必须核对并登记许可证；MVP 优先 CC0，代码许可证与资产许可证分别核验。
- 三维场景必须通过显式映射绑定真实 `communityId`、`stationId`、`deviceId` 和 `portId`；模型节点名不得替代业务 ID。
- Mock、静态或合成数据必须明确标识，不得表述为真实业务数据或实时数据。
- 本地开发使用 Windows 原生 MySQL、Maven、Node.js 和 npm；不得重新引入 Docker 开发工作流。
- 后端正式 Java 基线为 JDK 21；`charge-safe-backend/pom.xml` 的编译 release 与 Maven 运行时均必须保持为 21，不得新增 Java 17 兼容配置或以 JDK 17 规避测试失败。
- 后端不得使用 Java preview feature、`--enable-preview`、无明确官方依据的 `--add-opens` 或 `--add-exports`。
- 声明后端修改完成前，必须在 JDK 21 下重新执行完整测试与打包；测试不得依赖运行时动态 Agent 自附加。
- 修改应保持范围最小；未经授权不修改业务源码、依赖、数据库结构、系统配置，不提交敏感文件。
