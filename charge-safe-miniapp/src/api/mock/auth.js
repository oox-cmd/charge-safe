/**
 * 认证 Mock
 */
import { registerMock } from './registry'

const MOCK_SMS_CODE = '123456'
const BASE_URL = 'http://localhost:8080'

// 测试账号
const accounts = [
  { phone: '13800000001', password: '123456', nickname: '张先生', userId: 1001, token: 'mock_token_1001' },
  { phone: '13800000002', password: '123456', nickname: '李女士', userId: 1002, token: 'mock_token_1002' },
  { phone: '13800000003', password: '123456', nickname: '王师傅', userId: 1003, token: 'mock_token_1003' }
]
const registeredPhones = new Set(accounts.map(a => a.phone))

// ============ 密码登录 ============
registerMock('POST', `${BASE_URL}/api/auth/login`, (data) => {
  const { phone, password } = data
  if (!phone || !password) return { code: 500, message: '手机号和密码不能为空', data: null }
  const acc = accounts.find(a => a.phone === phone)
  if (!acc) return { code: 500, message: '账号不存在，请先注册', data: null }
  if (acc.password !== password) return { code: 500, message: '密码错误', data: null }
  return {
    code: 200, message: 'success',
    data: { token: acc.token, userId: acc.userId, phone: acc.phone, nickname: acc.nickname }
  }
})

// ============ 短信登录 ============
registerMock('POST', `${BASE_URL}/api/auth/login/sms`, (data) => {
  const { phone, smsCode } = data
  if (!phone) return { code: 500, message: '手机号不能为空', data: null }
  if (smsCode !== MOCK_SMS_CODE) return { code: 500, message: '验证码错误', data: null }
  let acc = accounts.find(a => a.phone === phone)
  if (!acc) {
    acc = { phone, nickname: `用户${phone.slice(-4)}`, userId: 1000 + registeredPhones.size + 1, token: `mock_token_${1000 + registeredPhones.size + 1}` }
    accounts.push(acc)
    registeredPhones.add(phone)
  }
  return {
    code: 200, message: 'success',
    data: { token: acc.token, userId: acc.userId, phone: acc.phone, nickname: acc.nickname }
  }
})

// ============ 发送验证码 ============
registerMock('POST', `${BASE_URL}/api/auth/sms/send`, (data) => {
  const { phone } = data
  if (!phone) return { code: 500, message: '手机号不能为空', data: null }
  if (!/^1[3-9]\d{9}$/.test(phone)) return { code: 500, message: '手机号格式不正确', data: null }
  console.log(`[Mock] 验证码已发送至 ${phone}：${MOCK_SMS_CODE}`)
  return { code: 200, message: '验证码发送成功', data: null }
})

// ============ 注册 ============
registerMock('POST', `${BASE_URL}/api/auth/register`, (data) => {
  const { phone, password, confirmPassword, nickname } = data
  if (!phone) return { code: 500, message: '手机号不能为空', data: null }
  if (!/^1[3-9]\d{9}$/.test(phone)) return { code: 500, message: '手机号格式不正确', data: null }
  if (!password || password.length < 6) return { code: 500, message: '密码至少6位', data: null }
  if (password !== confirmPassword) return { code: 500, message: '两次密码输入不一致', data: null }
  if (registeredPhones.has(phone)) return { code: 500, message: '该手机号已注册', data: null }

  const newUser = { phone, password, nickname: nickname || `用户${phone.slice(-4)}`, userId: 1000 + registeredPhones.size + 1, token: `mock_token_${1000 + registeredPhones.size + 1}` }
  accounts.push(newUser)
  registeredPhones.add(phone)

  console.log(`[Mock] 注册成功: ${phone}`)
  return {
    code: 200, message: '注册成功',
    data: { token: newUser.token, userId: newUser.userId, phone: newUser.phone, nickname: newUser.nickname }
  }
})

// ============ 重置密码 ============
registerMock('POST', `${BASE_URL}/api/auth/password/reset`, (data) => {
  const { phone, smsCode, newPassword, confirmPassword } = data
  if (!phone) return { code: 500, message: '手机号不能为空', data: null }
  if (smsCode !== MOCK_SMS_CODE) return { code: 500, message: '验证码错误', data: null }
  if (!newPassword || newPassword.length < 6) return { code: 500, message: '新密码至少6位', data: null }
  if (newPassword !== confirmPassword) return { code: 500, message: '两次密码输入不一致', data: null }

  const acc = accounts.find(a => a.phone === phone)
  if (acc) acc.password = newPassword
  console.log(`[Mock] 密码重置成功: ${phone}`)
  return { code: 200, message: '密码重置成功', data: null }
})
