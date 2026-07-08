/**
 * Mock 注册中心 — 独立的注册器，避免循环依赖
 */

const handlers = new Map()

export function registerMock(method, url, handler) {
  handlers.set(`${method.toUpperCase()}:${url}`, handler)
}

export function getHandler(method, url) {
  return handlers.get(`${method.toUpperCase()}:${url}`)
}

export function hasHandler(method, url) {
  return handlers.has(`${method.toUpperCase()}:${url}`)
}

export function getHandlerCount() {
  return handlers.size
}
