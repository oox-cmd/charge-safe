/**
 * 认证 API
 */
import request from './request'

export function loginByPassword(params) {
  return request({ url: '/api/auth/login', method: 'POST', data: params, skipAuth: true })
}

export function getCurrentUser() {
  return request({ url: '/api/auth/me', method: 'GET' })
}
