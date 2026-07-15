import test from 'node:test'
import assert from 'node:assert/strict'

import { createMockRequest } from '../../src/api/mock/runtime.mjs'

test('未注册的请求原样交给真实 uni.request', () => {
  const calls = []
  const originalRequest = (options) => {
    calls.push(options)
    return 'real-request-result'
  }
  const request = createMockRequest(originalRequest, {
    hasHandler: () => false,
    getHandler: () => undefined,
    schedule: () => assert.fail('未命中 Mock 时不应调度模拟响应'),
    logger: () => {}
  })

  const options = { method: 'GET', url: 'https://api.example.com/api/orders/my' }
  assert.equal(request(options), 'real-request-result')
  assert.deepEqual(calls, [options])
})

test('已注册请求返回带明确 Mock 标识的响应', async () => {
  const request = createMockRequest(() => assert.fail('命中 Mock 时不应发起真实请求'), {
    hasHandler: () => true,
    getHandler: () => () => ({ code: 200, data: { id: 1 } }),
    schedule: (callback) => callback(),
    logger: () => {}
  })
  let response

  await request({
    method: 'GET',
    url: 'https://api.example.com/api/user/info',
    success: (value) => { response = value }
  })

  assert.equal(response.statusCode, 200)
  assert.equal(response.header['X-ChargeSafe-Mock'], 'true')
})
