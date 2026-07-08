export const opsRoutes = {
  path: '/ops',
  component: () => import('@/layouts/OpsLayout.vue'),
  redirect: '/ops/dashboard',
  meta: { requiresAuth: true, roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'MAINTAINER', 'INSPECTOR'] },
  children: [
    {
      path: 'dashboard',
      name: 'OpsDashboard',
      component: () => import('@/views/ops/dashboard/Index.vue'),
      meta: { title: '运维工作台', icon: 'Odometer' }
    },
    {
      path: 'community',
      name: 'OpsCommunity',
      component: () => import('@/views/community/Index.vue'),
      meta: { title: '社区管理', icon: 'OfficeBuilding', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    },
    {
      path: 'parking-area',
      name: 'OpsParkingArea',
      component: () => import('@/views/community/ParkingArea.vue'),
      meta: { title: '车棚管理', icon: 'Garage', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    },
    {
      path: 'device',
      name: 'OpsDevice',
      component: () => import('@/views/device/Index.vue'),
      meta: { title: '充电设备', icon: 'Monitor', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    },
    {
      path: 'port',
      name: 'OpsPort',
      component: () => import('@/views/device/Port.vue'),
      meta: { title: '充电端口', icon: 'Connection', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    },
    {
      path: 'reservation',
      name: 'OpsReservation',
      component: () => import('@/views/charging/Reservation.vue'),
      meta: { title: '预约记录', icon: 'Clock', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    },
    {
      path: 'order',
      name: 'OpsOrder',
      component: () => import('@/views/charging/Order.vue'),
      meta: { title: '订单监控', icon: 'Document', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    },
    {
      path: 'repair-report',
      name: 'OpsRepairReport',
      component: () => import('@/views/repair/Report.vue'),
      meta: { title: '报修记录', icon: 'Warning', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    },
    {
      path: 'repair-ticket',
      name: 'OpsRepairTicket',
      component: () => import('@/views/repair/Ticket.vue'),
      meta: { title: '维修工单', icon: 'Tickets', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'MAINTAINER'] }
    },
    {
      path: 'inspection-task',
      name: 'OpsInspectionTask',
      component: () => import('@/views/inspection/Task.vue'),
      meta: { title: '巡检任务', icon: 'Checked', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'INSPECTOR'] }
    },
    {
      path: 'hazard',
      name: 'OpsHazard',
      component: () => import('@/views/inspection/Hazard.vue'),
      meta: { title: '隐患整改', icon: 'WarnTriangleFilled', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'INSPECTOR'] }
    },
    {
      path: 'statistics',
      name: 'OpsStatistics',
      component: () => import('@/views/statistics/Index.vue'),
      meta: { title: '运维统计', icon: 'DataAnalysis', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    },
    {
      path: 'notification',
      name: 'OpsNotification',
      component: () => import('@/views/notification/Index.vue'),
      meta: { title: '消息通知', icon: 'Bell' }
    }
  ]
}
