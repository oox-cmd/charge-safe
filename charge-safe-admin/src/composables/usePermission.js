import { useUserStore } from '@/stores/user'

// 降级用的权限映射矩阵：当后端未返回权限数据时，使用此备用矩阵
const fallbackRolePermissionMatrix = {
  // 平台管理员：拥有所有系统管理权限及平台全局操作权限
  'PLATFORM_ADMIN': ['*'],

  // 物业管理员：拥有辖区内社区、车棚、设备、订单、工单、巡检的所有操作权，但不含系统用户及角色管理
  'PROPERTY_ADMIN': [
    'community:*',
    'parking-area:*',
    'device:*',
    'port:*',
    'device-category:*',
    'charging:*',
    'repair:*',
    'inspection:*',
    'statistics:*',
    'log:*'
  ],

  // 维修/运维人员：仅拥有维修工单的查看、接单、执行与闭环，以及部分只读信息
  'MAINTAINER': [
    'repair:ticket:*',
    'repair:report:view',
    'device:view',
    'port:view',
    'parking-area:view'
  ],

  // 巡检/安全人员：仅拥有巡检任务的执行、隐患的提报与整改，以及部分只读信息
  'INSPECTOR': [
    'inspection:task:*',
    'inspection:hazard:*',
    'device:view',
    'port:view',
    'parking-area:view'
  ],

  // 普通居民 / 外部用户：只读或基本预约操作权限
  'RESIDENT': [
    'charging:reserve',
    'charging:order:view'
  ]
}

/**
 * 判断指定用户是否拥有目标权限标识
 * @param {Object} userStore 当前用户 store
 * @param {string} requestedPermission 待检查的权限标识
 * @returns {boolean}
 */
export function checkPermission(userStore, requestedPermission) {
  const roleCode = userStore.roleCode
  if (!roleCode) return false
  
  // 优先使用后端返回的真实动态权限
  let grantedPermissions = userStore.permissions || []
  
  // 容错降级：如果后端没有返回权限数据，则使用本地硬编码矩阵
  if (grantedPermissions.length === 0) {
    grantedPermissions = fallbackRolePermissionMatrix[roleCode] || []
  }

  if (grantedPermissions.includes('*')) return true

  return grantedPermissions.some(granted => {
    // 精确匹配
    if (granted === requestedPermission) return true

    // 通配符匹配 (如 "system:*" 匹配 "system:user:add")
    if (granted.endsWith(':*')) {
      const prefix = granted.slice(0, -2)
      return requestedPermission.startsWith(prefix + ':')
    }

    return false
  })
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
 * Vue 自定义指令，用于在 DOM 节点上做按钮级权限控制：v-permission="'system:user:add'"
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
      throw new Error(`[v-permission] 必须传入 string 类型的权限标识，例如 v-permission="'system:user:add'"`)
    }
  }
}
