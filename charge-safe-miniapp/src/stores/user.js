/**
 * 用户状态管理 - Pinia
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUser, loginByPassword, loginBySms } from '@/api/auth'
import { updateProfile as updateProfileApi, uploadAvatar as uploadAvatarApi, changePassword as changePasswordApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const savedCommunity = uni.getStorageSync('community') || {}
  const token = ref(uni.getStorageSync('token') || '')
  const userId = ref(null)
  const username = ref('')
  const phone = ref('')
  const nickname = ref('')
  const avatar = ref('')
  const roleCode = ref('')
  const roleName = ref('')
  const communityId = ref(savedCommunity.id || null)
  const communityName = ref(savedCommunity.name || '')

  const isLoggedIn = computed(() => !!token.value)

  function saveLoginState(data) {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username || ''
    phone.value = data.phone || ''
    nickname.value = data.nickname
    avatar.value = data.avatar || ''
    roleCode.value = data.roleCode || ''
    roleName.value = data.roleName || ''
    communityId.value = data.communityId || null
    uni.setStorageSync('token', data.token)
    uni.setStorageSync('userInfo', {
      userId: data.userId, username: data.username || '', phone: data.phone || '',
      nickname: data.nickname, avatar: data.avatar || '', roleCode: data.roleCode || '',
      roleName: data.roleName || '', communityId: data.communityId || null
    })
  }

  async function loginWithPassword(params) {
    const res = await loginByPassword(params)
    if (res.code === 200 && res.data) { saveLoginState(res.data); return true }
    return false
  }

  async function loginWithSms(params) {
    const res = await loginBySms(params)
    if (res.code === 200 && res.data) { saveLoginState(res.data); return true }
    return false
  }

  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      if (res.code === 200 && res.data) {
        userId.value = res.data.userId
        username.value = res.data.username || ''
        phone.value = res.data.phone || ''
        nickname.value = res.data.nickname
        avatar.value = res.data.avatar || ''
        roleCode.value = res.data.roleCode || ''
        roleName.value = res.data.roleName || ''
        communityId.value = res.data.communityId || null
        return true
      }
    } catch { /* ignore */ }
    return false
  }

  async function updateProfile(params) {
    const res = await updateProfileApi(params)
    if (res.code === 200 && res.data) {
      nickname.value = res.data.nickname
      phone.value = res.data.phone
      avatar.value = res.data.avatar || avatar.value
      // 同步到 storage
      const info = uni.getStorageSync('userInfo') || {}
      info.nickname = res.data.nickname
      info.phone = res.data.phone
      uni.setStorageSync('userInfo', info)
      return true
    }
    return false
  }

  async function uploadAvatar(filePath) {
    const res = await uploadAvatarApi(filePath)
    if (res.code === 200 && res.data) {
      avatar.value = res.data.avatar
      const info = uni.getStorageSync('userInfo') || {}
      info.avatar = res.data.avatar
      uni.setStorageSync('userInfo', info)
      return res.data.avatar
    }
    return null
  }

  async function changePassword(params) {
    const res = await changePasswordApi(params)
    return res.code === 200
  }

  function setCommunity(id, name) {
    communityId.value = id
    communityName.value = name
    uni.setStorageSync('community', { id, name })
  }

  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    phone.value = ''
    nickname.value = ''
    avatar.value = ''
    roleCode.value = ''
    roleName.value = ''
    communityId.value = null
    communityName.value = ''
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('community')
  }

  return { token, userId, username, phone, nickname, avatar, roleCode, roleName, communityId, communityName, isLoggedIn,
    loginWithPassword, loginWithSms, fetchUserInfo, updateProfile, uploadAvatar, changePassword, setCommunity, logout, saveLoginState }
})
