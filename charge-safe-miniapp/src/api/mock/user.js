/**
 * 用户信息 Mock
 */
import { registerMock } from './registry'

let currentUser = {
  userId: 1001, phone: '13800000001', nickname: '张先生', avatar: '', unreadMessages: 3
}

// ============ 获取用户信息 ============
registerMock('GET', '/api/user/info', () => ({
  code: 200, message: 'success', data: { ...currentUser }
}))

// ============ 更新个人信息 ============
registerMock('PUT', '/api/user/profile', (data) => {
  const { nickname, phone, smsCode } = data
  if (nickname !== undefined) {
    if (!nickname.trim()) return { code: 500, message: '昵称不能为空', data: null }
    currentUser.nickname = nickname.trim()
  }
  if (phone !== undefined) {
    if (!/^1[3-9]\d{9}$/.test(phone)) return { code: 500, message: '手机号格式不正确', data: null }
    if (!smsCode || smsCode !== '123456') return { code: 500, message: '验证码错误', data: null }
    currentUser.phone = phone
  }
  console.log('[Mock] 用户信息已更新')
  return { code: 200, message: '保存成功', data: { ...currentUser } }
})

// ============ 上传头像 ============
registerMock('UPLOAD', '/api/user/avatar', () => {
  const avatarUrl = 'data:image/svg+xml,' + encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100">' +
    '<defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">' +
    '<stop offset="0%" style="stop-color:#409eff"/>' +
    '<stop offset="100%" style="stop-color:#337ecc"/>' +
    '</linearGradient></defs>' +
    '<rect width="100" height="100" rx="50" fill="url(#g)"/>' +
    '<text x="50" y="55" text-anchor="middle" fill="white" font-size="32" font-family="Arial">' +
    currentUser.nickname.charAt(0) + '</text></svg>')
  currentUser.avatar = avatarUrl
  return { code: 200, message: '头像上传成功', data: { avatar: avatarUrl } }
})

// ============ 修改密码 ============
registerMock('PUT', '/api/user/password', (data) => {
  const { oldPassword, newPassword, confirmPassword } = data
  if (!oldPassword) return { code: 500, message: '请输入旧密码', data: null }
  if (oldPassword !== '123456') return { code: 500, message: '旧密码错误', data: null }
  if (!newPassword || newPassword.length < 6) return { code: 500, message: '新密码至少6位', data: null }
  if (newPassword !== confirmPassword) return { code: 500, message: '两次密码输入不一致', data: null }
  return { code: 200, message: '密码修改成功', data: null }
})

// ============ 未读消息数 ============
registerMock('GET', '/api/user/messages/unread-count', () => ({
  code: 200, message: 'success', data: { count: currentUser.unreadMessages }
}))
