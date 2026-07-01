import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      },
      // 社区管理
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/community/Index.vue'),
        meta: { title: '社区管理', icon: 'OfficeBuilding', roles: ['PLATFORM_ADMIN'] }
      },
      // 车棚管理
      {
        path: 'parking-area',
        name: 'ParkingArea',
        component: () => import('@/views/community/ParkingArea.vue'),
        meta: { title: '车棚管理', icon: 'Garage', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
      },
      // 设备管理
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/device/Index.vue'),
        meta: { title: '设备管理', icon: 'Monitor', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
      },
      // 端口管理
      {
        path: 'port',
        name: 'Port',
        component: () => import('@/views/device/Port.vue'),
        meta: { title: '端口管理', icon: 'Connection', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
      },
      // 预约记录
      {
        path: 'reservation',
        name: 'Reservation',
        component: () => import('@/views/charging/Reservation.vue'),
        meta: { title: '预约记录', icon: 'Clock', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
      },
      // 充电订单
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/charging/Order.vue'),
        meta: { title: '充电订单', icon: 'Document', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
      },
      // 报修记录
      {
        path: 'repair-report',
        name: 'RepairReport',
        component: () => import('@/views/repair/Report.vue'),
        meta: { title: '报修记录', icon: 'Warning', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
      },
      // 维修工单
      {
        path: 'repair-ticket',
        name: 'RepairTicket',
        component: () => import('@/views/repair/Ticket.vue'),
        meta: { title: '维修工单', icon: 'Tickets', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'MAINTAINER'] }
      },
      // 巡检任务
      {
        path: 'inspection-task',
        name: 'InspectionTask',
        component: () => import('@/views/inspection/Task.vue'),
        meta: { title: '巡检任务', icon: 'Checked', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'INSPECTOR'] }
      },
      // 安全隐患
      {
        path: 'hazard',
        name: 'Hazard',
        component: () => import('@/views/inspection/Hazard.vue'),
        meta: { title: '安全隐患', icon: 'WarnTriangleFilled', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'INSPECTOR'] }
      },
      // 消息通知
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('@/views/notification/Index.vue'),
        meta: { title: '消息通知', icon: 'Bell' }
      },
      // 操作日志
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/log/Index.vue'),
        meta: { title: '操作日志', icon: 'Notebook', roles: ['PLATFORM_ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth === false) {
    // 不需要认证的页面
    if (userStore.token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
  } else {
    // 需要认证的页面
    if (!userStore.token) {
      next('/login')
    } else {
      // 检查角色权限
      if (to.meta.roles && to.meta.roles.length > 0) {
        if (to.meta.roles.includes(userStore.roleCode)) {
          next()
        } else {
          ElMessage.error('无权访问该页面')
          next(from.path || '/')
        }
      } else {
        next()
      }
    }
  }
})

export default router
