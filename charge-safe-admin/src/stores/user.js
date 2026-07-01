import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(null)
  const username = ref('')
  const nickname = ref('')
  const roleCode = ref('')
  const roleName = ref('')
  const communityId = ref(null)

  const isLoggedIn = computed(() => !!token.value)

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
      localStorage.setItem('token', res.data.token)
      return true
    }
    return false
  }

  async function fetchUserInfo() {
    const res = await getCurrentUser()
    if (res.code === 200) {
      userId.value = res.data.userId
      username.value = res.data.username
      nickname.value = res.data.nickname
      roleCode.value = res.data.roleCode
      roleName.value = res.data.roleName
      communityId.value = res.data.communityId
      return true
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
    localStorage.removeItem('token')
  }

  return {
    token,
    userId,
    username,
    nickname,
    roleCode,
    roleName,
    communityId,
    isLoggedIn,
    login,
    fetchUserInfo,
    logout
  }
})
