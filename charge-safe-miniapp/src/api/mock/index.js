/**
 * Mock 入口 — 拦截 uni.request
 */
import { hasHandler, getHandler, getHandlerCount } from './registry'

// 加载各模块注册 Mock（无循环依赖 — 它们只依赖 registry）
import './auth'
import './user'
import './community'

const originalRequest = uni.request

uni.request = function (options) {
  const { method = 'GET', url } = options

  if (hasHandler(method, url)) {
    console.log(`[Mock] ${method} ${url}`)
    const delay = 200 + Math.random() * 400

    return new Promise((resolve) => {
      setTimeout(() => {
        const handler = getHandler(method, url)
        const result = handler(options.data || {})
        options.success && options.success({ statusCode: 200, data: result })
        options.complete && options.complete({ statusCode: 200, data: result })
        resolve({ statusCode: 200, data: result })
      }, delay)
    })
  }

  return originalRequest.call(uni, options)
}

console.log('[Mock] Mock 已拦截 uni.request，已注册', getHandlerCount(), '个接口')
