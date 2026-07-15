# 2026-07-15 开发记录：后端 Java 21 技术栈统一

- 基线提交：`3eb44663df8057113318a1e71b19fca8c01a930c`（开始时 `main` 与 `origin/main` 一致，且包含 `784a9f7d1d756ab5350b9dbf4d4a62475e7191e2`）
- 目标：在纯 JDK 21 环境下稳定编译、测试、打包和启动后端，不以 JDK 17 作为兜底。
- 范围内文件：后端 POM、两个单元测试、测试资源、Windows 启动说明、根 README、审计/证据/路线/过程文档。
- 范围外事项：未修改业务接口、权限语义、数据库结构、Flyway V1-V6、管理端、小程序或大屏。

## 原技术基线与冲突清单

- POM 仅声明 `java.version=17`，有效 POM 的 `maven.compiler.release` 也是 17；但实际 Maven 为 OpenJDK 21.0.11。
- Spring Boot 3.2.5 管理 Maven Compiler 3.11.0、Surefire 3.1.2、Mockito 5.7.0、Byte Buddy 1.14.13、Lombok 1.18.32、MyBatis-Plus 3.5.5、MySQL Connector/J 8.3.0、Flyway 9.22.3、JUnit Jupiter 5.10.2。
- 修改前 `mvn clean test` 和 `mvn clean package` 均为 47/47 通过，但编译使用 release 17、javac 报隐式注解处理与 unchecked 警告，Mockito/Byte Buddy 在运行时动态加载 Agent，并提示未来 JDK 默认禁止。
- 未发现 preview、`--enable-preview`、`--add-opens`、`--add-exports`、JDK 内部 API 或 Java EE `javax` 与 Jakarta Web API 混用造成的 Java 21 运行冲突；`javax.crypto.SecretKey` 是 JDK 标准加密 API，不是旧 Java EE API。

## 根因与处理

- 根因：Boot 管理的 Mockito 5.7.0 包含 Byte Buddy Agent；在 JDK 21 下 Mockito 初始化会自行 attach，产生动态 Agent 警告。项目未声明 `mockito-inline`，但 Mockito 5 默认 inline 机制仍会走该路径。
- 先验证了 subclass/proxy mock maker；它们不能阻止该版本 Mockito 初始化阶段的 attach。因此保持 Boot BOM 版本，改为 Surefire 的显式 `-javaagent`，路径由 `${settings.localRepository}`、BOM 的 `${byte-buddy.version}` 组成，不含用户绝对路径。
- Byte Buddy Agent 继续由 Boot 管理的 `mockito-core` 传递提供；Surefire 以同一 BOM 版本的工件启动。当前没有 JaCoCo，`argLine` 不需要合并。
- POM 同时设置 `java.version=21`、`maven.compiler.release=21`，使用 Enforcer 的 `[21,22)` 限制 Maven 运行 JDK，并显式 `proc=full` 消除 JDK 注解处理提示。
- `ChargingServiceTest` 改用构造器注入与 `UserContext.setUser/remove`，删除私有字段反射；泛型 matcher 改为类型推断，消除 unchecked 编译警告。未修改生产业务实现。

## 未采用项

- 未升级 Spring Boot、MyBatis-Plus、Flyway、MySQL 驱动、Lombok、Mockito 或 Byte Buddy：当前 BOM 版本在 JDK 21 实测可用，升级并非解决本问题的必要条件。
- 未启用虚拟线程、record、switch/pattern matching、sealed class 或任何 preview feature：未见与本次兼容性目标直接相关的收益。
- 未使用 `-XX:+EnableDynamicAgentLoading`、`-Djdk.attach.allowAttachSelf=true`、`--add-opens` 或跳过测试。

## 验证命令与结果

- 环境：OpenJDK/Javac 21.0.11，Maven 3.9.16；Maven 输出确认 Java 21.0.11。
- 修改前：`mvn clean test`、`mvn clean package` 为 47/47，但分别出现动态 Agent、release 17、注解处理和 unchecked 警告。
- 专项：`mvn clean test -Dtest=ChargingServiceTest` 为 15/15，`ResidentDashboardServiceTest` 为 3/3；无动态 Agent 警告。
- 全量：`mvn clean test` 为 47/47，`mvn clean package` 为 47/47 并产出可执行 jar；均显示 `javac [debug release 21]`、Enforcer Java 21 通过，无动态 Agent、非法反射或 module-access 错误。
- local Profile：通过 `scripts/dev-backend-wsl.sh` 在 Windows 原生 MySQL 9.7 启动；Flyway 成功校验 V1-V6，schema 当前版本 6、无 pending migration。Flyway 对 MySQL 9.7 给出“高于已测试 MySQL 8.0”的既有兼容性提醒，未修改数据库或迁移。
- 只读 HTTP：`/doc.html` 200、`/v3/api-docs` 200、`POST /api/auth/login` 200、无 Token `GET /api/auth/me` 401、管理员 `GET /api/auth/me`/`permissions`/`statistics/dashboard` 200、居民 `GET /api/resident/dashboard` 200。居民访问管理大屏返回项目既有 HTTP 200 + 响应体 `code:403`，权限语义保持不变。

- 数据库影响：无；未执行 clean、repair、baseline，未修改 V1-V6。
- 外部资产：无。
- 未解决事项：本地 MySQL 9.7 超出 Flyway 9.22.3 已测试的 MySQL 8.0 范围，是既有环境风险；本轮未升级无关依赖。
- 下一步：停止；不自动开始任何小程序、居民大屏或三级大屏任务。
