import test from 'node:test'
import assert from 'node:assert/strict'

import { resolveMockEnabled } from '../../src/config/runtime.mjs'

test('Mock 缺省关闭', () => {
  assert.equal(resolveMockEnabled({ isDev: true, flag: undefined }), false)
})

test('Mock 只在开发环境显式 true 时开启', () => {
  assert.equal(resolveMockEnabled({ isDev: true, flag: 'true' }), true)
  assert.equal(resolveMockEnabled({ isDev: true, flag: 'false' }), false)
})

test('生产环境即使误配 true 也保持关闭', () => {
  assert.equal(resolveMockEnabled({ isDev: false, flag: 'true' }), false)
})
