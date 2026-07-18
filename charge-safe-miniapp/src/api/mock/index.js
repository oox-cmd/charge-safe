/**
 * Mock 入口 — 拦截 uni.request
 */
import { hasHandler, getHandler, getHandlerCount } from './registry'
import { createMockRequest } from './runtime.mjs'

// 加载各模块注册 Mock（无循环依赖 — 它们只依赖 registry）
import './auth'

let installed = false

export function installMock(requestApi = uni) {
  if (installed) return false

  const originalRequest = requestApi.request.bind(requestApi)
  requestApi.request = createMockRequest(originalRequest, { hasHandler, getHandler })
  installed = true
  console.log('[Mock] 开发模拟模式已启用，已注册', getHandlerCount(), '个接口')
  return true
}
