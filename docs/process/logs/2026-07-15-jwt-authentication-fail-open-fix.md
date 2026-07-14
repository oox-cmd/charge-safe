# 2026-07-15 开发记录：关闭 JWT 认证 fail-open 路径

- 基线提交：`05f7cdca8d98fc1bcc03d04213690718c9275def`
- 目标：修复自定义 JWT 认证过滤器在缺失或无效 Token 时继续放行的问题，并补充 HTTP 安全回归测试。
- 范围内文件：`JwtAuthenticationFilter.java`、`JwtAuthenticationFilterHttpTest.java` 及与该问题状态直接相关的中期审查、证据索引和路线图。
- 范围外事项：权限码统一、小程序、管理端、大屏、数据库、Flyway、依赖升级、Token 有效期和 JWT 密钥策略。

## Bug 表现与根因

修复前，过滤器仅在 Token 可验证时设置 `UserContext`。缺失 Token、Authorization 格式错误、无法解析、签名错误、过期或认证异常都不会生成认证失败响应；过滤器在 catch 后仍执行 `filterChain.doFilter`。因此，没有方法级权限注解且没有自行检查 `UserContext` 的 `/api/*` Controller 可能以匿名身份继续执行。

项目没有使用 Spring Security Web 的 `SecurityContext`、`AuthenticationEntryPoint` 或 `AccessDeniedHandler`，实际认证链是 `FilterRegistrationBean` 注册的 `JwtAuthenticationFilter`、线程本地 `UserContext` 和 `PermissionAspect`。修复必须落在这条真实链路上，不能假设不存在的 Spring Security 组件。

## 失败测试证据

先新增 `JwtAuthenticationFilterHttpTest`，使用 MockMvc、真实 `JwtUtils`、真实过滤器、真实 `PermissionAspect` 和测试 Controller 验证 HTTP 状态与统一响应结构。实现修改前定向测试结果为：12 项中 9 项失败、3 项通过；失败项均是预期 HTTP 401、实际 HTTP 200，真实复现了匿名继续进入 Controller 的 fail-open 行为。

覆盖场景：无 Token、Authorization 格式错误、无法解析、签名无效、过期、用户不存在、用户禁用、合法 Token、公开登录、认证与权限不足区分、旧上下文清理，以及错误响应不泄露 Token、密钥、密码或堆栈。

## 实际变更

- 请求开始和结束均清理 `UserContext`，避免线程复用残留。
- `POST /api/auth/login` 保持无 Token 可访问；没有扩大白名单。
- 其余 `/api/*` 缺失或格式错误 Authorization、无效 Token、用户不存在、用户禁用或认证异常时立即返回统一 HTTP 401 JSON，不再执行 Controller。
- 合法 Token 仍按原有方式加载用户、社区和权限；登录契约、权限码和权限切面未改变。
- 认证日志只记录拒绝原因/异常类别和请求路径，不记录 Token、JWT 密钥、密码或认证对象。
- 已认证但无业务权限继续由 `PermissionAspect` 与既有全局异常机制返回业务码 403；本次没有改变该 HTTP 200 + body code 403 的既有契约。

## 验证结果

环境：OpenJDK 21.0.11、Apache Maven 3.9.16。项目 Java 编译目标仍为 17，未修改 `pom.xml`。

| 验证 | 结果 |
| --- | --- |
| 修改前定向测试 | 12 项中 9 项按预期失败，实际 HTTP 200 复现 fail-open |
| 修改后定向测试 | 12/12 通过 |
| `mvn clean test` | 28/28 通过，0 failure、0 error |
| `mvn clean package` | 通过，生成后端 jar；构建产物未纳入 Git |
| Flyway/local Profile | 连接 Windows MySQL；6 条迁移验证成功，schema 为 V5，无 pending migration、未执行新迁移 |
| 公开只读入口 | `/doc.html`、`/v3/api-docs` 均 HTTP 200 |
| 认证运行态 | 受保护只读接口无 Token/错误 Token 均 HTTP 401 + body code 401；合法登录后 `/api/auth/me` 和 `/api/statistics/dashboard` 均 HTTP 200 + body code 200 |
| 权限不足区分 | HTTP 层自动化测试验证合法认证但权限不足仍返回既有 body code 403；运行态没有调用任何增删改接口来制造权限拒绝 |
| 敏感信息 | 验证输出和 401 响应未包含数据库密码、JWT_SECRET、完整 Token 或堆栈 |

完整测试仍出现 Mockito/Byte Buddy 动态 agent 的未来兼容性警告，但本次 JDK 21 环境未复现此前初始化失败；未升级依赖，也未修改 `pom.xml`。

## 数据库与外部资产

- 数据库影响：无；只执行应用启动时的 Flyway validate 和业务只读查询。
- Flyway：V1-V5 未修改，迁移历史未修改。
- 外部资产：无。

## 未解决事项与下一步

- 前后端权限码不一致仍是下一项 P1，本次没有修改权限定义、菜单或前端按钮。
- 当前权限不足沿用 HTTP 200 + body code 403 的既有业务响应；是否统一为 HTTP 403 应在独立契约任务中评估，不能混入本修复。
- 认证过滤器的公开端点清单目前只有登录；未来新增公开 `/api/*` 端点时必须显式评审并补安全测试。
- 下一步按路线图处理“权限码不一致”，不得自动扩展本任务范围。
