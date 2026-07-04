# ChargeSafe 小程序居民端设计文档

> 生成时间：2026-07-04

---

## 1. 当前状态

小程序端（charge-safe-miniapp）当前只有基础骨架，包含：
- 登录页（基础表单）
- 首页（静态数据展示）
- 车棚列表（静态数据展示）

**结论**：需要从零搭建核心流程原型。

---

## 2. 页面结构设计

### 2.1 页面层级

```
charge-safe-miniapp/
├── pages/
│   ├── login/           # 登录页
│   ├── index/           # 首页
│   ├── parking/         # 车棚相关
│   │   ├── list.vue     # 车棚列表
│   │   └── detail.vue   # 车棚详情（端口状态）
│   ├── reservation/     # 预约相关
│   │   └── confirm.vue  # 预约确认
│   ├── charging/        # 充电相关
│   │   ├── scanning.vue # 扫码开始充电
│   │   └── charging.vue # 充电中页面
│   ├── order/           # 订单相关
│   │   ├── list.vue     # 我的订单
│   │   └── detail.vue   # 订单详情
│   ├── repair/          # 报修相关
│   │   ├── submit.vue   # 提交报修
│   │   └── list.vue     # 报修记录
│   ├── notification/    # 消息相关
│   │   └── list.vue     # 消息列表
│   └── profile/         # 个人中心
│       └── index.vue    # 个人中心
├── api/                 # API 请求封装
├── utils/               # 工具函数
├── static/              # 静态资源
├── pages.json           # 页面配置
├── manifest.json        # 应用配置
└── App.vue              # 根组件
```

---

## 3. 页面设计

### 3.1 登录页

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/login.vue |
| 当前状态 | 基础骨架 |
| 是否需要重构 | 重写 |
| 用户能做什么 | 输入用户名密码登录 |
| 展示哪些字段 | 用户名、密码、登录按钮 |
| 调用哪些接口 | POST /api/auth/login |
| 参考 | 微信小程序登录规范 |
| 第一阶段最小实现 | 完整登录流程 |

### 3.2 首页

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/index/index.vue |
| 当前状态 | 基础骨架 |
| 是否需要重构 | 重写 |
| 用户能做什么 | 查看车棚列表、快速预约、查看最近订单 |
| 展示哪些字段 | 车棚名称、空闲端口数、最近订单 |
| 调用哪些接口 | GET /api/parking-areas, GET /api/orders/my |
| 参考 | 充电管理系统小程序首页 |
| 第一阶段最小实现 | 车棚列表 + 最近订单 |

### 3.3 车棚列表

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/parking/list.vue |
| 当前状态 | 基础骨架 |
| 是否需要重构 | 重写 |
| 用户能做什么 | 查看所有车棚、查看空闲端口数 |
| 展示哪些字段 | 车棚名称、位置、空闲端口数、总端口数 |
| 调用哪些接口 | GET /api/parking-areas |
| 第一阶段最小实现 | 完整列表展示 |

### 3.4 车棚详情（端口状态）

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/parking/detail.vue |
| 当前状态 | 未实现 |
| 是否需要重构 | 新增 |
| 用户能做什么 | 查看端口状态、选择空闲端口预约 |
| 展示哪些字段 | 端口名称、状态、功率、当前订单 |
| 调用哪些接口 | GET /api/ports/parking-area/{id} |
| 特殊功能 | 端口状态颜色标签 |
| 第一阶段最小实现 | 端口列表 + 状态展示 |

### 3.5 预约确认

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/reservation/confirm.vue |
| 当前状态 | 未实现 |
| 是否需要重构 | 新增 |
| 用户能做什么 | 确认预约、查看预约信息 |
| 展示哪些字段 | 端口信息、预约时间、过期时间 |
| 调用哪些接口 | POST /api/reservations |
| 第一阶段最小实现 | 预约确认 + 成功提示 |

### 3.6 扫码开始充电

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/charging/scanning.vue |
| 当前状态 | 未实现 |
| 是否需要重构 | 新增 |
| 用户能做什么 | 扫描二维码、开始充电 |
| 展示哪些字段 | 端口信息、预约信息 |
| 调用哪些接口 | POST /api/charging/start |
| 特殊功能 | 调用相机扫码 |
| 第一阶段最小实现 | 扫码 + 开始充电 |

### 3.7 充电中页面

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/charging/charging.vue |
| 当前状态 | 未实现 |
| 是否需要重构 | 新增 |
| 用户能做什么 | 查看充电状态、结束充电 |
| 展示哪些字段 | 充电时长、当前费用、端口状态 |
| 调用哪些接口 | POST /api/charging/{orderId}/finish |
| 第一阶段最小实现 | 充电状态 + 结束充电按钮 |

### 3.8 我的订单

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/order/list.vue |
| 当前状态 | 未实现 |
| 是否需要重构 | 新增 |
| 用户能做什么 | 查看历史订单列表 |
| 展示哪些字段 | 订单编号、端口、时间、时长、费用、状态 |
| 调用哪些接口 | GET /api/orders/my |
| 第一阶段最小实现 | 完整订单列表 |

### 3.9 提交报修

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/repair/submit.vue |
| 当前状态 | 未实现 |
| 是否需要重构 | 新增 |
| 用户能做什么 | 选择故障设备、描述故障、上传照片 |
| 展示哪些字段 | 设备列表、故障类型、故障描述、照片上传 |
| 调用哪些接口 | POST /api/repair-reports |
| 第一阶段最小实现 | 完整报修表单 |

### 3.10 报修记录

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/repair/list.vue |
| 当前状态 | 未实现 |
| 是否需要重构 | 新增 |
| 用户能做什么 | 查看报修记录和处理进度 |
| 展示哪些字段 | 故障类型、状态、报修时间、处理进度 |
| 第一阶段最小实现 | 完整报修列表 |

### 3.11 消息列表

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/notification/list.vue |
| 当前状态 | 未实现 |
| 是否需要重构 | 新增 |
| 用户能做什么 | 查看消息通知、标记已读 |
| 展示哪些字段 | 标题、内容、类型、时间、已读状态 |
| 调用哪些接口 | GET /api/notifications |
| 第一阶段最小实现 | 完整消息列表 |

### 3.12 个人中心

| 项目 | 说明 |
|------|------|
| 文件位置 | pages/profile/index.vue |
| 当前状态 | 未实现 |
| 是否需要重构 | 新增 |
| 用户能做什么 | 查看个人信息、退出登录 |
| 展示哪些字段 | 昵称、手机号、角色、退出按钮 |
| 第一阶段最小实现 | 基础个人中心 |

---

## 4. API 请求封装

需要新建 `api/request.js`，封装 uni.request：

```javascript
const BASE_URL = 'http://localhost:8080'

const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Authorization': 'Bearer ' + uni.getStorageSync('token'),
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data)
        } else if (res.data.code === 401) {
          uni.navigateTo({ url: '/pages/login' })
          reject(res.data)
        } else {
          uni.showToast({ title: res.data.message, icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default request
```

---

## 5. 技术栈

| 技术 | 说明 |
|------|------|
| 框架 | uni-app + Vue3 |
| UI 组件 | uView 或 自定义组件 |
| 状态管理 | Pinia |
| 请求封装 | uni.request |
| 扫码功能 | uni.scanCode |

---

## 6. 参考项目

| 项目 | 参考内容 |
|------|----------|
| 充电管理系统小程序 | 首页、充电桩状态、订单列表 |
| 微信小程序报修系统 | 报修页面、工单进度 |
| uni-app 官方示例 | 页面结构、组件使用 |

---

## 7. 实现优先级

| 优先级 | 页面 | 工作量 |
|--------|------|--------|
| P0 | 登录页 | 0.5天 |
| P0 | 首页 | 1天 |
| P0 | 车棚详情 | 1天 |
| P0 | 预约确认 | 0.5天 |
| P0 | 充电中页面 | 1天 |
| P0 | 我的订单 | 0.5天 |
| P1 | 扫码充电 | 0.5天 |
| P1 | 提交报修 | 1天 |
| P1 | 报修记录 | 0.5天 |
| P1 | 消息列表 | 0.5天 |
| P2 | 个人中心 | 0.5天 |

**总计**：约 7-8 天
