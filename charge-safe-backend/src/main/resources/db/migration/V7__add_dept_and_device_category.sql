-- ----------------------------
-- 1. 部门管理表 sys_dept
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `parent_id` bigint DEFAULT NULL COMMENT '上级部门ID',
  `name` varchar(50) NOT NULL COMMENT '部门名称',
  `code` varchar(50) NOT NULL COMMENT '部门编码',
  `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `sort` int NOT NULL DEFAULT 1 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ----------------------------
-- 初始化部门基础数据
-- ----------------------------
INSERT IGNORE INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `leader`, `phone`, `sort`, `status`, `create_time`) VALUES
(1, NULL, 'ChargeSafe集团', 'CS_GROUP', '张健', '18888888888', 1, 1, '2026-07-01 10:00:00'),
(2, 1, '系统研发部', 'DEPT_RD', '金昊然', '13900000001', 1, 1, '2026-07-01 10:30:00'),
(3, 1, '项目运营部', 'DEPT_OPS', '吴诗颖', '13900000002', 2, 1, '2026-07-01 10:35:00'),
(4, 1, '财务管理部', 'DEPT_FI', '王小红', '13900000003', 3, 1, '2026-07-01 10:40:00'),
(5, 3, '华北区域运营组', 'OPS_NORTH', '李华', '13910000001', 1, 1, '2026-07-01 11:00:00'),
(6, 3, '华东区域运营组', 'OPS_EAST', '张明', '13920000002', 2, 1, '2026-07-01 11:05:00'),
(7, 3, '华南区域运营组', 'OPS_SOUTH', '赵磊', '13930000003', 3, 1, '2026-07-01 11:10:00');

-- ----------------------------
-- 2. 设备分类表 device_category
-- ----------------------------
CREATE TABLE IF NOT EXISTS `device_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `code` varchar(50) NOT NULL COMMENT '分类编码',
  `power` varchar(20) NOT NULL COMMENT '适用功率',
  `description` varchar(500) DEFAULT NULL COMMENT '描述说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分类表';

-- ----------------------------
-- 初始化电动自行车充电设备分类
-- ----------------------------
INSERT IGNORE INTO `device_category` (`id`, `name`, `code`, `power`, `description`, `status`, `create_time`) VALUES
(1, '标准电动自行车充电桩', 'E_BIKE_STANDARD', '350W', '适用于小区集中车棚的常规电动自行车充电。', 1, '2026-07-01 10:00:00'),
(2, '智能功率分配充电桩', 'E_BIKE_SMART', '500W', '支持多端口动态功率分配，适用于高使用率车棚。', 1, '2026-07-01 10:15:00'),
(3, '高功率共享充电桩', 'E_BIKE_HIGH_POWER', '800W', '适用于经供电容量核验后允许较高功率充电的车棚。', 1, '2026-07-01 10:20:00'),
(4, '低功率充电插座', 'E_BIKE_SOCKET', '200W', '适用于零散车位的基础电动自行车充电场景。', 1, '2026-07-02 09:00:00');

-- 业务关联：部门不再只是独立字典，设备分类可实际绑定到充电设备。
ALTER TABLE `sys_user`
  ADD COLUMN `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID' AFTER `community_id`,
  ADD KEY `idx_dept_id` (`dept_id`);

ALTER TABLE `charging_device`
  ADD COLUMN `category_id` bigint DEFAULT NULL COMMENT '设备分类ID' AFTER `device_type`,
  ADD KEY `idx_category_id` (`category_id`);

-- ----------------------------
-- 3. 添加权限数据 (sys_menu, sys_role_menu)
-- ----------------------------
INSERT IGNORE INTO `sys_menu`
(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission_code`, `icon`, `sort_order`, `status`)
VALUES
(1140, 1100, '部门管理', 2, '/permission/system/dept', 'permission/system/Department', 'system:dept:view', 'OfficeBuilding', 24, 1),
(1141, 1140, '新增部门', 3, NULL, NULL, 'system:dept:create', NULL, 241, 1),
(1142, 1140, '编辑部门', 3, NULL, NULL, 'system:dept:edit', NULL, 242, 1),
(1143, 1140, '删除部门', 3, NULL, NULL, 'system:dept:delete', NULL, 243, 1),
(1330, 1300, '设备分类', 2, '/permission/device-category', 'permission/device-category/Index', 'device:category:view', 'Grid', 43, 1),
(1331, 1330, '新增分类', 3, NULL, NULL, 'device:category:create', NULL, 431, 1),
(1332, 1330, '编辑分类', 3, NULL, NULL, 'device:category:edit', NULL, 432, 1),
(1333, 1330, '删除分类', 3, NULL, NULL, 'device:category:delete', NULL, 433, 1);

INSERT IGNORE INTO `sys_menu`
(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission_code`, `icon`, `sort_order`, `status`)
VALUES
(1340, 1300, '设备模拟', 2, '/permission/simulator', 'simulator/Index', 'simulator:manage', 'Cpu', 44, 1),
(1730, 1700, '三维管理大屏', 2, '/bigscreen3d', 'bigscreen3d/Index', 'statistics:bigscreen:view', 'Platform', 83, 1);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `sys_menu` WHERE `id` IN (1140, 1141, 1142, 1143, 1330, 1331, 1332, 1333, 1340, 1730);

-- 物业管理员可在设备登记时选择已启用分类，并可访问其既有的管理大屏入口。
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, `id` FROM `sys_menu` WHERE `id` IN (1330, 1730);

-- 居民端权限不进入管理菜单树，仅用于本人报修的创建与查询。
INSERT INTO `sys_menu`
(`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission_code`, `icon`, `sort_order`, `status`)
SELECT 0, '居民报修提交', 3, NULL, NULL, 'resident:repair:create', NULL, 910, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `permission_code` = 'resident:repair:create' AND `deleted` = 0
);

INSERT INTO `sys_menu`
(`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission_code`, `icon`, `sort_order`, `status`)
SELECT 0, '居民报修查询', 3, NULL, NULL, 'resident:repair:view', NULL, 911, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `permission_code` = 'resident:repair:view' AND `deleted` = 0
);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role_record.`id`, permission_record.`id`
FROM `sys_role` role_record
JOIN `sys_menu` permission_record
  ON permission_record.`permission_code` IN ('resident:repair:create', 'resident:repair:view')
 AND permission_record.`deleted` = 0
WHERE role_record.`role_code` = 'RESIDENT'
  AND role_record.`deleted` = 0;
