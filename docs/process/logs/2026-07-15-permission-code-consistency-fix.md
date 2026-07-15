# 2026-07-15 开发记录：统一跨层权限码

- 基线提交：`2dc5f2cae24358290b402ccbd522710d21955af5`
- 目标：统一 Controller 权限注解、V3 菜单权限码和管理端按钮判断，保持既有角色访问边界。
- 范围内文件：认证权限查询、管理端权限加载/判断、引用错误权限码的业务页面、跨层契约与 HTTP 测试、相关中期文档。
- 范围外事项：RBAC 重构、补齐所有接口权限注解、小程序、大屏、UI、数据库结构、Flyway、依赖升级和 JWT 逻辑。

## 问题表现与根因

后端 21 个 `@RequiresPermission` 权限码全部存在于 V3 `sys_menu.permission_code`。不一致发生在两个边界：

1. 管理端 11 个页面共引用 30 个 Controller 和 V3 均不存在的动作码，例如 `add/edit/toggle-status/reset-pwd`、`repair:ticket:*` 和 `inspection:*`。
2. `/api/auth/permissions` 从 `listCurrentUserMenus()` 生成结果，而该菜单查询明确排除了 `menu_type=3` 的按钮，因此平台管理员拿不到系统 CRUD 按钮码。
3. `usePermission.js` 在权限为空或请求失败时启用本地角色通配符矩阵，使用另一套前缀和通配符语义，掩盖前两项问题，并可能显示后端不会接受的操作。

## 修改前权限码矩阵与规范选择

| 模块/操作 | 修改前管理端码 | Controller/V3 规范码 |
| --- | --- | --- |
| 用户新增、编辑、启停、重置 | `system:user:add/edit/toggle-status/reset-pwd` | `system:user:create/update/status/reset-password` |
| 角色新增、编辑 | `system:role:add/edit` | `system:role:create/update` |
| 菜单新增、编辑 | `system:menu:add/edit` | `system:menu:create/update` |
| 社区新增、编辑、启停 | `community:add/edit/toggle-status` | `community:list:view` |
| 车棚新增、编辑、删除 | `parking-area:add/edit/delete` | `parking-area:view` |
| 设备新增、编辑、启停 | `device:add/edit/toggle-status` | `device:list:view` |
| 端口状态更新 | `port:update-status` | `port:view` |
| 报修派单 | `repair:report:dispatch` | `repair-ticket:view` |
| 工单接单、完成、验收、驳回、关闭 | `repair:ticket:*` 各动作 | `repair-ticket:view` |
| 巡检创建、开始、完成 | `inspection:task:*` | `inspection-task:view` |
| 隐患整改、复查、关闭 | `inspection:hazard:*` | `hazard:view` |

规范码没有重新命名：接口安全边界以 Controller 注解为准，数据库通过 V3 中同名 `permission_code` 分发，管理端只精确消费后端返回集合。非系统业务继续沿用现有页面级码保护写操作，以避免新增权限、修改角色绑定或扩大/缩小角色访问范围。

## RED 证据

生产实现修改前执行：

```bash
mvn -Dtest=PermissionCodeConsistencyTest,AuthControllerPermissionsHttpTest test
```

结果为 4 项测试中 3 项失败、1 项通过：跨层测试准确列出 30 个管理端孤儿权限码；辅助测试发现本地角色矩阵；HTTP 测试期望首个按钮码 `system:user:create`，实际只收到页面码 `system:user:view`。同时，“全部 Controller 权限码存在于 V3”测试通过，证明不需要修改迁移数据。

## 修复方式

- `/api/auth/permissions` 改用 JWT 过滤器同样调用的 `listPermissionCodesByUserId`，排序后返回完整页面与按钮权限码。
- 管理端 30 个权限引用改为对应 Controller/V3 现有码。
- 移除本地角色权限矩阵和 `:*` 通配符匹配；权限为空或请求失败时按钮权限 fail-closed。
- 登录、刷新用户和带持久化角色的页面刷新都会在渲染权限按钮前从后端获取权限，避免异步加载导致指令提前移除节点。
- 未修改 `PermissionAspect`、JWT 过滤器、V1-V5、角色菜单关系或任何业务接口。

## 验证结果

环境：OpenJDK 21.0.11、Apache Maven 3.9.16、管理端 Vite 8.1.2。

| 验证 | 结果 |
| --- | --- |
| 权限定向测试 | 4/4 通过 |
| 权限 + JWT HTTP 回归 | 16/16 通过；无权限仍为既有 body code 403，无效 JWT 仍为 HTTP 401 |
| `mvn clean test` | 32/32 通过，0 failure、0 error |
| `mvn clean package` | 通过 |
| 管理端 `npm ci` | 103 packages，0 vulnerability |
| 管理端 `npm run build` | 通过；保留既有 Lottie `eval` 和依赖注释警告 |
| Flyway/local Profile | Windows MySQL 连接成功；6 条迁移验证成功，schema V5，无新迁移 |
| 五角色权限 | PLATFORM_ADMIN/PROPERTY_ADMIN/MAINTAINER/INSPECTOR/RESIDENT 分别返回 39/22/5/6/0 个权限码，关键允许与拒绝集合符合 V3/V5 |
| 只读接口 | 管理员、物业、运维、巡检、居民各选一个 GET 均 HTTP 200；错误 JWT 为 HTTP 401 |

生产 Controller 没有带 `@RequiresPermission` 的只读 GET。为遵守只读限制，没有调用增删改接口制造运行态 403；合法认证但缺权限的拒绝由真实 MockMvc HTTP 测试覆盖。验证使用当前进程生成的短期 JWT，不调用带操作日志的登录接口，不输出 Token、JWT_SECRET 或数据库凭据。

## 数据库与迁移

- 不需要 V6：规范码已存在于 V3，实际 Windows MySQL 角色权限数与 V3/V5 一致。
- V1-V5、`flyway_schema_history`、`sys_menu` 和 `sys_role_menu` 均未修改。
- H2 初始化没有维护菜单权限数据，本次未把 H2 配置描述为可用的 RBAC 运行事实。

## 未处理事项与下一步

- 多个菜单查看码没有对应 Controller 方法级注解、部分只读接口只依赖 JWT；这是权限覆盖评审，不属于现有码命名修复，未擅自改变角色访问边界。
- 管理端菜单表单的 `permission` 与后端 `permissionCode` 字段契约仍需独立联调。
- 路由角色矩阵和大屏匿名路由未在本轮修改。
- 下一项 P1 是“小程序 Mock 显式开关”；本轮未开始小程序工作。
