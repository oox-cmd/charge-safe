# ChargeSafe 调研记录汇总

> 本文件汇总了三位组员的调研记录，包含前端框架、可视化大屏、数据库设计、权限管理、工单流程、3D场景等多个方向的参考资料。

## 已拉取到本地的参考源码

所有参考源码位于 `docs/references/` 目录（已加入 .gitignore，不提交到仓库）。

### 完整拉取的项目

| 项目 | 本地目录 | 来源 | 调研人 | 说明 |
|------|---------|------|--------|------|
| vue-vben-admin | `vue-vben-admin/` | GitHub | - | Vue3 后台管理模板 |
| element-plus | `element-plus/` | GitHub | - | Element Plus 官方源码 |
| RuoYi-Vue | `RuoYi-Vue/` | GitHub | 金昊然 | RBAC 权限管理系统 |
| vue-element-plus-admin | `vue-element-plus-admin/` | GitHub | 吴诗颖 | Element Plus Admin 集成方案 |
| vue3-management-system | `vue3-management-system/` | GitHub | 吴诗颖 | 轻量级 Vue3 管理系统 |
| ev-charging-platform | `ev-charging-platform/` | GitHub | 吴诗颖 | 充电桩平台数据库设计 |
| es-big-screen | `es-big-screen/` | GitHub | 张健 | 大屏可视化模板 |
| IofTV-Screen-Vue3 | `IofTV-Screen-Vue3/` | GitHub | 张健 | 大数据可视化模板 |
| vue-data-visualization | `vue-data-visualization/` | GitHub | 张健 | 数据可视化编辑器 |
| smartPark-3D | `smartPark-3D/` | GitHub | 张健 | 智慧园区 3D 可视化 |
| 3d-datacenter | `3d-datacenter/` | GitHub | 张健 | 数据中心 3D 可视化 |
| Vue3Echarts | `Vue3Echarts/` | GitHub | 张健 | Vue3 ECharts 大屏 |
| EV Charging Station Management | `electric-vehicle-charging-station-management-system/` | GitHub | 金昊然 | 充电桩管理系统 |
| vue-echarts | `vue-echarts/` | GitHub | 金昊然 | Vue ECharts 封装 |
| DashBoard | `DashBoard/` | Gitee | 吴诗颖 | 仪表盘设计器 |

### 只拉取 README 的大型项目

| 项目 | 本地目录 | 来源 | 调研人 | 说明 |
|------|---------|------|--------|------|
| JeecgBoot | `JeecgBoot/` | GitHub | 金昊然 | 只有 README，项目太大 |
| Apache DolphinScheduler | `dolphinscheduler/` | GitHub | 金昊然 | 只有 README，项目太大 |
| OpenRemote | `openremote/` | GitHub | 金昊然 | 只有 README，项目太大 |
| BigDataView | `BigDataView/` | GitHub | 张健 | 只有 README，项目太大 |

**未拉取的非 GitHub 资料**（在线文档，无法本地拉取）：

| 资料 | 链接 | 来源 | 调研人 |
|------|------|------|--------|
| 绿能充电数据库设计 | https://juejin.cn/post/7638517197009092623 | 掘金 | 吴诗颖 |
| 充电站平台数据库应用 | https://cnevcharger.com/zh-CN/... | 专业网站 | 吴诗颖 |
| 物业报修系统 | https://blog.csdn.net/... | CSDN | 吴诗颖 |
| FeelDesk 工单系统 | https://www.feeldesk.cn/news/1066 | FeelDesk | 吴诗颖 |
| 禅道工单系统 | https://www.zentao.net/book/zentaopms/ticket-960.html | 禅道 | 吴诗颖 |
| 数字孪生智慧园区 | https://segmentfault.com/a/... | SegmentFault | 吴诗颖 |
| Three.js 智慧园区入门 | https://blog.csdn.net/... | CSDN | 吴诗颖 |
| GCPAAS DashBoard | https://gitee.com/gcpaas/DashBoard | Gitee | 吴诗颖 |
| 充电管理系统公众号 | https://mp.weixin.qq.com/... | 微信公众号 | 吴诗颖 |
| 微信小程序报修系统 | https://blog.csdn.net/... | CSDN | 吴诗颖 |
| Apache ECharts 官方示例 | https://echarts.apache.org/examples/ | 官网 | 张健 |
| 智慧园区 3D 场景 | https://threejs3d.com/examples/yuanquan | Three3D | 张健 |
| ThingJS 智慧园区 | https://www.thingjs.com/guide/intelligentCampus/ | ThingJS | 张健 |
| 图扑智慧充电桩 | https://www.hightopo.com/blog/3922.html | 图扑 | 张健 |
| 图扑光储充一体化 | https://www.hightopo.com/blog/4631.html | 图扑 | 张健 |
| 素材来源 | Pexels / Unsplash / Pixabay / Wikimedia Commons | 图片网站 | 张健 |
| 有来后台模板在线预览 | https://vue.youlai.tech | 在线演示 | 吴诗颖 |
| vue-element-plus-admin 文档 | https://element-plus-admin-doc.cn | 文档站 | 吴诗颖 |

---

# 一、吴诗颖 - 调研记录

## 资料 1：vue3-element-admin（有来开源后台管理系统

- **链接**：https://github.com/youlai-org/vue3-element-admin
- **来源**：GitHub
- **技术栈**：Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router + ECharts + Vue-i18n
- **这个资料主要讲什么**：一套完整的企业级后台管理系统前端模板，包含登录页、首页控制台、权限管理、动态路由、表格 CRUD、弹窗表单、接口文档等全套功能。配套 Java 后端（youlai-boot）。
- **我们可以借鉴什么**：
  - 左侧菜单设计：支持动态路由加载，菜单根据用户权限动态渲染，支持多级嵌套和图标配置。
  - 表格查询页面：基于 Element Plus 的 el-table + el-pagination，支持多条件筛选、自定义列、批量操作。
  - 新增/编辑弹窗：使用 el-dialog 封装表单，支持表单校验、重置、提交 loading。
  - 首页仪表盘：集成 ECharts 图表，展示数据统计卡片 + 折线图/饼图。
  - 登录权限：JWT Token + RBAC 角色权限控制，菜单权限、按钮权限双重控制。
- **对应 ChargeSafe 哪个模块**：后台首页、左侧菜单、表格查询、弹窗表单、登录权限。
- **我觉得是否值得参考**：值得（强烈推荐作为基础框架）
- **备注或截图说明**：该项目是 Element Plus 官方推荐的 Vue3 后台模板，文档完善，社区活跃，mini 分支适合二次开发。在线预览：https://vue.youlai.tech

## 资料 2：vue-element-plus-admin（Element Plus Admin 集成方案）

- **链接**：https://github.com/kailong321200875/vue-element-plus-admin
- **来源**：GitHub
- **技术栈**：Vue 3 + Vite + TypeScript + Element Plus + Pinia + UnoCSS
- **这个资料主要讲什么**：一个功能更丰富的后台集成方案，比 vue3-element-admin 多了更多开箱即用的功能（如表单设计器、图表大屏、工作流等），但代码冗余也更多。
- **我们可以借鉴什么**：
  - 后台首页布局：顶部导航栏 + 左侧菜单 + 右侧内容区的经典布局，支持标签页（TagsView）多页签切换。
  - 表格查询：高级查询组件封装，支持快速筛选、导出 Excel、列拖拽排序。
  - 弹窗设计：抽屉（Drawer）和 Dialog 两种编辑模式，支持表单分步（Step Form）。
  - 暗黑模式：一键切换主题，对运维平台夜间值班场景很友好。
- **对应 ChargeSafe 哪个模块**：后台整体布局、表格查询、弹窗交互、主题切换。
- **我觉得是否值得参考**：值得（参考其布局设计和交互细节，但建议用 mini 分支或 vue3-element-admin 做基础）
- **备注或截图说明**：文档地址：https://element-plus-admin-doc.cn，适合学习其组件封装思路。

## 资料 3：HearLing Vue3 管理系统（轻量级学习项目）

- **链接**：https://github.com/HearLing/vue3-management-system
- **来源**：GitHub / 博客
- **技术栈**：Vue 3 + TypeScript + Vite + Pinia + Element Plus + ECharts + Axios
- **这个资料主要讲什么**：一个适合学习 Vue3 的轻量级后台管理系统，代码简洁，功能覆盖登录、首页、表格、图表等基础模块。
- **我们可以借鉴什么**：
  - 首页仪表盘：简单的数据卡片 + ECharts 折线图/饼图组合，适合快速搭建 ChargeSafe 的首页看板原型。
  - 表格页面：基础的增删改查表格，代码量少，易于理解。
  - 弹窗表单：基础的 Dialog + Form 组合，适合新手参考。
- **对应 ChargeSafe 哪个模块**：后台首页、表格查询、弹窗。
- **我觉得是否值得参考**：一般（代码简洁但功能较基础，适合学习而非直接复用）
- **备注或截图说明**：博客介绍：https://hearling.github.io/posts/30403/，适合快速理解 Vue3 + Element Plus 的后台开发模式。

## 资料 4：绿能充电 — 6 张表搞定充电桩平台数据库设计

- **链接**：https://juejin.cn/post/7638517197009092623
- **来源**：掘金
- **技术栈**：SQLite + better-sqlite3（前端视角设计）
- **这个资料主要讲什么**：一位前端开发者从零设计的充电桩平台数据库，包含 6 张核心表：用户表、充电站表、充电会话表、支付记录表、预约表、故障告警表，并详细说明了表关系、索引设计和踩坑经验。
- **我们可以借鉴什么**：
  - 数据库表设计：6 张表的结构可以直接参考，尤其是 charging_sessions（充电订单）、fault_alerts（故障告警）、reservations（预约）三张表。
  - 字段设计：status 状态字段的枚举值设计（如 active/frozen、online/offline/maintenance）。
  - 索引设计：登录字段（phone）、外键字段必须建索引的经验。
  - 金额存储：必须用 DECIMAL(10,2) 而非 float。
  - 冗余字段：stations.available_ports 作为冗余字段提升性能。
- **对应 ChargeSafe 哪个模块**：数据库设计（用户、站点、订单、预约、告警）。
- **我觉得是否值得参考**：值得（设计思路清晰，适合中小型项目快速启动）
- **备注或截图说明**：作者还提供了完整的 SQL 建表脚本，可以直接复制使用。项目地址：https://github.com/Rhi637/ev-charging-platform

## 资料 5：充电站平台数据库应用（企业级架构）

- **链接**：https://cnevcharger.com/zh-CN/application-of-charging-station-platform-database/
- **来源**：专业充电设备网站
- **技术栈**：MySQL + MyBatis + Spring Boot（企业级）
- **这个资料主要讲什么**：企业级充电站平台的数据库架构设计，涵盖资产管理、用户会员、交易结算、运维监控、营销推广五大模块的核心表设计。
- **我们可以借鉴什么**：
  - 资产管理模块："运营商-站点-充电桩-充电枪" 四级树状层级结构，便于权限控制。
  - 用户会员系统：多钱包机制（不同运营商下独立余额），支持微信 OpenID/UnionID 绑定。
  - 运维监控：告警表、消息日志表、故障记录表的设计，支持 OCPP 协议通信消息存储。
  - 交易结算：充电订单完整记录（启动→充电→完成→支付），支持实时电量/金额计算。
- **对应 ChargeSafe 哪个模块**：数据库设计（全模块）、资产层级、交易结算、运维监控。
- **我觉得是否值得参考**：值得（企业级视角，表设计更完整，适合 ChargeSafe 扩展）
- **备注或截图说明**：该文章从商业平台角度讲解，对"多租户"和"分账结算"的设计很有参考价值。

## 资料 6：基于 SpringBoot 的物业报修系统（权限设计参考）

- **链接**：https://blog.csdn.net/2502_92355795/article/details/152319470
- **来源**：CSDN
- **技术栈**：Spring Boot + Vue + MyBatis-Plus + Spring Security
- **这个资料主要讲什么**：小区物业报修系统的完整设计与实现，重点包括 Spring Security 权限控制、角色分级（管理员/维修人员/业主）。
- **我们可以借鉴什么**：
  - 权限设计：Spring Security + JWT 实现角色权限控制，不同角色看到不同菜单和功能。
  - 用户状态机：账户状态（可用/异常/冻结/注销）的状态流转设计。
  - 报修流程：业主提交→系统派单→维修人员处理→业主确认评价，完整的闭环流程。
- **对应 ChargeSafe 哪个模块**：登录权限、用户管理、报修工单。
- **我觉得是否值得参考**：值得（Spring Security 权限设计是 Java 后台的标准方案）
- **备注或截图说明**：虽然页面普通，但后端权限设计思路清晰，适合 ChargeSafe 的后台权限模块参考。

## 资料 7：FeelDesk 工单系统搭建指南

- **链接**：https://www.feeldesk.cn/news/1066
- **来源**：FeelDesk 官网
- **技术栈**：通用工单系统设计方法论
- **这个资料主要讲什么**：从零搭建工单系统的完整步骤，包括需求分析、技术架构、工作流自动化、SLA 设定、测试上线等。
- **我们可以借鉴什么**：
  - 工单状态流程：创建→指派→处理中→已处理→关闭/激活，支持转 Bug/需求。
  - 自动化规则：自动分配、升级规则、超时提醒（催单机制）。
  - SLA 设计：服务等级协议，超时未处理自动升级。
  - 多渠道接入：支持 Web、小程序、电话等多渠道提交工单。
- **对应 ChargeSafe 哪个模块**：报修工单、运维工单。
- **我觉得是否值得参考**：值得（工单系统的通用设计方法论，不限技术栈）
- **备注或截图说明**：禅道工单（资料 8）的状态流转设计（创建→指派→处理中→完成→关闭→激活）可以直接参考。

## 资料 8：禅道工单系统（状态机设计）

- **链接**：https://www.zentao.net/book/zentaopms/ticket-960.html
- **来源**：禅道官网
- **技术栈**：项目管理 / 工单系统
- **这个资料主要讲什么**：禅道项目管理软件中的工单模块，详细说明了工单的生命周期状态流转和通知机制。
- **我们可以借鉴什么**：
  - 工单状态机：创建→指派→开始处理（处理中）→完成→关闭→激活，共 6 个状态。
  - 通知机制：创建/编辑/指派/激活 通知指派人员；开始/完成/关闭/激活 通知创建者。
  - 关闭原因：不予处理、重复提交等关闭原因分类。
  - 转需求：复杂工单可转 Bug 或研发需求继续跟踪。
- **对应 ChargeSafe 哪个模块**：报修工单、运维工单。
- **我觉得是否值得参考**：值得（状态机设计清晰，通知机制完善）
- **备注或截图说明**：禅道是国内主流项目管理工具，其工单流程经过大量企业验证，可靠性高。

## 资料 9：数字孪生智慧园区 IBMS 三维可视化运维项目

- **链接**：https://segmentfault.com/a/1190000042593421
- **来源**：SegmentFault / 掘金
- **技术栈**：ECharts + Three.js + WebGL
- **这个资料主要讲什么**：一个完整的智慧园区 3D 可视化运维项目，包含园区总览、园区态势、设备设施、安防管理四大模块，支持 3D 漫游、电子围栏、设备告警。
- **我们可以借鉴什么**：
  - 3D 园区场景：Three.js 构建园区 3D 模型，动态展示车流、人流、设备运行状态。
  - ECharts 数据看板：园区总览页面集成设备告警、车流统计、能耗数据等图表。
  - 设备监控：点击 3D 模型中的设备可查看实时状态和告警信息。
  - 安防管理：电子围栏、安防预警、通信管控、停车管理。
  - 漫游功能：3D 场景中的自动漫游和手动浏览。
- **对应 ChargeSafe 哪个模块**：3D 充电桩场景可视化、ECharts 数据大屏、设备监控。
- **我觉得是否值得参考**：值得（3D 可视化 + 数据看板的结合方案非常契合 ChargeSafe 的运维监控需求）
- **备注或截图说明**：掘金原文：https://juejin.cn/post/7152749012221689864，包含大量效果图和实现思路。

## 资料 10：Three.js 智慧园区 3D 可视化入门

- **链接**：https://blog.csdn.net/weixin_29255731/article/details/157377025
- **来源**：CSDN
- **技术栈**：Three.js + WebGL
- **这个资料主要讲什么**：Three.js 构建智慧园区 3D 可视化的完整入门教程，从场景创建、模型加载、光照渲染到动态交互。
- **我们可以借鉴什么**：
  - 3D 场景搭建：基础场景创建（Scene、Camera、Renderer）代码。
  - 模型加载：支持 glTF/GLB 格式模型加载，适合导入充电桩 3D 模型。
  - 动态交互：车辆轨迹跟踪、设备状态动态呈现、点击事件绑定。
  - 性能优化：模型轻量化、LOD（细节层次）优化。
- **对应 ChargeSafe 哪个模块**：3D 充电桩场景展示。
- **我觉得是否值得参考**：值得（Three.js 基础教程，适合快速上手 3D 可视化）
- **备注或截图说明**：适合 ChargeSafe 的"充电桩 3D 监控"模块，可以展示充电桩实时状态、故障告警高亮等。

## 资料 11：GCPAAS DashBoard 仪表盘设计器

- **链接**：https://gitee.com/gcpaas/DashBoard
- **来源**：Gitee
- **技术栈**：Vue 2 + Element UI + G2Plot + ECharts
- **这个资料主要讲什么**：一个开源的仪表盘设计器，支持拖拽式布局、多种图表组件、数据绑定、预览导出。
- **我们可以借鉴什么**：
  - 仪表盘布局：拖拽式卡片布局，支持自由调整图表位置和大小。
  - 图表组件：文本、图片、轮播表、排名表、翻牌器、折线图、柱状图、饼图等。
  - 数据绑定：支持静态数据和动态 API 数据绑定。
  - 导出功能：支持导出为图片或嵌入第三方系统。
- **对应 ChargeSafe 哪个模块**：ECharts 数据看板、首页仪表盘。
- **我觉得是否值得参考**：值得（仪表盘设计器的思路可以直接借鉴到 ChargeSafe 的大屏设计）
- **备注或截图说明**：虽然是 Vue2 项目，但设计思路完全适用于 Vue3 + Element Plus。

## 资料 12：GitHub 开源充电管理系统（鸿蒙 + 小程序 + 云平台）

- **链接**：https://mp.weixin.qq.com/s?__biz=Mzg2MDgxNTE1Ng==&mid=2247484905（微信公众号文章）
- **来源**：微信公众号 / GitHub
- **技术栈**：鸿蒙 App（ArkTS + ArkUI）+ 小程序（uni-app）+ 云平台（Vue3 + Element Plus）+ Go + SQLite
- **这个资料主要讲什么**：一套包含鸿蒙 App、微信小程序、云平台的三端充电设备管理系统，功能涵盖充电站列表、充电桩状态、在线充电、订单查询、个人中心等。
- **我们可以借鉴什么**：
  - 小程序首页：充电站列表展示、附近站点地图、距离信息。
  - 充电桩状态：桩编号、状态（可用/占用/故障）的 UI 展示。
  - 订单管理：历史充电记录列表，实时查看订单状态。
  - 个人中心：头像上传、昵称修改、余额充值、退出登录。
  - 多租户：支持不同机构独立管理充电站点。
- **对应 ChargeSafe 哪个模块**：小程序首页、充电桩状态、订单列表、个人中心。
- **我觉得是否值得参考**：值得（技术栈与 ChargeSafe 高度吻合，uni-app 小程序可以直接参考）
- **备注或截图说明**：这是目前找到的与 ChargeSafe 技术栈最匹配的开源项目，建议重点研究其小程序端设计。

## 资料 13：基于微信小程序的报修系统（Uni-app）

- **链接**：https://blog.csdn.net/whirlwind526/article/details/131442697
- **来源**：CSDN
- **技术栈**：uni-app + Spring Boot + MyBatis-Plus
- **这个资料主要讲什么**：基于微信小程序的校园报修管理系统，包含报修提交、工单分配、进度跟踪、评价反馈等功能。
- **我们可以借鉴什么**：
  - 报修页面：故障类型选择、位置描述、图片上传的表单设计。
  - 工单进度：报修记录列表，展示处理状态（待处理/处理中/已完成）。
  - 消息通知：工单状态变更时推送微信通知。
  - 地图定位：自动定位故障位置，支持手动选择。
- **对应 ChargeSafe 哪个模块**：小程序报修页面、消息通知。
- **我觉得是否值得参考**：值得（报修流程与 ChargeSafe 的故障报修模块高度相似）
- **备注或截图说明**：虽然是校园场景，但报修流程（提交→派单→处理→评价）完全适用于充电桩故障报修。

---

# 二、张健 - 调研记录

## 负责方向

可视化大屏 + 3D 场景方向。

本次调研不是直接写代码，也不是照搬别人的项目，而是为 ChargeSafe 后续页面优化、数据大屏、设备监控和 3D 场景提供参考。重点关注这些问题：

1. 大屏页面怎么布局；
2. 图表怎么展示端口、订单、工单、隐患和告警；
3. 充电设备状态怎么用颜色、标签、闪烁、弹窗表现；
4. 3D 场景能不能改造成社区、车棚、充电桩、电动车；
5. 哪些效果适合我们项目第一阶段落地，哪些适合后续扩展。

## 当前项目中可直接对接的内容

根据项目文档和现有代码，ChargeSafe 已经具备做可视化大屏的基础数据：

| ChargeSafe 数据或模块 | 可视化方式 | 可参考效果 |
|---|---|---|
| 端口总数、空闲端口、充电中、故障端口 | 顶部数字卡片、环形图、端口状态矩阵 | 数字翻牌、状态色块 |
| 今日预约、今日订单 | 趋势折线图、柱状图、滚动列表 | 订单趋势、实时预约动态 |
| 维修工单、待处理工单 | 告警列表、状态漏斗、工单滚动表 | 工单派发动态 |
| 安全隐患、待整改隐患 | 风险等级图、隐患列表、闪烁提示 | 隐患预警 |
| `alarm_record` 告警记录 | 告警中心、故障排行、红色闪烁点位 | 设备告警大屏 |
| 社区、车棚、设备、端口层级 | 2D 平面图或 3D 场景 | 社区-车棚-充电桩-端口 |
| 端口状态 `IDLE / RESERVED / CHARGING / FAULT / MAINTENANCE / DISABLED / OFFLINE` | 不同颜色和图标 | 设备点位状态 |

我筛选资料时优先选这几类：

1. 和 ChargeSafe 技术栈接近：Vue3、Vite、ECharts、Element Plus、Three.js；
2. 能看出页面布局或源码结构；
3. 能改造成社区充电场景；
4. 有截图、演示或 README，便于组长快速判断；
5. 不只看炫酷程度，更看是否能对应我们的模块和数据。

---

## 资料 1：Vue3 ECharts 大屏数据可视化

- 链接：https://github.com/IT-NuanxinPro/Vue3Echarts
- 来源：GitHub
- 技术栈：Vue 3、Vite、ECharts 5、Element Plus、Pinia、Axios、GSAP、Sass
- 这个资料主要讲什么：
  一个基于 Vue 3 + ECharts 5 的现代化大屏数据可视化项目，包含大屏页面结构、左右中布局、多个图表组件、响应式适配和动画效果。项目 README 明确提到大屏适配、响应式布局、多种 ECharts 图表、实时数据和组件化结构。
- 我们可以借鉴什么：
  可以借鉴它的三栏大屏布局，把 ChargeSafe 当前普通工作台改成"左侧统计 + 中间总览 + 右侧告警/趋势"的可视化大屏。左侧可以放端口状态、车棚利用率；中间放社区或车棚概况；右侧放订单趋势、工单/隐患滚动列表。
- 对应 ChargeSafe 哪个模块：
  统计分析模块、工作台首页、端口状态统计、订单统计、工单和隐患统计。
- 我觉得是否值得参考：值得。
- 备注或截图说明：
  README 有预览图和项目结构，已保存截图如下：

![Vue3Echarts 大屏布局参考](张健images/images/zhangjian-vue3-echarts-01.png)

## 资料 2：es-big-screen 大屏可视化项目模板

- 链接：https://github.com/vangleer/es-big-screen
- 来源：GitHub
- 技术栈：Vue 3、ECharts、高德地图、Pinia
- 这个资料主要讲什么：
  一个基于 Vue3 + ECharts + 高德地图 + Pinia 的大屏可视化模板。它提供大屏适配、ECharts 图表封装、高德地图组件封装、拖拽布局、入场动画和滚动效果。
- 我们可以借鉴什么：
  可以借鉴它的大屏适配方式、图表组件封装和滚动列表效果。ChargeSafe 后续可以把"最新报修、最新工单、最新告警、最新预约"做成右侧动态滚动列表，把"社区/车棚分布"扩展成地图或平面图。
- 对应 ChargeSafe 哪个模块：
  统计分析模块、消息通知模块、告警记录、预约记录、充电订单、设备管理。
- 我觉得是否值得参考：值得。
- 备注或截图说明：
  适合重点看大屏适配、图表封装、拖拽布局和滚动列表部分，已保存效果图如下：

![es-big-screen 大屏滚动与布局参考](张健images/images/zhangjian-es-big-screen-01.gif)

## 资料 3：IofTV-Screen-Vue3 大数据可视化模板

- 链接：https://github.com/daidaibg/IofTV-Screen-Vue3
- 来源：GitHub
- 技术栈：Vue 3、Vite、ECharts
- 这个资料主要讲什么：
  一个 Vue3 + Vite + ECharts 的大数据可视化模板，包含自适应组件、滚动配置、数字滚动、中间地图、无缝轮播等大屏常见效果。
- 我们可以借鉴什么：
  数字滚动可以用于"端口总数、今日预约、今日订单、故障端口"；胶囊柱图可以用于"各车棚端口利用率"；轮播列表可以用于"实时告警/最新工单/最新报修"。它比完整低代码平台轻，适合参考具体组件。
- 对应 ChargeSafe 哪个模块：
  工作台首页、统计分析模块、端口管理、报修工单、巡检隐患、消息通知。
- 我觉得是否值得参考：值得。
- 备注或截图说明：
  README 有组件说明，已保存截图如下：

![IofTV-Screen-Vue3 大屏组件参考](张健images/images/zhangjian-ioftv-screen-01.png)

## 资料 4：vue-data-visualization 数据可视化大屏设计与编辑器

- 链接：https://github.com/miyuesc/vue-data-visualization
- 来源：GitHub
- 技术栈：Vue 3、ECharts 5、Element Plus、JavaScript
- 这个资料主要讲什么：
  一个通用数据可视化大屏设计和编辑器项目，主要使用 Vue3、ECharts5 和 Element Plus，支持元素拖拽、缩放和通用大屏配置。
- 我们可以借鉴什么：
  ChargeSafe 不需要做完整大屏编辑器，但可以借鉴它的组件化思想：数字卡片、环形图、折线图、告警列表、状态面板都拆成独立组件。这样后续替换后端接口或调整布局会更方便。
- 对应 ChargeSafe 哪个模块：
  统计分析模块、可视化大屏页面、图表组件封装。
- 我觉得是否值得参考：值得。
- 备注或截图说明：
  更适合看"图表组件怎么拆分"和"画布布局怎么组织"，不建议照搬完整编辑器。已保存截图如下：

![vue-data-visualization 组件化大屏编辑参考](张健images/images/zhangjian-data-visualization-01.png)

## 资料 5：BigDataView 大屏模板合集

- 链接：https://github.com/iGaoWei/BigDataView
- 来源：GitHub
- 技术栈：HTML、CSS、JavaScript、ECharts
- 这个资料主要讲什么：
  一个大屏 HTML5 模板合集，包含社区、物业、政务、交通、金融银行等行业的大屏模板。
- 我们可以借鉴什么：
  虽然它不一定是 Vue3 项目，但适合快速参考大屏视觉样式、标题栏、边框、图表排列、科技风背景、社区/物业类模板。ChargeSafe 可以从里面选"社区、物业、设备监控"相近模板，提取布局和配色思路，而不是复制代码。
- 对应 ChargeSafe 哪个模块：
  可视化大屏、社区概况、物业运维、设备监控。
- 我觉得是否值得参考：一般到值得。
- 备注或截图说明：
  适合作为视觉风格库，但要注意模板质量参差不齐。已保存示意图如下：

![BigDataView 大屏模板参考](张健images/images/zhangjian-bigdataview-01.svg)

## 资料 6：Apache ECharts 官方示例

- 链接：https://echarts.apache.org/examples/en/index.html
- 来源：Apache ECharts 官方
- 技术栈：ECharts
- 这个资料主要讲什么：
  ECharts 官方示例库，包含折线图、柱状图、饼图、散点图、地图、雷达图等多种图表类型。ECharts 官方说明中也提到它提供多种图表类型和组件组合能力。
- 我们可以借鉴什么：
  ChargeSafe 已经使用 ECharts，所以图表样式最好优先从官方示例里选。比如：端口状态用环形图，订单趋势用折线图，车棚利用率用横向柱状图，隐患等级用玫瑰图，角色或模块占比用饼图。
- 对应 ChargeSafe 哪个模块：
  统计分析模块、工作台图表、后续大屏图表。
- 我觉得是否值得参考：值得。
- 备注或截图说明：
  官方示例可作为图表配置参考。该资料主要用于查具体图表配置，暂未单独保存截图。

## 资料 7：threejs-park 智慧园区 3D 可视化系统

- 链接：https://github.com/zenitlab/smartPark-3D
- 来源：GitHub
- 技术栈：Vue 3、Three.js、Vite、GSAP、Sass、JavaScript
- 这个资料主要讲什么：
  一个基于 Three.js 的智慧园区 3D 可视化系统，可以展示园区建筑、车辆、绿化与设备，并支持楼层拆解、轨迹巡游、视频投放、视角切换、CSS2D 标签、模型加载等能力。
- 我们可以借鉴什么：
  可以把它的"智慧园区"思路改成"社区充电站/车棚 3D 场景"。例如：楼层拆解可以改成车棚区域切换；车辆轨迹可以改成电动车进出和停放；CSS2D 标签可以显示充电桩编号、端口状态、故障状态；视频投放可以改成监控画面或宣传屏。
- 对应 ChargeSafe 哪个模块：
  社区资源管理、车棚管理、设备管理、端口管理、设备监控大屏。
- 我觉得是否值得参考：非常值得，尤其适合 3D 场景方向。
- 备注或截图说明：
  README 里有场景展示和楼层分体图，已保存截图如下：

![smartPark-3D 智慧园区场景参考](张健images/images/zhangjian-smartpark-3d-01.png)

## 资料 8：智慧园区 3D 前端场景

- 链接：https://threejs3d.com/examples/yuanquan
- 来源：Three3D 小站 / 文章
- 技术栈：Vue、Three.js
- 这个资料主要讲什么：
  介绍一个 Three.js + Vue 的智慧园区 3D 可视化系统，包含模型加载、场景交互、楼层管理、电力监测、水利监测、超标警告等内容。
- 我们可以借鉴什么：
  这个资料的"电力监测"和"超标警告"非常适合 ChargeSafe。可以把电力监测改成充电功率、端口状态、故障告警；把楼层/房间标签改成车棚/充电桩/端口标签；点击设备后显示端口详情、当前预约、充电订单或故障信息。
- 对应 ChargeSafe 哪个模块：
  设备管理、端口管理、充电订单、告警记录、统计分析。
- 我觉得是否值得参考：值得。
- 备注或截图说明：
  文章里有功能介绍和图片，适合补充 3D 设备监控思路。该页面图片暂未稳定下载，后续可手动补截图。

## 资料 9：ThingJS 智慧园区可视化

- 链接：https://www.thingjs.com/guide/intelligentCampus/
- 来源：ThingJS 官网
- 技术栈：ThingJS、数字孪生、3D 可视化、低代码/零代码搭建
- 这个资料主要讲什么：
  ThingJS 的智慧园区数字孪生方案，强调对园区建筑结构和设备位置进行真实还原，并集成能耗指标、设备状态、楼控系统等数据，实现可视化监控预警。
- 我们可以借鉴什么：
  可以借鉴它的"园区-建筑-楼层-设备"的层级展示方式，改成 ChargeSafe 的"社区-车棚-充电设备-充电端口"。设备点位可以用不同颜色表示空闲、充电中、故障、离线、维修中。
- 对应 ChargeSafe 哪个模块：
  社区资源模块、车棚管理、设备管理、端口状态、告警记录。
- 我觉得是否值得参考：值得，但更适合作为效果和方案参考，不一定直接引入 ThingJS。
- 备注或截图说明：
  官网有产品效果图和场景说明。该页面图片暂未稳定下载，后续可手动补截图。

## 资料 10：图扑智慧充电桩可视化运营平台

- 链接：https://www.hightopo.com/blog/3922.html
- 来源：图扑软件博客
- 技术栈：HT for Web、2D/3D 可视化、数字孪生、数据大屏
- 这个资料主要讲什么：
  一个专门面向新能源充电桩的可视化运营平台案例，展示充电桩预约时长、已充电次数、停车场用户、空闲电桩、日充功率统计和充电功率变化趋势等信息。
- 我们可以借鉴什么：
  这是和 ChargeSafe 业务最接近的资料。可以参考它的大屏指标设计，例如：空闲端口数量、充电中端口数量、故障端口数量、今日预约、今日订单、功率趋势、设备状态分布、告警列表。
- 对应 ChargeSafe 哪个模块：
  预约充电、端口管理、充电订单、设备模拟、告警记录、统计分析。
- 我觉得是否值得参考：非常值得。
- 备注或截图说明：
  这篇文章适合作为"我们为什么要做充电桩可视化"的业务参考。已保存效果图如下：

![图扑智慧充电桩运营大屏参考](张健images/images/zhangjian-hightopo-charging-01.gif)

## 资料 11：图扑数字孪生"光储充"一体化智慧充电站

- 链接：https://www.hightopo.com/blog/4631.html
- 来源：图扑软件博客
- 技术栈：HT for Web、3D 可视化、数字孪生、能源监控
- 这个资料主要讲什么：
  一个光伏、储能、充电站结合的数字孪生案例，展示储能充电站运行状态、储能电池、充换电流程、设备状态和实时充放电数据。
- 我们可以借鉴什么：
  ChargeSafe 不做真实光伏和储能，但可以借鉴它的"3D 场景 + 两侧 2D 数据面板"的表达方式。中间展示充电站或车棚场景，两侧展示端口状态、功率趋势、订单趋势、告警列表。
- 对应 ChargeSafe 哪个模块：
  可视化大屏、端口管理、设备监控、订单统计、告警记录。
- 我觉得是否值得参考：值得。
- 备注或截图说明：
  适合作为 3D + 大屏结合的效果参考。已保存效果图如下：

![图扑光储充一体化大屏参考](张健images/images/zhangjian-hightopo-storage-charging-01.gif)
  
## 资料 12：3d-datacenter 数据中心 3D 可视化管理系统

- 链接：https://github.com/go-laoji/3d-datacenter
- 来源：GitHub
- 技术栈：React、Three.js、React Three Fiber、Ant Design Pro、TypeScript
- 这个资料主要讲什么：
  一个基于 Three.js 的数据中心 3D 可视化管理系统，提供 3D 设备展示、设备状态、环境监控、告警中心等能力。
- 我们可以借鉴什么：
  虽然它不是 Vue 项目，但设备状态展示很有参考价值。ChargeSafe 可以把"机柜/服务器"换成"充电桩/充电端口"，用颜色表示 `IDLE`、`CHARGING`、`FAULT`、`OFFLINE`，用闪烁或高亮表示故障告警，用点击详情展示端口编号、功率、当前订单、报修状态。
- 对应 ChargeSafe 哪个模块：
  设备管理、端口管理、告警记录、设备模拟、报修工单。
- 我觉得是否值得参考：值得，主要参考设备状态和告警交互。
- 备注或截图说明：
  README 有仪表板、环境监控、告警中心和 3D 视图说明，已保存截图如下：

![3d-datacenter 设备状态与告警中心参考](张健images/images/zhangjian-3d-datacenter-01.png)

---

## 技术路线对比

| 方案 | 适合做什么 | 优点 | 风险或不足 | 对 ChargeSafe 的建议 |
|---|---|---|---|---|
| Vue3 + ECharts | 统计大屏、图表、趋势、状态分布 | 当前项目已经使用，落地最快 | 视觉上偏 2D，缺少沉浸式场景 | 第一阶段优先做 |
| Vue3 + ECharts + DataV 类组件 | 科技风边框、数字翻牌、滚动表 | 大屏效果明显 | 组件质量要筛选，可能有兼容问题 | 可借鉴样式和组件 |
| Vue3 + Three.js | 3D 车棚、充电桩、设备点位 | 自由度高，适合项目展示 | 模型、性能、交互复杂度较高 | 第二阶段做简化 3D |
| ThingJS / HT for Web | 数字孪生、园区级 3D 平台 | 效果成熟，业务表达强 | 商业平台，未必适合直接接入 | 主要参考效果和方案 |
| 静态 HTML 大屏模板 | 视觉参考、页面布局参考 | 找样式快 | 技术栈不一致，代码不一定好维护 | 只参考布局和配色 |

---

## 我建议 ChargeSafe 大屏第一版这样设计

页面名称定为：`社区充电安全态势大屏`。

视觉设计建议从原来的 **Cyberpunk 暗夜霓虹** 修正为 **克制的社区充电安全运维风**。原因是 ChargeSafe 的重点是端口状态、故障告警、维修工单、巡检隐患和统计分析，过多霓虹背景会抢走业务数据的注意力，也容易显得像通用模板。

### 页面布局结构

```text
┌────────────────────────────────────────────────────────────┐
│ ChargeSafe 社区充电安全态势大屏      时间 / 社区 / 运维角色 │
├───────────────┬────────────────────────────┬───────────────┤
│ 左侧图表       │ 中间主视觉                  │ 右侧列表       │
│ ┌───────────┐ │ ┌────────────────────────┐ │ ┌───────────┐ │
│ │端口状态环图│ │ │ 车棚 / 设备 / 端口矩阵  │ │ │实时告警    │ │
│ ├───────────┤ │ └────────────────────────┘ │ ├───────────┤ │
│ │车棚利用率  │ │ ┌────────────────────────┐ │ │工单隐患    │ │
│ └───────────┘ │ │ 许可清楚的充电设备小图   │ │ │滚动列表    │ │
│               │ └────────────────────────┘ │ └───────────┘ │
├───────────────┴────────────────────────────┴───────────────┤
│ 底部面板：今日预约 / 订单趋势、故障变化趋势、消息概览       │
└────────────────────────────────────────────────────────────┘
```

### 视觉配色规范

| 用途 | 颜色 | 说明 |
|---|---|---|
| 页面背景 | `#07111f` | 深蓝黑，突出数据 |
| 面板背景 | `rgba(8, 18, 32, 0.86)` | 稳定承载图表 |
| 主要高亮 | `#2f80ed` | 标题、选中态、关键线条 |
| 辅助高亮 | `#38bdf8` | 少量边框和数据强调 |
| 空闲端口 | `#22c55e` | 对应 `IDLE` |
| 充电中 | `#f59e0b` | 对应 `CHARGING` |
| 已预约 | `#3b82f6` | 对应 `RESERVED` |
| 故障 | `#ef4444` | 对应 `FAULT` |
| 维修 / 离线 | `#94a3b8` | 对应 `MAINTENANCE` / `OFFLINE` |

### 数据对接模块

| 大屏区域 | 对应后端 API / 数据表 | 第一版表现形式 |
|---|---|---|
| 顶部统计指标 | `/api/statistics/dashboard` | 清晰数字卡片，少量动效 |
| 端口分布比例 | `/api/ports/statistics` | ECharts 环形图，颜色按状态语义映射 |
| 端口状态矩阵 | `/api/ports` | 车棚 / 设备 / 端口层级状态格子 |
| 实时告警滚动 | `alarm_record` 或模拟告警数据 | 右侧滚动列表，故障项红色强调 |
| 工单与隐患 | `/api/repair-tickets` / `/api/hazards` | 状态列表、数量卡片、进度概览 |
| 预约 / 订单趋势 | `/api/orders` 后续统计或 mock | ECharts 折线图 |

### 素材使用方向

大屏素材不再建议使用纯霓虹边框和赛博背景。更合适的素材包括：

1. 真实充电接口 close-up；
2. 社区车棚或充电区域；
3. 电动自行车充电站图片；
4. 少量充电过程短视频；
5. 自制 CSS / ECharts 端口状态矩阵。

---

## 3D 场景改造思路

3D 建议放在第二阶段，且第一版不要依赖真实模型素材：

1. 先用简单几何体表达"车棚 + 充电设备 + 端口灯"，端口灯颜色映射后端状态；
2. `FAULT` 状态可以做低频红色闪烁，但不要整屏高频闪烁；
3. 点击端口弹出详情卡片，展示编号、状态、当前预约、工单或隐患信息；
4. 后续再考虑 GLB / GLTF 模型，模型进入项目前必须确认来源和许可；
5. ThingJS、HT for Web 这类商业可视化案例只作为效果参考，不直接引入其素材或代码。

---

## 我的总结

1. 我觉得大屏整体布局可以参考：
   `Vue3Echarts`、`es-big-screen`、`IofTV-Screen-Vue3`。这几个都适合把 ChargeSafe 当前普通工作台升级成"顶部标题 + 左中右三栏 + 多图表 + 滚动告警"的大屏结构。

2. 我觉得 3D 场景可以参考：
   `threejs-park 智慧园区 3D 可视化系统` 和 `ThingJS 智慧园区可视化`。它们的园区建筑、设备点位、标签、视角切换、模型加载方式都可以改造成"社区充电站/车棚/充电桩"场景。

3. 我觉得图表样式可以参考：
   `Apache ECharts 官方示例`、`IofTV-Screen-Vue3` 和 `vue-data-visualization`。端口状态适合环形图，订单趋势适合折线图，车棚利用率适合柱状图，告警和工单适合滚动列表。

4. 我觉得告警/设备状态展示可以参考：
   `图扑智慧充电桩可视化运营平台`、`图扑光储充一体化智慧充电站` 和 `3d-datacenter`。前两个业务最接近 ChargeSafe，后者的设备状态颜色、告警高亮、设备详情交互可以迁移到充电端口监控。

5. 我认为最值得组长重点看的 3 个链接是：
   - https://www.hightopo.com/blog/3922.html
   - https://github.com/zenitlab/smartPark-3D
   - https://github.com/vangleer/es-big-screen

---

## 对 ChargeSafe 后续实现的建议

1. 第一阶段先做独立的 `可视化大屏` 页面，不要一开始就上复杂 3D。
2. 大屏第一版直接复用现有后端统计字段：端口总数、空闲端口、充电中、故障端口、今日预约、今日订单、待处理工单、待整改隐患、未读消息。
3. 第一版图表建议做：
   - 端口状态环形图；
   - 今日预约/订单趋势折线图；
   - 各车棚端口利用率柱状图；
   - 告警/工单/隐患滚动列表；
   - 故障端口排行。
4. 第二阶段再做 3D 场景：先用简单模型表达车棚和充电桩，用颜色标记端口状态，再加入点击详情、告警闪烁、视角切换。
5. 当前项目已有 Vue3、Vite、Element Plus 和 ECharts，落地成本最低的是 ECharts 大屏；Three.js 和 ThingJS 更适合后续展示效果增强。

---

# 三、金昊然 - 调研报告

## 资料1：RuoYi-Vue（RBAC 权限管理）

- **GitHub：** https://github.com/yangzongzhuan/RuoYi-Vue
- **演示：** https://vue.ruoyi.vip
- **技术栈：** Spring Boot + Spring Security + JWT + Vue2 + MyBatis
- **借鉴模块：** 登录、RBAC 权限、菜单管理、日志管理

## 资料2：JeecgBoot（企业级 RBAC + 工作流）

- **GitHub：** https://github.com/jeecgboot/JeecgBoot
- **演示：** https://boot3.jeecg.com
- **技术栈：** Spring Boot 3 + Vue3 + MyBatis-Plus + JWT
- **借鉴模块：**
  - 用户管理
  - 角色管理
  - 数据权限
  - 工作流
  - 在线表单
  - 系统日志

重点查看：

```
sys_user
sys_role
sys_permission
sys_depart
```

⭐⭐⭐⭐⭐

## 资料3：Apache DolphinScheduler（工单/任务流转参考）

> 虽然不是物业系统，但它的**任务流转、状态管理、审批思想**非常值得借鉴。

- **GitHub：** https://github.com/apache/dolphinscheduler
- **演示：** https://dolphinscheduler.apache.org/
- **技术栈：** Java + Spring Boot + Vue

可以借鉴：

- 工单状态设计
- 流程流转
- 操作记录
- 审批流程
- 日志记录

对应 ChargeSafe：

```
工单 → 派单 → 处理 → 完成
```

⭐⭐⭐⭐⭐

## 资料4：OpenRemote（巡检 + 设备运维）

这是国外非常成熟的 IoT 运维平台。

- **GitHub：** https://github.com/openremote/openremote
- **官网：** https://openremote.io
- **技术栈：** Java, Spring Boot, Docker, MQTT, Vue

可以学习：

- 设备管理
- 告警
- 巡检
- 运维
- 状态监控

对应 ChargeSafe：

```
设备 → 巡检 → 发现故障 → 派维修 → 完成
```

⭐⭐⭐⭐⭐

## 资料5：Electric Vehicle Charging Station Management System（充电桩管理）

- **GitHub：** https://github.com/rezaep/electric-vehicle-charging-station-management-system
- **技术栈：** Spring Boot, Hibernate, PostgreSQL, Docker

可以借鉴：

- 充电站
- 充电桩
- 地图定位
- 设备管理
- 数据模型

对应 ChargeSafe：

```
Station → Pile → Port → Charging
```

⭐⭐⭐⭐⭐

## 资料6：vue-echarts（后台数据大屏）

虽然它不是后台系统，但是**国内很多 SpringBoot+Vue 项目都是基于它做数据驾驶舱**。

- **GitHub：** https://github.com/ecomfe/vue-echarts
- **官方 Demo：** https://vue-echarts.dev
- **技术栈：** Vue3, ECharts, TypeScript

可以借鉴：

- 首页驾驶舱
- 实时图表
- 柱状图
- 折线图
- 饼图
- 仪表盘

对应 ChargeSafe：

```
首页驾驶舱 → 设备统计 → 订单统计 → 故障统计 → 收入统计
```

⭐⭐⭐⭐⭐

---

## 这套资料的优势

| 项目 | GitHub | 对应模块 | 推荐指数 |
|---|---|---|---|
| RuoYi-Vue | ✅ | 权限管理 | ⭐⭐⭐⭐⭐ |
| JeecgBoot | ✅ | RBAC、工作流 | ⭐⭐⭐⭐⭐ |
| Apache DolphinScheduler | ✅ | 工单流程、状态流转 | ⭐⭐⭐⭐⭐ |
| OpenRemote | ✅ | 巡检、设备运维 | ⭐⭐⭐⭐⭐ |
| EV Charging Station Management System | ✅ | 充电桩、设备管理 | ⭐⭐⭐⭐⭐ |
| vue-echarts | ✅ | ECharts 数据驾驶舱 | ⭐⭐⭐⭐⭐ |

## 金昊然的总结建议

1. **后端技术架构**：ChargeSafe 后端建议采用 Spring Boot + Spring Security + JWT + MyBatis-Plus 的技术架构，参考 RuoYi-Vue 和 JeecgBoot 的整体设计思路。接口按照功能模块进行划分，如用户管理、角色权限、设备管理、充电管理、预约管理、订单管理、报修工单、巡检管理、数据统计等，每个模块分别包含 Controller、Service、Mapper 和 Entity，方便后续维护与扩展。同时可借鉴其统一返回结果、异常处理、日志记录和权限认证机制，提高系统的规范性和可维护性。

2. **数据库设计**：数据库设计可以综合多个项目的优点，采用模块化设计。
   - 系统基础表可参考 RuoYi-Vue 的 sys_user、sys_role、sys_menu、sys_user_role、sys_role_menu 等权限相关表。
   - 设备管理部分可参考充电桩管理系统，设计 device（设备表）、device_port（端口表）、device_alarm（告警表）、charge_order（充电订单表）、reservation（预约表）等。
   - 工单管理可参考物业报修系统，设计 repair_order（报修工单）、repair_record（处理记录）、repair_image（图片附件）等数据表。
   - 巡检模块可参考巡检管理系统，设计 inspection_plan（巡检计划）、inspection_record（巡检记录）、hazard（隐患记录）、rectification（整改记录）等表，使整个数据库结构更加清晰，便于后续业务扩展。

3. **工单流程设计**：建议采用完整的工单流转机制，既方便管理员跟踪处理进度，也便于后期统计分析。

建议流程如下：

```
用户提交报修 → 管理员审核 → 派发维修工单 → 维修人员接单 → 现场维修处理 → 提交处理结果 → 管理员验收 → 工单完成
```

整个过程中建议记录每一步的处理时间、处理人员、备注信息及图片附件，并增加工单流转记录表，用于保存所有历史操作，方便后续查询和责任追溯。

4. **权限管理设计**：建议采用 RBAC（基于角色的访问控制）模型，实现用户、角色和权限之间的解耦。

不同角色拥有不同的菜单权限和操作权限，例如：
- 超级管理员：拥有系统全部权限，可管理用户、设备、工单、巡检和数据统计等功能。
- 社区管理员：负责社区设备、预约、订单及工单管理。
- 巡检人员：负责巡检任务执行、隐患上报和整改确认。
- 维修人员：负责接收维修工单、处理设备故障并反馈维修结果。
- 普通用户：可预约充电、扫码充电、提交报修、查看订单及维修进度。

同时建议增加操作日志、登录日志和数据权限控制，确保系统操作可追溯，提高平台安全性。

5. **最值得重点看的 3 个链接**：
   - RuoYi-Vue：https://github.com/yangzongzhuan/RuoYi-Vue
     推荐原因：权限管理、数据库设计和后台架构成熟，适合作为整个后台系统的基础框架。
   - JeecgBoot：https://github.com/jeecgboot/JeecgBoot
     推荐原因：企业级后台框架，RBAC 权限体系完善，工作流和数据权限设计值得参考。
   - Electric Vehicle Charging Station Management System：https://github.com/rezaep/electric-vehicle-charging-station-management-system
     推荐原因：与 ChargeSafe 业务最接近，可重点参考充电站、充电桩、设备管理、端口管理及订单管理等业务模型。
