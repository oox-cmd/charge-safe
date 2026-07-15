import { useUserStore } from '@/stores/user'

/**
 * 判断指定用户是否拥有目标权限标识
 * @param {Object} userStore 当前用户 store
 * @param {string} requestedPermission 待检查的权限标识
 * @returns {boolean}
 */
export function checkPermission(userStore, requestedPermission) {
  if (!userStore.roleCode) return false
  const grantedPermissions = userStore.permissions || []
  return grantedPermissions.includes(requestedPermission)
}

/**
 * Vue Composable 权限校验钩子
 */
export function usePermission() {
  const userStore = useUserStore()

  /**
   * 检查当前登录用户是否拥有某项权限
   * @param {string} permission 权限标识，如 'system:user:delete'
   * @returns {boolean}
   */
  const hasPermission = (permission) => {
    return checkPermission(userStore, permission)
  }

  /**
   * 检查当前用户是否属于指定角色之一
   * @param {string|string[]} roles 角色编码或角色列表
   * @returns {boolean}
   */
  const hasRole = (roles) => {
    const roleCode = userStore.roleCode
    if (!roleCode) return false
    if (Array.isArray(roles)) {
      return roles.includes(roleCode)
    }
    return roleCode === roles
  }

  return {
    hasPermission,
    hasRole
  }
}

/**
 * Vue 自定义指令，用于在 DOM 节点上做按钮级权限控制：v-permission="'system:user:create'"
 */
export const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    
    if (value && typeof value === 'string') {
      const hasPerm = checkPermission(userStore, value)
      if (!hasPerm) {
        // 如果没有权限，将节点从 DOM 中移除
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`[v-permission] 必须传入 string 类型的权限标识，例如 v-permission="'system:user:create'"`)
    }
  }
}
