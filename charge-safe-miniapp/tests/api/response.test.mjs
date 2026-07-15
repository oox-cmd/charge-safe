import test from 'node:test'
import assert from 'node:assert/strict'

import { parseApiResponse } from '../../src/api/response.mjs'

test('HTTP 200 且业务 code 200 才视为成功', () => {
  const body = { code: 200, message: 'success', data: { id: 1 } }
  assert.equal(parseApiResponse({ statusCode: 200, data: body }), body)
})

test('HTTP 200 的业务错误必须 reject 而不是伪成功', () => {
  assert.throws(
    () => parseApiResponse({ statusCode: 200, data: { code: 403, message: '无权限访问' } }),
    (error) => error.code === 403 && error.message === '无权限访问'
  )
})

test('HTTP 401 保留认证错误语义', () => {
  assert.throws(
    () => parseApiResponse({ statusCode: 401, data: { code: 401, message: '未登录或登录已过期' } }),
    (error) => error.statusCode === 401 && error.code === 401
  )
})
