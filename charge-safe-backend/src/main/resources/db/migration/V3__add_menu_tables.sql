-- ChargeSafe V3: menu permission tables and initial RBAC menu bindings
-- Keep V1/V2 immutable. This migration only adds sys_menu and sys_role_menu.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单ID，0表示顶级菜单',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL DEFAULT 2 COMMENT '菜单类型: 1-目录 2-菜单 3-按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '前端路由路径',
  `component` varchar(200) DEFAULT NULL COMMENT '前端组件路径',
  `permission_code` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(50) DEFAULT NULL COMMENT '菜单图标',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统菜单权限表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

-- Top-level directories and route menus. IDs are fixed so role bindings are deterministic.
INSERT IGNORE INTO `sys_menu`
(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission_code`, `icon`, `sort_order`, `status`)
VALUES
(1000, 0, '工作台', 2, '/dashboard', 'dashboard/Index', 'dashboard:view', 'Odometer', 10, 1),

(1100, 0, '系统管理', 1, '/system', NULL, 'system:view', 'Setting', 20, 1),
(1110, 1100, '用户管理', 2, '/system/user', 'system/User', 'system:user:view', 'User', 21, 1),
(1120, 1100, '角色管理', 2, '/system/role', 'system/Role', 'system:role:view', 'UserFilled', 22, 1),
(1130, 1100, '菜单管理', 2, '/system/menu', 'system/Menu', 'system:menu:view', 'Menu', 23, 1),

(1200, 0, '社区管理', 1, '/community-manage', NULL, 'community:view', 'OfficeBuilding', 30, 1),
(1210, 1200, '社区列表', 2, '/community', 'community/Index', 'community:list:view', 'OfficeBuilding', 31, 1),
(1220, 1200, '车棚管理', 2, '/parking-area', 'community/ParkingArea', 'parking-area:view', 'House', 32, 1),

(1300, 0, '设备管理', 1, '/device-manage', NULL, 'device:view', 'Monitor', 40, 1),
(1310, 1300, '充电设备', 2, '/device', 'device/Index', 'device:list:view', 'Monitor', 41, 1),
(1320, 1300, '充电端口', 2, '/port', 'device/Port', 'port:view', 'Connection', 42, 1),

(1400, 0, '充电业务', 1, '/charging-manage', NULL, 'charging:view', 'Lightning', 50, 1),
(1410, 1400, '预约记录', 2, '/reservation', 'charging/Reservation', 'reservation:view', 'Clock', 51, 1),
(1420, 1400, '充电订单', 2, '/order', 'charging/Order', 'order:view', 'Document', 52, 1),

(1500, 0, '运维管理', 1, '/operation-manage', NULL, 'operation:view', 'Tools', 60, 1),
(1510, 1500, '报修记录', 2, '/repair-report', 'repair/Report', 'repair-report:view', 'Warning', 61, 1),
(1520, 1500, '维修工单', 2, '/repair-ticket', 'repair/Ticket', 'repair-ticket:view', 'Tickets', 62, 1),
(1530, 1500, '巡检任务', 2, '/inspection-task', 'inspection/Task', 'inspection-task:view', 'Checked', 63, 1),
(1540, 1500, '安全隐患', 2, '/hazard', 'inspection/Hazard', 'hazard:view', 'WarnTriangleFilled', 64, 1),

(1600, 0, '消息中心', 1, '/message-center', NULL, 'notification:view', 'Bell', 70, 1),
(1610, 1600, '消息通知', 2, '/notification', 'notification/Index', 'notification:list:view', 'Bell', 71, 1),

(1700, 0, '数据统计', 1, '/statistics-manage', NULL, 'statistics:view', 'DataAnalysis', 80, 1),
(1710, 1700, '统计分析', 2, '/statistics', 'statistics/Index', 'statistics:analysis:view', 'TrendCharts', 81, 1),
(1720, 1700, '可视化大屏', 2, '/bigscreen', 'bigscreen/Index', 'statistics:bigscreen:view', 'Platform', 82, 1),

(1800, 0, '系统监控', 1, '/monitor', NULL, 'monitor:view', 'Notebook', 90, 1),
(1810, 1800, '操作日志', 2, '/log', 'log/Index', 'operation-log:view', 'Notebook', 91, 1);

-- Button-level permissions are reserved for later expansion.
INSERT IGNORE INTO `sys_menu`
(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission_code`, `icon`, `sort_order`, `status`)
VALUES
(1111, 1110, '新增用户', 3, NULL, NULL, 'system:user:create', NULL, 211, 1),
(1112, 1110, '编辑用户', 3, NULL, NULL, 'system:user:update', NULL, 212, 1),
(1113, 1110, '删除用户', 3, NULL, NULL, 'system:user:delete', NULL, 213, 1),
(1114, 1110, '启停用户', 3, NULL, NULL, 'system:user:status', NULL, 214, 1),
(1115, 1110, '分配角色', 3, NULL, NULL, 'system:user:assign-role', NULL, 215, 1),
(1116, 1110, '重置密码', 3, NULL, NULL, 'system:user:reset-password', NULL, 216, 1),
(1121, 1120, '新增角色', 3, NULL, NULL, 'system:role:create', NULL, 221, 1),
(1122, 1120, '编辑角色', 3, NULL, NULL, 'system:role:update', NULL, 222, 1),
(1123, 1120, '删除角色', 3, NULL, NULL, 'system:role:delete', NULL, 223, 1),
(1124, 1120, '分配菜单', 3, NULL, NULL, 'system:role:assign-menu', NULL, 224, 1),
(1131, 1130, '新增菜单', 3, NULL, NULL, 'system:menu:create', NULL, 231, 1),
(1132, 1130, '编辑菜单', 3, NULL, NULL, 'system:menu:update', NULL, 232, 1),
(1133, 1130, '删除菜单', 3, NULL, NULL, 'system:menu:delete', NULL, 233, 1);

-- PLATFORM_ADMIN owns all backend menus and reserved button permissions.
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `sys_menu` WHERE `deleted` = 0;

-- PROPERTY_ADMIN owns the main operational management menus, statistics, bigscreen, messages and logs.
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, `id` FROM `sys_menu`
WHERE `id` IN (
  1000,
  1200, 1220,
  1300, 1310, 1320,
  1400, 1410, 1420,
  1500, 1510, 1520, 1530, 1540,
  1600, 1610,
  1700, 1710, 1720,
  1800, 1810
);

-- MAINTAINER focuses on repair tickets and message handling.
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 3, `id` FROM `sys_menu`
WHERE `id` IN (
  1000,
  1500, 1520,
  1600, 1610
);

-- INSPECTOR focuses on inspection tasks, safety hazards and messages.
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 4, `id` FROM `sys_menu`
WHERE `id` IN (
  1000,
  1500, 1530, 1540,
  1600, 1610
);

-- RESIDENT primarily uses the miniapp. No backend management menus are assigned in P0.

SET FOREIGN_KEY_CHECKS = 1;
