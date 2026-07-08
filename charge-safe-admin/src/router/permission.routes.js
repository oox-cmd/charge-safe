export const permissionRoutes = {
  path: '/permission',
  component: () => import('@/layouts/PermissionLayout.vue'),
  redirect: '/permission/dashboard',
  meta: { requiresAuth: true, roles: ['PLATFORM_ADMIN'] },
  children: [
    {
      path: 'dashboard',
      name: 'PermissionDashboard',
      component: () => import('@/views/permission/dashboard/Index.vue'),
      meta: { title: '系统总览', icon: 'Odometer' }
    },
    {
      path: 'system/user',
      name: 'PermissionUser',
      component: () => import('@/views/system/User.vue'),
      meta: { title: '用户管理', icon: 'User' }
    },
    {
      path: 'system/role',
      name: 'PermissionRole',
      component: () => import('@/views/system/Role.vue'),
      meta: { title: '角色管理', icon: 'UserFilled' }
    },
    {
      path: 'system/menu',
      name: 'PermissionMenu',
      component: () => import('@/views/system/Menu.vue'),
      meta: { title: '菜单管理', icon: 'Menu' }
    },
    {
      path: 'system/dept',
      name: 'PermissionDept',
      component: () => import('@/views/permission/system/Department.vue'),
      meta: { title: '部门管理', icon: 'OfficeBuilding' }
    },
    {
      path: 'device-category',
      name: 'PermissionDeviceCategory',
      component: () => import('@/views/permission/device-category/Index.vue'),
      meta: { title: '设备分类', icon: 'Memo' }
    },
    {
      path: 'simulator',
      name: 'PermissionSimulator',
      component: () => import('@/views/permission/simulator/Index.vue'),
      meta: { title: '设备模拟', icon: 'Cpu' }
    },
    {
      path: 'log',
      name: 'PermissionLog',
      component: () => import('@/views/log/Index.vue'),
      meta: { title: '操作日志', icon: 'Notebook' }
    },
    {
      path: 'notification',
      name: 'PermissionNotification',
      component: () => import('@/views/notification/Index.vue'),
      meta: { title: '消息通知', icon: 'Bell' }
    },
    {
      path: 'statistics',
      name: 'PermissionStatistics',
      component: () => import('@/views/statistics/Index.vue'),
      meta: { title: '数据统计', icon: 'DataAnalysis' }
    }
  ]
}
