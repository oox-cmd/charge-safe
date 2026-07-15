import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { permissionRoutes } from './permission.routes'
import { opsRoutes } from './ops.routes'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    name: 'Root',
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  permissionRoutes,
  opsRoutes,
  {
    path: '/bigscreen',
    name: 'BigScreen',
    component: () => import('@/views/bigscreen/Index.vue'),
    meta: {
      title: '管理运维大屏',
      requiresAuth: true,
      permission: 'statistics:bigscreen:view'
    }
  },
  {
    path: '/resident/dashboard',
    name: 'ResidentDashboard',
    component: () => import('@/views/resident/Dashboard.vue'),
    meta: {
      title: '社区充电首页',
      requiresAuth: true,
      permission: 'resident:dashboard:view',
      roles: ['RESIDENT']
    }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/auth/Forbidden.vue'),
    meta: { title: '无权访问', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 根据角色获取默认落地页路径
 * 所有角色都有对应的页面，永远不会返回 '/login'，从根本上杜绝无限跳转
 */
const getDefaultPathByRole = (roleCode) => {
  if (roleCode === 'PLATFORM_ADMIN') return '/permission/dashboard'
  if (roleCode === 'PROPERTY_ADMIN') return '/ops/dashboard'
  if (roleCode === 'MAINTAINER') return '/ops/repair-ticket'
  if (roleCode === 'INSPECTOR') return '/ops/inspection-task'
  if (roleCode === 'RESIDENT') return '/resident/dashboard'
  return '/403'
}

// 标记是否正在进行用户信息拉取，防止并发重复请求
let isFetchingUserInfo = false

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 如果有 token 但内存中没有 roleCode，尝试从持久化恢复或从服务器拉取
  // 只有当 roleCode 真的为空时才拉取，避免重复请求
  if (userStore.token && !userStore.roleCode && !isFetchingUserInfo) {
    isFetchingUserInfo = true
    try {
      const ok = await userStore.fetchUserInfo()
      if (!ok) {
        // 服务器返回失败，说明 token 已失效，清除并去登录
        userStore.logout()
        isFetchingUserInfo = false
        next('/login')
        return
      }
    } catch (error) {
      userStore.logout()
      isFetchingUserInfo = false
      next('/login')
      return
    }
    isFetchingUserInfo = false
  }

  // ---- 无需认证的页面 (/login) ----
  if (to.meta.requiresAuth === false) {
    // 已登录用户访问 /login，重定向到其首页
    if (userStore.token && to.path === '/login') {
      next(getDefaultPathByRole(userStore.roleCode))
    } else {
      next()
    }
    return
  }

  // 刷新页面时 roleCode 可能已持久化，但权限码仍需从后端重新获取
  if (userStore.token && !userStore.permissionsLoaded) {
    await userStore.fetchPermissions()
  }

  // ---- 需要认证的页面 ----
  if (!userStore.token) {
    // 未登录，去登录页
    next('/login')
    return
  }

  // 根路径：重定向到该角色的默认首页
  if (to.path === '/') {
    next(getDefaultPathByRole(userStore.roleCode))
    return
  }

  // 检查当前路由是否有角色限制
  const requiredRoles = to.matched.flatMap(record => record.meta.roles || [])
  if (requiredRoles.length > 0 && !requiredRoles.includes(userStore.roleCode)) {
    ElMessage.error('无权访问该页面')
    next({ path: '/403', replace: true, query: { from: to.fullPath } })
    return
  }

  // 页面权限以登录后从后端加载的 permission_code 为唯一来源
  const requiredPermissions = to.matched
    .map(record => record.meta.permission)
    .filter(Boolean)
  if (requiredPermissions.some(permission => !userStore.permissions.includes(permission))) {
    ElMessage.error('无权访问该页面')
    next({ path: '/403', replace: true, query: { from: to.fullPath } })
    return
  }

  next()
})

export default router
