import test from 'node:test'
import assert from 'node:assert/strict'

import { extractRequestPath, normalizeApiBaseUrl, resolveApiUrl } from '../../src/config/api.mjs'

test('API 地址缺失时给出可理解错误', () => {
  assert.throws(
    () => normalizeApiBaseUrl('  '),
    /VITE_API_BASE_URL/
  )
})

test('请求 URL 去除多余斜杠', () => {
  assert.equal(
    resolveApiUrl('/api/auth/login', 'http://dev-host:8080/'),
    'http://dev-host:8080/api/auth/login'
  )
})

test('baseURL 已含 api 时不会重复拼接', () => {
  assert.equal(
    resolveApiUrl('/api/auth/login', 'https://api.example.com/api'),
    'https://api.example.com/api/auth/login'
  )
})

test('显式相对根地址支持 H5 同源代理', () => {
  assert.equal(resolveApiUrl('/api/auth/login', '/'), '/api/auth/login')
})

test('Mock 匹配只使用路径，不依赖固定主机或查询参数', () => {
  assert.equal(
    extractRequestPath('https://dev-host.example:8443/api/auth/login?from=miniapp'),
    '/api/auth/login'
  )
})
