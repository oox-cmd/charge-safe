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
  const permissionsLoaded = ref(false)

  const isLoggedIn = computed(() => !!token.value)

  // 将用户关键信息持久化到 localStorage
  function _persistUser() {
    localStorage.setItem(STORAGE_KEYS.token, token.value)
    localStorage.setItem(STORAGE_KEYS.roleCode, roleCode.value)
    localStorage.setItem(STORAGE_KEYS.userId, userId.value ?? '')
    localStorage.setItem(STORAGE_KEYS.nickname, nickname.value)
  }

  // 从后端加载权限码；失败时保持空集合并关闭按钮权限，不使用本地角色矩阵兜底
  async function fetchPermissions() {
    try {
      const permRes = await getPermissions()
      if (permRes && permRes.code === 200) {
        permissions.value = permRes.data || []
        permissionsLoaded.value = true
        return true
      }
    } catch (e) {
      console.warn('[UserStore] 权限列表获取失败，按钮权限将保持关闭', e?.message)
    }
    permissions.value = []
    permissionsLoaded.value = true
    return false
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

      // 跳转前加载权限，避免按钮指令在权限数据到达前误删节点
      await fetchPermissions()

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

        await fetchPermissions()

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
    permissionsLoaded.value = false

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
    permissionsLoaded,
    isLoggedIn,
    fetchPermissions,
    login,
    fetchUserInfo,
    logout,
  }
})
