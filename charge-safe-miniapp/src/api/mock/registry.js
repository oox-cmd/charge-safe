/**
 * Mock 注册中心 — 独立的注册器，避免循环依赖
 */

import { extractRequestPath } from '@/config/api.mjs'

const handlers = new Map()

function getRequestPath(url) {
  return extractRequestPath(url)
}

function getKey(method, url) {
  return `${method.toUpperCase()}:${getRequestPath(url)}`
}

export function registerMock(method, url, handler) {
  handlers.set(getKey(method, url), handler)
}

export function getHandler(method, url) {
  return handlers.get(getKey(method, url))
}

export function hasHandler(method, url) {
  return handlers.has(getKey(method, url))
}

export function getHandlerCount() {
  return handlers.size
}
