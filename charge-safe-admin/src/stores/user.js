import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser, getPermissions } from '@/api/auth'

// 将关键用户信息也持久化到 localStorage，避免刷新后丢失 roleCode 导致路由死循环
const STORAGE_KEYS = {
  token: 'token',
  roleCode: 'roleCode',
  userId: 'userId',
  nickname: 'nickname',
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(STORAGE_KEYS.token) || '')
  const userId = ref(localStorage.getItem(STORAGE_KEYS.userId) ? Number(localStorage.getItem(STORAGE_KEYS.userId)) : null)
  const username = ref('')
  const nickname = ref(localStorage.getItem(STORAGE_KEYS.nickname) || '')
  const roleCode = ref(localStorage.getItem(STORAGE_KEYS.roleCode) || '')
  const roleName = ref('')
  const communityId = ref(null)
  const permissions = ref([])

  const isLoggedIn = computed(() => !!token.value)

  // 将用户关键信息持久化到 localStorage
  function _persistUser() {
    localStorage.setItem(STORAGE_KEYS.token, token.value)
    localStorage.setItem(STORAGE_KEYS.roleCode, roleCode.value)
    localStorage.setItem(STORAGE_KEYS.userId, userId.value ?? '')
    localStorage.setItem(STORAGE_KEYS.nickname, nickname.value)
  }

  // 安全地获取权限列表（不抛出异常，失败时静默降级）
  async function _fetchPermissionsSilently() {
    try {
      const permRes = await getPermissions()
      if (permRes && permRes.code === 200) {
        permissions.value = permRes.data || []
      }
    } catch (e) {
      // 权限接口失败不影响登录流程，前端有本地降级矩阵兜底
      console.warn('[UserStore] 权限列表获取失败，将使用本地降级策略', e?.message)
      permissions.value = []
    }
  }

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    if (res.code === 200) {
      token.value = res.data.token
      userId.value = res.data.userId
      username.value = res.data.username
      nickname.value = res.data.nickname
      roleCode.value = res.data.roleCode
      roleName.value = res.data.roleName
      communityId.value = res.data.communityId

      // 持久化关键字段，防止刷新后 roleCode 丢失
      _persistUser()

      // 异步获取权限，不阻塞登录跳转
      _fetchPermissionsSilently()

      return true
    }
    return false
  }

  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      if (res && res.code === 200) {
        userId.value = res.data.userId
        username.value = res.data.username
        nickname.value = res.data.nickname
        roleCode.value = res.data.roleCode
        roleName.value = res.data.roleName
        communityId.value = res.data.communityId

        // 刷新持久化数据
        _persistUser()

        // 异步获取权限，不阻塞路由跳转
        _fetchPermissionsSilently()

        return true
      }
    } catch (e) {
      console.warn('[UserStore] 获取用户信息失败', e?.message)
    }
    return false
  }

  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    nickname.value = ''
    roleCode.value = ''
    roleName.value = ''
    communityId.value = null
    permissions.value = []

    // 清除所有持久化数据
    Object.values(STORAGE_KEYS).forEach(key => localStorage.removeItem(key))
  }

  return {
    token,
    userId,
    username,
    nickname,
    roleCode,
    roleName,
    communityId,
    permissions,
    isLoggedIn,
    login,
    fetchUserInfo,
    logout,
  }
})
