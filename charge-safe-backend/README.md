# ChargeSafe 后端服务 (charge-safe-backend)

> 《面向社区场景的电动自行车充电停放与安全运维管理系统》后端服务
> 统一输出 RESTful API，负责所有业务逻辑、鉴权、充电桩设备通信模拟、消息推送、统计计算、缓存与定时任务。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.2.5 |
| 语言 | Java | 17+（本机使用 JDK 21）|
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.x |
| 数据库迁移 | Flyway | 9.22.3 |
| 认证 | JWT (jjwt) | 0.12.5 |
| 密码加密 | Spring Security Crypto (BCrypt) | - |
| 缓存 | Caffeine（本地缓存）| 3.1.8 |
| 接口文档 | Knife4j (Swagger/OpenAPI3) | 4.4.0 |
| AOP | Spring AOP | - |

## 环境要求

- **JDK 17 或以上**（注意：本机默认 `JAVA_HOME` 指向 JDK 8，启动前必须切换到 JDK 17+）
- **Maven 3.8+**
- **MySQL 8.x**（本机使用 `C:\Program Files\MySQL\MySQL Server 8.0`）
- Maven 本地仓库已存在相关依赖（首次构建需联网下载）

## 快速开始

### 1. 准备数据库

```sql
CREATE DATABASE IF NOT EXISTS charge_safe_dev DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS 'charge_safe_app'@'%' IDENTIFIED BY 'cugb666';
GRANT ALL PRIVILEGES ON charge_safe_dev.* TO 'charge_safe_app'@'%';
FLUSH PRIVILEGES;
```

Flyway 会在应用启动时自动执行 `src/main/resources/db/migration` 下的迁移脚本（V1→V5），无需手动建表。

### 2. 启动（本地开发）

**方式一：双击启动脚本（推荐，已配好 JDK/Maven 路径）**

```powershell
cd charge-safe-backend
.\run-local.bat
```

> 首次运行前请用记事本打开 `run-local.bat`，确认 `JAVA_HOME` 指向你机器上 JDK 17+ 的实际安装路径（脚本里已列出常见路径，取消注释即可）。

**方式二：命令行启动**

```powershell
cd charge-safe-backend

# 必须指定 JDK 17+（本机示例路径）
$env:JAVA_HOME = "D:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
$env:PATH = "C:\tools\apache-maven-3.9.8\bin;" + $env:PATH

mvn spring-boot:run -Dspring-boot.run.profiles=local
```

启动成功后日志出现 `Started ChargeSafeApplication`，访问：

- 接口地址：`http://localhost:8080/api`
- API 文档（Knife4j）：`http://localhost:8080/doc.html`

### 3. 打包部署

```powershell
mvn clean package -DskipTests
java -jar target/charge-safe-backend-1.0.0.jar --spring.profiles.active=prod
```

### 常见启动问题

| 现象 | 原因 | 解决 |
|------|------|------|
| `无效的目标发行版: --release` | 默认 JAVA_HOME 指向 JDK 8 | 设置 JAVA_HOME 为 JDK 17+（run-local.bat 已内置配置，确认路径即可） |
| `Port 8080 was already in use` | 端口被占用 | 修改 run-local.bat 顶部 `PORT=8082`，或命令行加 `-Dspring-boot.run.arguments=--server.port=8082` |
| MySQL 连接失败 | MySQL 未启动/库不存在/密码错 | 确认 MySQL 服务运行中，且已执行上方建库 SQL |
| 依赖下载失败 | 网络/镜像配置 | 配置 Maven 阿里云镜像或检查代理 |

## 多环境配置

通过 Spring Profile 切换，配置文件位于 `src/main/resources/`：

| Profile | 文件 | 端口 | 数据库 |
|---------|------|------|--------|
| 本地开发 | `application-local.yml` | 8080 | `127.0.0.1:3306/charge_safe_dev` |
| 云端开发 | `application-dev.yml` | 8080 | `127.0.0.1:13306/charge_safe_dev`（需 SSH 隧道）|
| 正式演示 | `application-prod.yml` | 8081 | `127.0.0.1:3306/charge_safe`（环境变量注入）|

数据库敏感信息可通过环境变量覆盖：`CHARGE_SAFE_DB_HOST` / `CHARGE_SAFE_DB_PORT` / `CHARGE_SAFE_DB_NAME` / `CHARGE_SAFE_DB_USERNAME` / `CHARGE_SAFE_DB_PASSWORD`。

## 默认账号

| 角色 | 用户名 | 密码 | roleCode | 数据范围 |
|------|--------|------|----------|----------|
| 平台管理员 | admin | 123456 | PLATFORM_ADMIN | 全部社区 |
| 物业管理员 | property01 | 123456 | PROPERTY_ADMIN | 所属社区（community_id=1）|
| 运维人员 | maintainer01 | 123456 | MAINTAINER | 自己接单的工单 |
| 巡检人员 | inspector01 | 123456 | INSPECTOR | 自己分配的巡检任务 |
| 居民用户 | resident01 | 123456 | RESIDENT | 自己的预约/订单/消息 |

## 项目结构

```
com.chargesafe
├── ChargeSafeApplication.java          # 启动类（@EnableScheduling 已开启）
├── common/                              # 公共组件
│   ├── Result / ResultCode / PageResult / PageQuery   # 统一响应与分页
│   ├── BusinessException / GlobalExceptionHandler      # 业务异常与全局处理
│   ├── OperationLog / OperationLogAspect                # 操作日志注解 + AOP
│   └── OperationLogEntity / OperationLogMapper          # 操作日志表映射
├── config/                              # 配置
│   ├── MybatisPlusConfig.java           # MyBatis-Plus 分页插件
│   ├── SecurityConfig.java              # 注册 JWT 过滤器
│   ├── WebConfig.java                   # CORS 跨域配置
│   ├── CacheConfig.java                 # Caffeine 本地缓存（@EnableCaching）
│   └── OpenApiConfig.java               # Knife4j 文档元信息 + JWT 安全方案
├── security/                            # 安全与权限
│   ├── JwtUtils / JwtAuthenticationFilter              # JWT 生成/校验/过滤器
│   ├── LoginUser / UserContext                         # 登录用户模型 + ThreadLocal
│   ├── DataScope                                      # 数据权限（行级）判定工具
│   ├── RequiresPermission / PermissionAspect          # 接口级权限注解 + AOP 切面
└── modules/                             # 业务模块（controller/dto/entity/enums/mapper/service/vo）
    ├── auth/          # 认证：登录、当前用户、菜单、权限码
    ├── user/          # 用户/角色/菜单（RBAC）
    ├── community/     # 社区、车棚
    ├── device/        # 充电设备、端口
    ├── charging/      # 预约、充电、订单 + 定时任务（预约过期自动取消）
    ├── repair/        # 报修、维修工单
    ├── inspection/    # 巡检任务、安全隐患
    ├── notification/  # 消息通知
    ├── statistics/    # 统计分析（带缓存）
    ├── simulator/     # 设备状态模拟（无真实硬件）
    └── log/           # 操作日志查询
```

## 权限模型（RBAC）

系统采用**两层权限控制**：

### 1. 数据权限（行级）— `security/DataScope.java`

列表/分页查询按角色自动收窄数据范围，不依赖前端传参：

- **平台管理员**：可见全部社区数据
- **物业管理员**：仅所属社区（社区/车棚/设备/端口/报修/工单/隐患/预约/订单/统计）
- **运维人员**：工单仅自己接单的；其余按本社区
- **巡检人员**：巡检任务仅自己分配的；其余按本社区
- **居民用户**：预约/订单仅自己的；其余按本社区

### 2. 接口级权限（方法级）— `@RequiresPermission`

- 基于 `sys_menu.permission_code`，由 `PermissionAspect` 在 Controller 方法执行前校验
- 平台管理员权限码集合为 `null`，表示拥有全部权限
- 越权访问返回 `403`（FORBIDDEN）
- 居民自有接口（我的预约/我的订单/提交报修/充电/我的消息）不加管理权限码，保证小程序正常使用

权限码示例：`system:user:view`、`community:list:view`、`device:list:view`、`repair-ticket:view`、`statistics:analysis:view` 等，详见 `V3__add_menu_tables.sql`。

## 缓存

- 实现：`Caffeine` 本地缓存（`spring-context-support` + `caffeine`）
- 配置：`config/CacheConfig.java`，缓存名 `statistics`，最大 200 条，写后 5 分钟 TTL
- 统计模块 6 个接口标注 `@Cacheable`
- 改变端口状态的业务操作（开始/结束充电、取消/过期/故障取消预约）标注 `@CacheEvict(allEntries = true)`，主动失效保证统计及时刷新

## 定时任务

- `modules/charging/scheduler/ReservationExpireScheduler.java`
- `@Scheduled(cron = "0 * * * * ?")`：每分钟扫描处于 `PENDING` 且已过期的预约，自动取消并恢复端口为空闲、发送通知
- 启动类已加 `@EnableScheduling`

## 数据库迁移（Flyway）

迁移脚本位于 `src/main/resources/db/migration`，按版本号顺序执行，**禁止修改已执行的迁移文件**：

| 版本 | 文件 | 内容 |
|------|------|------|
| V1 | `V1__init_schema.sql` | 19 张业务表结构 |
| V2 | `V2__init_data.sql` | 角色、用户、用户角色关联等初始化数据 |
| V3 | `V3__add_menu_tables.sql` | `sys_menu` / `sys_role_menu` 表 + 菜单与角色权限绑定 |
| V4 | `V4__add_community_coordinates.sql` | 社区经纬度字段 + 补充演示小区 |
| V5 | `V5__adjust_property_admin_menus.sql` | 为物业管理员补充 `community:list:view` 权限 |

新增结构变更必须提交新的迁移文件（如 `V6__xxx.sql`），版本号不得与已有重复。

## 接口规范

- 统一响应：`{ "code": 200, "message": "success", "data": {} }`
- 认证：JWT，`Authorization: Bearer {token}`
- 分页参数：`page`（页码，默认 1）、`size`（每页条数，默认 10，最大 100）
- 分页返回：`{ "total", "page", "size", "pages", "records" }`
- 错误处理：业务异常通过 `BusinessException` + `GlobalExceptionHandler` 统一返回，非 200 表示失败

## 测试

```powershell
mvn test
```

现有单元测试（`src/test`）：

- `ChargingServiceTest`：预约冲突、状态流转等 15 个用例
- `MenuTreeBuilderTest`：菜单树构建 1 个用例

## 常见问题

**1. 编译报错 `无效的目标发行版: --release`**
原因：默认 `JAVA_HOME` 指向 JDK 8。解决：启动前将 `JAVA_HOME` 切换到 JDK 17+。

**2. 启动报 `Port 8080 was already in use`**
原因：端口被占用。解决：更换端口 `mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments=--server.port=8082`。

**3. 所有接口返回 401 未登录**
检查请求头 `Authorization: Bearer <token>` 是否正确；token 由 `/api/auth/login` 获取。注意 JWT 过滤器在设置用户上下文后再加载权限码。

**4. 前端/小程序无法访问接口**
确认 CORS 配置（`WebConfig.java`）与请求路径前缀 `/api`；生产环境经 Nginx 反向代理到 `/api`。

## 文档索引

- 系统总文档：`../README.md`
- 数据库设计：`../docs/design/database-design.md`
- API 接口详见运行后的 Knife4j 文档：`http://localhost:8080/doc.html`
