/**
 * 认证 API
 */
import request from './request'

export function loginByPassword(params) {
  return request({ url: '/api/auth/login', method: 'POST', data: params })
}

export function getCurrentUser() {
  return request({ url: '/api/auth/me', method: 'GET' })
}

export function loginBySms(params) {
  return request({ url: '/api/auth/login/sms', method: 'POST', data: params })
}

export function sendSmsCode(params) {
  return request({ url: '/api/auth/sms/send', method: 'POST', data: params })
}

export function register(params) {
  return request({ url: '/api/auth/register', method: 'POST', data: params })
}

export function resetPassword(params) {
  return request({ url: '/api/auth/password/reset', method: 'POST', data: params })
}
