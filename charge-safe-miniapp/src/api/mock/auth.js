/** 开发环境仅保留与后端一致的用户名密码登录 Mock。 */
import { registerMock } from './registry'

const accounts = [
  { username: 'resident', password: '123456', nickname: '张先生', userId: 1001, token: 'mock_token_1001' },
  { username: 'resident2', password: '123456', nickname: '李女士', userId: 1002, token: 'mock_token_1002' },
  { username: 'maintainer', password: '123456', nickname: '王师傅', userId: 1003, token: 'mock_token_1003' }
]

registerMock('POST', '/api/auth/login', (data) => {
  const { username, password } = data
  if (!username || !password) return { code: 500, message: '用户名和密码不能为空', data: null }
  const account = accounts.find((item) => item.username === username)
  if (!account || account.password !== password) return { code: 500, message: '用户名或密码错误', data: null }
  return {
    code: 200,
    message: 'success',
    data: { token: account.token, userId: account.userId, username: account.username, nickname: account.nickname, roleCode: 'RESIDENT', roleName: '居民', communityId: 1 }
  }
})

registerMock('GET', '/api/auth/me', () => ({
  code: 200,
  message: 'success',
  data: { token: 'mock_token_1001', userId: 1001, username: 'resident', nickname: '张先生', roleCode: 'RESIDENT', roleName: '居民', communityId: 1 }
}))
