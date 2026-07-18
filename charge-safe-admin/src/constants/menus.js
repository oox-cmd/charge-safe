export const permissionMenuItems = [
  { path: '/permission/dashboard', title: '系统总览', icon: 'Odometer' },
  {
    title: '系统管理',
    icon: 'Setting',
    children: [
      { path: '/permission/system/user', title: '用户管理', icon: 'User' },
      { path: '/permission/system/role', title: '角色管理', icon: 'UserFilled' },
      { path: '/permission/system/menu', title: '菜单管理', icon: 'Menu' },
      { path: '/permission/system/dept', title: '部门管理', icon: 'OfficeBuilding' }
    ]
  },
  {
    title: '基础配置',
    icon: 'SetUp',
    children: [
      { path: '/permission/device-category', title: '设备分类', icon: 'Memo' },
      { path: '/permission/simulator', title: '设备模拟', icon: 'Cpu' }
    ]
  },
  {
    title: '系统监控',
    icon: 'Monitor',
    children: [
      { path: '/permission/log', title: '操作日志', icon: 'Notebook' }
    ]
  },
  {
    title: '数据展示',
    icon: 'DataAnalysis',
    children: [
      { path: '/permission/statistics', title: '统计分析', icon: 'PieChart' },
      { path: '/bigscreen', title: '可视化大屏', icon: 'FullScreen' },
      { path: '/bigscreen3d', title: '三维管理大屏', icon: 'Platform' }
    ]
  }
]

export const opsMenuItems = [
  { path: '/ops/dashboard', title: '运维工作台', icon: 'Odometer', roles: [] },
  {
    title: '社区资源',
    icon: 'OfficeBuilding',
    roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'],
    children: [
      { path: '/ops/community', title: '社区管理', icon: 'Location', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
      { path: '/ops/parking-area', title: '车棚管理', icon: 'Garage', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    ]
  },
  {
    title: '设备管理',
    icon: 'Monitor',
    roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'],
    children: [
      { path: '/ops/device', title: '充电设备', icon: 'Cpu', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
      { path: '/ops/port', title: '充电端口', icon: 'Connection', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    ]
  },
  {
    title: '充电业务',
    icon: 'Lightning',
    roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'],
    children: [
      { path: '/ops/reservation', title: '预约记录', icon: 'Clock', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
      { path: '/ops/order', title: '订单监控', icon: 'Document', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    ]
  },
  {
    title: '运维工单',
    icon: 'Tickets',
    roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'MAINTAINER'],
    children: [
      { path: '/ops/repair-report', title: '报修记录', icon: 'Warning', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
      { path: '/ops/repair-ticket', title: '维修工单', icon: 'Tools', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'MAINTAINER'] }
    ]
  },
  {
    title: '巡检隐患',
    icon: 'WarnTriangleFilled',
    roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'INSPECTOR'],
    children: [
      { path: '/ops/inspection-task', title: '巡检任务', icon: 'Checked', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'INSPECTOR'] },
      { path: '/ops/hazard', title: '隐患整改', icon: 'Aim', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'INSPECTOR'] }
    ]
  },
  {
    title: '数据统计',
    icon: 'PieChart',
    roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'],
    children: [
      { path: '/ops/statistics', title: '运维统计', icon: 'DataAnalysis', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
      { path: '/bigscreen', title: '可视化大屏', icon: 'FullScreen', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
      { path: '/bigscreen3d', title: '三维管理大屏', icon: 'Platform', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] }
    ]
  }
]
