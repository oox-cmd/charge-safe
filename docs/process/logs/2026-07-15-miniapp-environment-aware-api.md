# 小程序 API 与上传地址环境化修复记录

> 日期：2026-07-15

## 问题

小程序普通请求、头像上传和 Mock handler 分别写死 `http://localhost:8080`，真机无法访问开发机；没有环境模板。真实密码登录发送 `phone/password`，而后端只接受 `username/password`。请求层又把 HTTP 200 的业务错误当成成功 resolve。

## 根因

请求和上传没有共享运行时配置；Mock 注册键耦合固定完整 URL；前端表单依据早期手机号原型实现，没有核对 `LoginDTO`/`LoginVO`；请求层只检查 HTTP 状态，没有检查统一响应 body code。

## 红测

- 首次新增配置与响应测试后，`npm run test:unit` 因 `config/api.mjs`、`api/response.mjs` 不存在而失败。
- 增加“Mock 匹配不依赖固定主机/查询参数”测试后，因 `extractRequestPath` 未导出而失败。

## 修改范围

- 新增 API URL 规范化模块和统一响应解析模块。
- 普通请求、上传统一使用 `VITE_API_BASE_URL`；移除小程序源码与测试的固定 localhost/LAN/WSL 地址。
- Mock registry 只按 method + path 匹配，不再硬编码主机。
- 密码登录改为 `username/password`，保存后端真实角色/社区字段，当前用户改读 `/api/auth/me`。
- 登录页隐藏后端不存在的短信、注册和找回密码入口；保留历史页面供后续独立实现。
- 新增 `.env.example`，README 说明 H5、微信开发者工具、真机、上传和生产配置。

## 验证

- `npm run test:unit`：4 个测试文件通过。
- `VITE_API_BASE_URL=/ npm run build:mp-weixin`：通过。
- `rg` 检查 `charge-safe-miniapp`（排除依赖和构建产物）：无 localhost、`127.0.0.1`、个人 LAN/WSL IP。
- HTTP 200 + body 403 测试确认 reject；HTTP 401 保留认证错误字段。

## 提交

提交哈希在本记录提交后生成，以 Git 历史中 `fix: make miniapp API endpoints environment-aware` 为准。

## 未处理事项

目标手机上的实际局域网、防火墙和微信合法域名验证需要真实设备环境；头像上传、短信、注册、找回密码后端能力不存在；预约/充电/订单仍未接入。

## 与下一任务的边界

下一批只收紧后端预约—开始—结束状态机和本人/社区访问边界，不同时修改居民页面或大屏。
