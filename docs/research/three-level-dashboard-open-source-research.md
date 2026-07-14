# ChargeSafe 三级三维大屏开源方案与免费资产调研

> 调研日期：2026-07-14
> 约束：本轮未向仓库引入代码、模型或依赖；许可证与资产适用性必须在实际下载前再次核验。

## 1. 需求边界

三级场景分别为 `globe`（地球/区域）、`community`（小区与站点）、`station`（车棚与端口）。目标是近似位置、自然转场、真实业务状态和稳定演示，不是测绘级 GIS，也不应展示与项目无关的全球连线或假数据。

当前仓库只有 ECharts/Lottie 二维大屏，没有 Three.js、MapLibre、Cesium、模型、场景状态机或三维数据接口。因此所有三维内容均属于下一阶段候选，不是已完成功能。

## 2. 技术路线比较

| 方案 | 优点 | 缺点 | 依赖体积 | 学习成本 | 场景统一性 | 地理精度 | Vue 适配 | 推荐程度 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| A. 原生 Three.js + three-globe | 一套渲染与资源生命周期；社区/站点自由度高；不依赖地图服务 | 地图、道路、建筑需程序化或自备数据；相机/清理需自行治理 | 中 | 中 | 高 | 近似 | Vue 管页面与生命周期，Three 独立封装 | **MVP 已采用** |
| B. MapLibre GL JS + Three.js | 官方支持 globe、建筑挤出和 Three 自定义 3D layer；真实点位扩展好 | 双重坐标/相机/GL 状态复杂；底图来源、离线与署名要治理 | 中高 | 中高 | 中 | 高 | 可直接集成，Vue wrapper 非必需 | 条件推荐 |
| C. CesiumJS + Three.js | WGS84、地形、3D Tiles、海量 GIS 能力成熟 | 体积与概念成本高；常见示例依赖 Cesium ion Token；当前需求过度设计 | 高 | 高 | 中低 | 很高 | 可集成但维护成本高 | 当前不推荐 |

MapLibre 官方已有“globe 中添加 Three.js 模型”示例和 `CustomLayerInterface`；这证明 B 可行，但不代表当前必须引入地图引擎。CesiumJS 本身 Apache-2.0，但 Cesium ion 内容与 Token 是另一层服务边界，不能混同。

## 3. GitHub 与官方实现候选

| 项目 | 仓库 | 维护情况（2026-07-14） | License | 可借鉴 | 不适用/风险 | 成本 | 建议 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Three.js | https://github.com/mrdoob/three.js | 活跃，官方发布持续 | MIT | 渲染、GLTFLoader、CSS2D、Raycaster、InstancedMesh、LOD | 需自行封装 Vue 生命周期 | 中 | 核心采用 |
| three-globe | https://github.com/vasturiano/three-globe | 活跃仓库，问题与提交持续 | MIT | 可作为 Three 场景内 Globe Object，便于统一三层 | 内置图层很多，需克制只用点/标签 | 低中 | 一级首选原型 |
| Globe.GL | https://github.com/vasturiano/globe.gl | 活跃 | MIT | 独立 Globe UI，快速镜头与标点 | 自带 renderer/control，嵌入统一场景不如 three-globe | 低 | 仅作快速原型参考，不作为正式依赖 |
| MapLibre GL JS | https://github.com/maplibre/maplibre-gl-js | 活跃、社区治理 | BSD-3-Clause | globe、矢量瓦片、3D 建筑、Three custom layer | 底图服务/署名/离线资源另行确认 | 中高 | 当前不采用；真实 GIS 需求成立后复评 |
| CesiumJS | https://github.com/CesiumGS/cesium | 活跃 | Apache-2.0 | 地球、地形、3D Tiles、真实坐标 | 当前近似场景不值得其复杂度 | 高 | 后期 GIS 才复评 |
| TresJS | https://github.com/Tresjs/tres | 活跃，2026 有发布 | MIT | Vue 声明式 Three 场景与生态 | 增加抽象层；复杂资源释放仍需懂 Three | 中 | 当前不引入；MVP 后按实际复杂度复评 |
| camera-controls | https://github.com/yomotsu/camera-controls | 活跃 | MIT | `fitToSphere`、平滑飞行、可等待转场 | 新依赖；需锁定与 Three 兼容版本 | 低 | 推荐演示版候选 |
| postprocessing | https://github.com/pmndrs/postprocessing | 活跃，2026 有发布 | Zlib | Outline/选择高亮、抗锯齿 | 容易滥用 bloom/glitch；增加 render pass | 中 | 仅选取 Outline/SMAA |
| three-mesh-bvh | https://github.com/gkjohnson/three-mesh-bvh | 活跃 | MIT | 大模型 raycast 加速 | MVP 小场景无必要；动态几何需重建/更新 | 中 | 性能验证后再引入 |
| glTF Sample Assets | https://github.com/KhronosGroup/glTF-Sample-Assets | 活跃策展 | **逐资产不同** | 验证加载器、压缩与材质能力 | 仓库层说明不能替代每个模型许可证 | 低 | 只作测试资产源 |
| Three.js-City | https://github.com/mauriciopoppe/Three.js-City | 老旧，Chrome 27 时代 | Apache-2.0（代码）；资产来源分散 | 程序化网格城市思路 | jQuery/AngularJS/旧 Three，资产许可链不适合复用 | 高 | 不采用，仅历史思路 |
| three.ar.js | https://github.com/google-ar/three.ar.js | 2022 已归档 | Apache-2.0 | 无 | 依赖已废弃 WebAR 路线，与需求无关 | 高 | 不采用 |

结论：不复制任何“智慧园区整套项目”。可复用的是官方 API、镜头/选择/资源清理模式，以及许可证清晰的小型库。

## 4. 状态交互实现证据

- Three.js `GLTFLoader` 可接 DRACO、KTX2 和 Meshopt 解码器：用于压缩后的按层懒加载。
- `Raycaster` 用于端口/设备拾取；端口数量较大时优先 `InstancedMesh`，业务 ID 存在实例映射表中。
- CSS2D/Sprite 只显示当前选择、告警和近景端口，避免数百标签造成 draw call 与遮挡。
- `postprocessing` 的 Outline 可做选择反馈；故障不应靠全屏 bloom 或闪烁。
- Three.js 官方明确要求显式 `dispose()` texture/geometry/material；切换三级场景时必须纳入资源管理器。
- MapLibre custom layer 需要处理 `webglcontextlost/restored` 和 `onRemove`，进一步说明双引擎路线的治理成本。

## 5. 免费模型与素材候选

### 5.1 可直接进入后续筛选的 CC0 候选

| 名称 | 来源/页面 | 格式 | License/作者 | 署名/再分发 | 大小/面数/贴图 | 层级 | 修改需求 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| City Kit (Roads) | https://www.kenney.nl/assets/city-kit-roads | 多种 3D 格式 | CC0 / Kenney | 不要求/允许 | 70 个文件；页面未披露体积面数 | community | 转 glTF、合批、统一材质 |
| City Kit (Suburban) | https://www.kenney.nl/assets/city-kit-suburban | 多种 3D 格式 | CC0 / Kenney | 不要求/允许 | 40 个文件；待下载复核 | community | 选少量住宅模块 |
| City Kit (Industrial) | https://www.kenney.nl/assets/city-kit-industrial | 多种 3D 格式 | CC0 / Kenney | 不要求/允许 | 25 个文件；待下载复核 | community/station | 选配电/仓储轮廓 |
| Nature Kit | https://kenney.nl/assets/nature-kit | 多种 3D 格式 | CC0 / Kenney | 不要求/允许 | 330 个文件；待下载复核 | community | 只选 3-5 种树并实例化 |
| Utility Box 01 | https://polyhaven.com/a/utility_box_01 | glTF 等 | CC0 / James Ray Cock | 不要求/允许 | 51.71 MB、约 4K tris、最高 8K 贴图 | station | 下载低分辨率、压缩贴图 |
| Security Camera 01 | https://polyhaven.com/a/security_camera_01 | glTF 等 | CC0 / Alexander Otterbeck、Yann Kervran | 不要求/允许 | 23.84 MB、14K tris、最高 8K | station | 降贴图、保留可转节点 |
| Korean Fire Extinguisher 01 | https://polyhaven.com/a/korean_fire_extinguisher_01 | glTF 等 | CC0 / UM JOORIN | 不要求/允许 | 364.09 MB、10K tris、最高 8K | station | 原包过大，只取低清并重打包 |
| Street Lamp 01 | https://polyhaven.com/a/street_lamp_01 | glTF 等 | CC0 / Josh Dean | 不要求/允许 | 31.41 MB、31K tris、最高 8K | community | 可用 Kenney 简模替代，或减面 |
| Monochrome Studio 01 HDRI | https://polyhaven.com/a/monochrome_studio_01 | HDR/JPG | CC0 / Grzegorz Wronkowski | 不要求/允许 | 完整下载 80.89 MB；可选低分辨率 | station | 转小尺寸环境贴图 |
| Box Vertex Colors | Khronos glTF Sample Assets 对应目录 | GLB/glTF | CC0 / Marco Hutter | 不要求/允许 | 极小测试模型；以目录 README 为准 | 技术验证 | 仅验证加载，不作正式美术 |

Poly Haven 官方许可页明确其资产为 CC0、允许商用和再分发；但网页缩略图、站点文字与 logo 不等同于 CC0 资产，不能一并抓取。

### 5.2 需要署名和用户确认的 CC BY 候选

| 名称 | 页面 | License/作者 | 大小与面数 | 适用性 | 结论 |
| --- | --- | --- | --- | --- | --- |
| Electric charging station | https://sketchfab.com/3d-models/electric-charging-station-a11e4043c6224aff98efac4097103466 | CC BY / cedric4296 | 9.4K tris、5K vertices，文件大小下载前复核 | 单柜接近，体量可控 | 值得用户确认署名后试用 |
| Bicycle Parking lot A3B2 | https://sketchfab.com/3d-models/bicycle-parking-lot-a3b2-027a84a5a80d460589c8c6ae36a5fb74 | CC BY / SUGINOKO | 386 tris、520 vertices | 低面自行车架 | 推荐候选，保留 NoAI 元数据要求 |
| Bicycle Game Asset | https://sketchfab.com/3d-models/bicycle-game-asset-571b8d5c48f54831abc61dbb0088cad4 | CC BY / RayznGames | 17.1K tris、9.1K vertices | 可作车辆点缀 | 需减材质/实例化 |
| New energy vehicle charging station | https://sketchfab.com/3d-models/new-energy-vehicle-charging-station-1930fe67b1d84a3a9dbcf4cb50c58a8f | CC BY / MrdT | 201.7K tris、82.4K vertices | 是汽车站，不符合电动自行车站尺度 | 仅构图参考，不优先下载 |

### 5.3 明确不采用的候选

| 候选 | 原因 |
| --- | --- |
| AI 生成的 Electric vehicle charging station（Sketchfab/Macheratus） | 虽标 CC BY，但 119.7K tris、来源为生成式工具，风格/权利链和优化成本均不优 |
| 433.5K tris 的 EV Charging Station Pl | 对当前站点层过重且是汽车充电设备 |
| 487K tris 的 EnBW 摄影测量站 | 商标/摄影测量噪声/体量不适合课程项目 |
| 许可证未知的“智慧园区完整模型/网盘包” | 无法形成再分发证据链 |

## 6. 是否找到完整充电站模型

没有找到同时满足“电动自行车车棚、端口可拆分、低面数、CC0、网页适配”的完整模型。找到的完整站多面向汽车充电、面数偏高或需要 CC BY 署名。推荐不等待完整模型，采用：

1. `PlaneGeometry` 地面与道路；
2. Box/Cylinder 程序化车棚柱、顶棚和充电柜；
3. 一个低面端口组件通过 `InstancedMesh` 重复；
4. Kenney 社区/道路/绿化模块；
5. Poly Haven 的配电箱、摄像头、消防器材只作为近景点缀；
6. CSS2D/Sprite/Outline 表达状态与选择。

## 7. 三套候选方案

| 方案 | 技术栈 | 免费素材策略 | 开发量 | 风险 | 视觉 | 数据要求 | 推荐 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MVP | Three.js + three-globe + ECharts；程序化 community/station | Kenney CC0 + 几何体，不引 CC BY | 中 | 低 | 清晰但简化 | 现有 ID/状态 + 前端坐标配置 | 高，先做 |
| 推荐演示版 | Three.js + three-globe + camera-controls + GLTFLoader + ECharts | Kenney + 精选 Poly Haven 低清；可选 1-2 个经确认的 CC BY | 中高 | 中 | 有自然飞入与单站特写 | 新的站点/端口聚合 DTO | **最高** |
| 后期增强 | MapLibre + Three.js 或在真实 GIS 需求下复评 Cesium | 优化 glTF、KTX2/Draco、可配置场景 | 高 | 高 | 最强 | 真实坐标、历史、告警、可能新迁移 | 暂缓 |

## 8. 视觉复核结论

`ui-ux-pro-max` 搜索建议与 `frontend-design` 原则共同支持：深蓝灰而非高饱和科技蓝；状态绿/蓝/橙/红/灰只用于业务状态；用文字、图标和形状冗余编码；加载和错误必须可见；转场 150-300ms 的 UI 反馈与更长的镜头飞行要能被中断；支持 `prefers-reduced-motion`。

不采用通用“地球随机飞线 + 六边形边框 + 雷达 + 全屏粒子”模板。一级地球只显示项目相关区域与进入点，二级突出站点容量，三级把端口空间与状态作为主视觉。

## 9. 临时技术验证记录

| 验证 | 命令/方式 | 结果 |
| --- | --- | --- |
| 代表仓库浅克隆 | `git clone --depth=1 --filter=blob:none ... /tmp/charge-safe-3d-research/` | `three-globe` checkout 完成；后续 `globe.gl` checkout 受网络/文件系统耗时影响中止，不将其误写为完整本地验证 |
| 官方页面核验 | GitHub 仓库、LICENSE、README、package.json/发布页 | 12 个代码候选已分别记录许可证；归档项目明确排除 |
| 资产许可 | Poly Haven/Kenney/Sketchfab/Khronos 原始资产页 | 10 个 CC0、4 个 CC BY 候选及多个排除项 |
| Three 加载原型 | 本轮未安装依赖、未修改 admin package | 未执行；列入下一阶段 MVP 验收 |

没有把临时文件、模型或截图复制进仓库。

## 10. 已确认决策与剩余确认项

2026-07-15 已确认：MVP 使用原生 Three.js + `three-globe`，Vue 负责页面、状态和生命周期，Three 引擎放入独立组件、模块和 composable；不采用 Globe.GL、TresJS、MapLibre 或 Cesium 作为当前实施依赖，也不维护两套 Three 实现。二级使用非测绘级程序化低多边形小区，三级优先程序化车棚、柜体、端口和标签锚点，不购买模型或等待完整成品模型。大屏位于管理端并使用现有登录、菜单和权限体系；教师免登录演示模式另行设计，不能绕过认证。

MVP 资产只采用 CC0。CC BY 仅作后备，必须由用户逐项确认并记录作者、来源 URL、License 和署名方式；代码许可证与模型许可证分别登记。未知、仅限个人用途、禁止再分发、All Rights Reserved、盗提、带水印、CC BY-NC 和 CC BY-ND 资产不采用。

剩余需要用户确认的仅是：最终视觉稿、具体 CC0 资产组合、站点/车棚术语、告警事实源，以及推荐演示版是否引入 `camera-controls` 等增量依赖。候选项目和 CC BY 资产保留为研究记录，不表示会实施或进入仓库。
