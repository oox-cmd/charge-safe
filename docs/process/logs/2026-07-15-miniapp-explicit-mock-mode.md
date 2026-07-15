# 小程序 Mock 显式开发模式修复记录

> 日期：2026-07-15

## 问题

小程序入口无条件导入并执行 Mock 模块，`request.js` 中的 `USE_MOCK` 缺省为开启，却没有控制 `uni.request` 是否被替换。正式构建可能运行模拟 handler，页面又始终展示 Mock 账号，无法证明真实请求链路。

## 根因

Mock 模块把注册 handler、安装拦截器和模块加载绑定在一起；环境变量只是一个未生效的常量。缺少生产强制关闭、安装幂等和未命中委托的自动测试。

## 红测

先增加 Node 内置测试，首次运行 `npm run test:unit` 失败：

- `src/config/runtime.mjs` 不存在，无法证明缺省/开发/生产策略；
- `src/api/mock/runtime.mjs` 不存在，无法证明未命中请求会走真实网络或响应带 Mock 标识。

## 修改范围

- 新增纯函数 `resolveMockEnabled`，只允许开发环境且显式字符串 `true` 开启。
- 将 Mock 改为显式、幂等的 `installMock()`；模块加载不再替换 `uni.request`。
- 未命中 handler 原样委托真实请求，命中响应增加 `X-ChargeSafe-Mock: true`。
- App 启动和登录页仅在 Mock 真正开启时标注“开发模拟数据模式/非真实业务数据”。
- 增加 Node 内置单元测试脚本，不新增测试框架依赖。

## 验证

- `npm run test:unit`：2 个测试文件通过。
- `VITE_USE_MOCK=true npm run build:mp-weixin`：通过。
- 生产编译产物 `config/runtime.js` 中 `isDev:false`，因此误设 `VITE_USE_MOCK=true` 后 `USE_MOCK` 仍为 `false`，`app.js` 不会调用 `installMock()`。
- 网络错误没有 Mock 回退逻辑；未注册 URL 的测试确认调用原始请求函数。

## 提交

提交哈希在本记录提交后生成，以 Git 历史中 `fix: make miniapp mock mode explicit` 为准。

## 未处理事项

API/上传地址仍含 localhost，真实密码登录参数仍未对齐，核心预约/充电页面仍未接入。本批不修改这些独立问题。

## 与下一任务的边界

下一批只处理 API 与上传地址环境化、请求错误语义和真实 `username/password` 登录契约；不同时修改后端 charging 状态机。
