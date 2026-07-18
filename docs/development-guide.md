# ChargeSafe 开发与联调指南

## 1. 事实与变更边界

- 结构唯一来源为 `charge-safe-backend/src/main/resources/db/migration/`；已发布的 V1–V5 不得修改，禁止手改 `flyway_schema_history`。
- 接口与权限唯一来源为 Controller、DTO、`@RequiresPermission` 与 Flyway 菜单迁移；前端菜单不构成安全边界。
- 本地开发使用 Windows 原生 MySQL、JDK 21、Maven、Node.js 与 npm，不引入 Docker 或 H2 Profile。
- Mock、静态或合成数据必须在页面明确标识，不能表述为实时业务或真实硬件数据。

## 2. 本地启动与验证

环境变量 `CHARGE_SAFE_DB_USERNAME`、`CHARGE_SAFE_DB_PASSWORD`、`JWT_SECRET` 必须存在。具体建库、账号和启动命令以根目录 [`README.md`](../README.md) 为准。

最小验证顺序：

1. `mvn clean package`（JDK 21）。
2. 启动 `local` Profile，让 Flyway 自动校验并执行 pending migration。
3. 以演示角色登录，确认 `/api/auth/me` 和权限接口。
4. 对本次修改对应的只读或可清理写入流程执行 HTTP/浏览器联调。
5. 前端修改后分别运行管理端和小程序的单测、构建，并检查浏览器控制台。

## 3. 数据与权限约束

- 社区、车棚、设备、端口、报修、预约、订单必须从 `UserContext` 和服务端数据域限定，不能信任客户端提交的社区范围。
- 居民只访问本人和所属社区数据；物业、运维、巡检分别按社区或被分配对象限定。
- 管理端大屏保留认证与 `statistics:bigscreen:view`；居民看板使用独立 `resident:dashboard:view`，两者不得绕过认证或混用数据范围。
- 部门与设备分类删除前必须检查引用；不得用页面禁用按钮替代服务端约束。

## 4. 三维与外部资产

- 三级大屏固定采用原生 Three.js + `three-globe`，Vue 只处理页面状态和生命周期。
- 场景显式绑定 `communityId`、`stationId`、`deviceId`、`portId`；模型节点名不是业务 ID。
- MVP 场景为示意性低多边形布局，不伪装为 GIS 或测绘数据；前端坐标与真实经纬度分离。
- 外部代码、模型、纹理与 HDRI 在入库前必须登记来源与许可证；优先 CC0，代码与资产许可证分别核验。

## 5. 文档规则

- 当前事实写入 [`summary.md`](summary.md)，当前待办写入 [`current-backlog.md`](current-backlog.md)。
- 一个实施批次可新增 `process/logs/YYYY-MM-DD-*.md`，只记录实际变更、验证与边界。
- 调研、旧审计、设计草案和完成计划移入 [`archive/`](archive/)，不得继续作为当前操作说明。
