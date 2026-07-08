-- ChargeSafe 社区充电安全运维平台 - 数据库表结构
-- MySQL 8.x | InnoDB | utf8mb4
-- Flyway V1: 初始化所有表结构

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户权限线
-- ----------------------------

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名/账号',
  `password` varchar(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `community_id` bigint DEFAULT NULL COMMENT '所属社区ID(物业/运维/巡检人员关联)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_community_id` (`community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
  `operation` varchar(50) NOT NULL COMMENT '操作描述',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
  `params` text DEFAULT NULL COMMENT '请求参数',
  `url` varchar(255) DEFAULT NULL COMMENT '请求URL',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `status` tinyint DEFAULT 1 COMMENT '操作状态: 0-失败 1-成功',
  `error_msg` text DEFAULT NULL COMMENT '错误信息',
  `duration` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 2. 社区资源线
-- ----------------------------

-- 社区表
CREATE TABLE IF NOT EXISTS `community` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '社区ID',
  `name` varchar(100) NOT NULL COMMENT '社区名称',
  `address` varchar(255) DEFAULT NULL COMMENT '社区地址',
  `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系人电话',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区表';

-- 车棚/停放区表
CREATE TABLE IF NOT EXISTS `parking_area` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '车棚ID',
  `community_id` bigint NOT NULL COMMENT '所属社区ID',
  `name` varchar(100) NOT NULL COMMENT '车棚名称',
  `location` varchar(255) DEFAULT NULL COMMENT '位置描述',
  `port_count` int NOT NULL DEFAULT 0 COMMENT '端口总数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_community_id` (`community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车棚/停放区表';

-- 充电设备表
CREATE TABLE IF NOT EXISTS `charging_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `parking_area_id` bigint NOT NULL COMMENT '所属车棚ID',
  `device_code` varchar(50) NOT NULL COMMENT '设备编码',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型',
  `port_count` int NOT NULL DEFAULT 0 COMMENT '端口数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用 2-故障',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_code` (`device_code`),
  KEY `idx_parking_area_id` (`parking_area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电设备表';

-- 充电端口表
CREATE TABLE IF NOT EXISTS `charging_port` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '端口ID',
  `device_id` bigint NOT NULL COMMENT '所属设备ID',
  `port_number` int NOT NULL COMMENT '端口号',
  `port_name` varchar(50) DEFAULT NULL COMMENT '端口名称',
  `status` varchar(30) NOT NULL DEFAULT 'IDLE' COMMENT '状态: IDLE-空闲 RESERVED-已预约 CHARGING-充电中 COMPLETED_WAITING_PICKUP-已完成待取车 OVERTIME_OCCUPIED-超时占用 FAULT-故障 MAINTENANCE-维修中 DISABLED-停用 OFFLINE-离线',
  `power_rating` decimal(10,2) DEFAULT NULL COMMENT '额定功率(W)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_port` (`device_id`, `port_number`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电端口表';

-- ----------------------------
-- 3. 居民与车辆线
-- ----------------------------

-- 居民档案表
CREATE TABLE IF NOT EXISTS `resident_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '档案ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `community_id` bigint NOT NULL COMMENT '所属社区ID',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `building` varchar(50) DEFAULT NULL COMMENT '楼栋',
  `room_number` varchar(20) DEFAULT NULL COMMENT '房号',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_community_id` (`community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='居民档案表';

-- 车辆表
CREATE TABLE IF NOT EXISTS `vehicle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
  `user_id` bigint NOT NULL COMMENT '车主用户ID',
  `plate_number` varchar(20) DEFAULT NULL COMMENT '车牌号',
  `vehicle_type` varchar(20) DEFAULT 'E_BIKE' COMMENT '车辆类型: E_BIKE-电动自行车 E_MOTORCYCLE-电动摩托车',
  `brand` varchar(50) DEFAULT NULL COMMENT '品牌',
  `color` varchar(20) DEFAULT NULL COMMENT '颜色',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- ----------------------------
-- 4. 预约与充电线
-- ----------------------------

-- 充电预约表
CREATE TABLE IF NOT EXISTS `charging_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `user_id` bigint NOT NULL COMMENT '预约用户ID',
  `port_id` bigint NOT NULL COMMENT '预约端口ID',
  `community_id` bigint DEFAULT NULL COMMENT '社区ID',
  `reserve_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
  `expire_time` datetime NOT NULL COMMENT '预约过期时间',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待生效 CONVERTED-已转订单 CANCELLED-用户取消 EXPIRED-超时取消 FAULT_CANCELLED-设备故障取消',
  `cancel_reason` varchar(200) DEFAULT NULL COMMENT '取消原因',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_port_id` (`port_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电预约表';

-- 充电订单表
CREATE TABLE IF NOT EXISTS `charging_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `port_id` bigint NOT NULL COMMENT '端口ID',
  `reservation_id` bigint DEFAULT NULL COMMENT '关联预约ID',
  `community_id` bigint DEFAULT NULL COMMENT '社区ID',
  `start_time` datetime DEFAULT NULL COMMENT '充电开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '充电结束时间',
  `duration_minutes` int DEFAULT NULL COMMENT '充电时长(分钟)',
  `power_consumed` decimal(10,2) DEFAULT NULL COMMENT '用电量(度)',
  `amount` decimal(10,2) DEFAULT 0.00 COMMENT '费用(元)',
  `status` varchar(30) NOT NULL DEFAULT 'CHARGING' COMMENT '状态: CHARGING-充电中 COMPLETED-已完成 SETTLED-已结算 CANCELLED-已取消 ABNORMAL-异常结束',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_port_id` (`port_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电订单表';

-- ----------------------------
-- 5. 报修与工单线
-- ----------------------------

-- 报修记录表
CREATE TABLE IF NOT EXISTS `repair_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报修ID',
  `reporter_id` bigint NOT NULL COMMENT '报修人ID',
  `community_id` bigint NOT NULL COMMENT '社区ID',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `port_id` bigint DEFAULT NULL COMMENT '端口ID',
  `fault_type` varchar(50) DEFAULT NULL COMMENT '故障类型',
  `fault_desc` varchar(500) NOT NULL COMMENT '故障描述',
  `fault_images` text DEFAULT NULL COMMENT '故障照片(JSON数组)',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待处理 CONFIRMED-已确认 DISPATCHED-已派单 REJECTED-已驳回',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_community_id` (`community_id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修记录表';

-- 维修工单表
CREATE TABLE IF NOT EXISTS `repair_ticket` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `ticket_no` varchar(50) NOT NULL COMMENT '工单编号',
  `report_id` bigint DEFAULT NULL COMMENT '关联报修ID',
  `community_id` bigint NOT NULL COMMENT '社区ID',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `port_id` bigint DEFAULT NULL COMMENT '端口ID',
  `dispatcher_id` bigint DEFAULT NULL COMMENT '派单人ID',
  `assignee_id` bigint DEFAULT NULL COMMENT '维修人员ID',
  `fault_desc` varchar(500) DEFAULT NULL COMMENT '故障描述',
  `repair_requirement` varchar(500) DEFAULT NULL COMMENT '维修要求',
  `repair_result` varchar(500) DEFAULT NULL COMMENT '维修结果',
  `repair_images` text DEFAULT NULL COMMENT '维修照片(JSON数组)',
  `status` varchar(30) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待受理 DISPATCHED-已派单 PROCESSING-处理中 WAITING_ACCEPTANCE-待验收 FINISHED-已完成 CLOSED-已关闭 REJECTED-已驳回',
  `dispatch_time` datetime DEFAULT NULL COMMENT '派单时间',
  `accept_time` datetime DEFAULT NULL COMMENT '接单时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_no` (`ticket_no`),
  KEY `idx_report_id` (`report_id`),
  KEY `idx_community_id` (`community_id`),
  KEY `idx_assignee_id` (`assignee_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单表';

-- 工单日志表
CREATE TABLE IF NOT EXISTS `repair_ticket_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `ticket_id` bigint NOT NULL COMMENT '工单ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `action` varchar(50) NOT NULL COMMENT '操作类型',
  `content` varchar(500) DEFAULT NULL COMMENT '操作内容',
  `from_status` varchar(30) DEFAULT NULL COMMENT '原状态',
  `to_status` varchar(30) DEFAULT NULL COMMENT '新状态',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ticket_id` (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单日志表';

-- ----------------------------
-- 6. 巡检与隐患线
-- ----------------------------

-- 巡检任务表
CREATE TABLE IF NOT EXISTS `inspection_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `community_id` bigint NOT NULL COMMENT '社区ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_desc` varchar(500) DEFAULT NULL COMMENT '任务描述',
  `inspector_id` bigint DEFAULT NULL COMMENT '巡检人员ID',
  `plan_start_time` datetime DEFAULT NULL COMMENT '计划开始时间',
  `plan_end_time` datetime DEFAULT NULL COMMENT '计划结束时间',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待执行 IN_PROGRESS-执行中 COMPLETED-已完成 ABNORMAL-发现异常 OVERDUE-已逾期 CANCELLED-已取消',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_community_id` (`community_id`),
  KEY `idx_inspector_id` (`inspector_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检任务表';

-- 巡检记录表
CREATE TABLE IF NOT EXISTS `inspection_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `task_id` bigint NOT NULL COMMENT '巡检任务ID',
  `inspector_id` bigint NOT NULL COMMENT '巡检人员ID',
  `check_item` varchar(100) NOT NULL COMMENT '检查项目',
  `check_result` varchar(20) NOT NULL COMMENT '检查结果: NORMAL-正常 ABNORMAL-异常',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `images` text DEFAULT NULL COMMENT '照片(JSON数组)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检记录表';

-- 安全隐患表
CREATE TABLE IF NOT EXISTS `safety_hazard` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '隐患ID',
  `community_id` bigint NOT NULL COMMENT '社区ID',
  `task_id` bigint DEFAULT NULL COMMENT '关联巡检任务ID',
  `reporter_id` bigint NOT NULL COMMENT '上报人ID',
  `hazard_type` varchar(50) NOT NULL COMMENT '隐患类型',
  `severity` varchar(20) NOT NULL DEFAULT 'GENERAL' COMMENT '严重程度: GENERAL-一般 SERIOUS-严重 URGENT-紧急',
  `description` varchar(500) NOT NULL COMMENT '隐患描述',
  `images` text DEFAULT NULL COMMENT '照片(JSON数组)',
  `status` varchar(30) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待处理 RECTIFYING-整改中 WAITING_REVIEW-待复查 REVIEW_PASSED-复查通过 REVIEW_FAILED-复查不通过 CLOSED-已关闭',
  `rectifier_id` bigint DEFAULT NULL COMMENT '整改人ID',
  `rectify_desc` varchar(500) DEFAULT NULL COMMENT '整改情况描述',
  `rectify_images` text DEFAULT NULL COMMENT '整改照片(JSON数组)',
  `rectify_time` datetime DEFAULT NULL COMMENT '整改时间',
  `reviewer_id` bigint DEFAULT NULL COMMENT '复查人ID',
  `review_result` varchar(20) DEFAULT NULL COMMENT '复查结果',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '复查备注',
  `review_time` datetime DEFAULT NULL COMMENT '复查时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_community_id` (`community_id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全隐患表';

-- ----------------------------
-- 7. 消息与统计线
-- ----------------------------

-- 消息通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) NOT NULL COMMENT '消息标题',
  `content` varchar(500) DEFAULT NULL COMMENT '消息内容',
  `type` varchar(30) NOT NULL COMMENT '消息类型: RESERVATION-预约相关 CHARGING-充电相关 REPAIR-报修工单 INSPECTION-巡检相关 HAZARD-隐患相关 SYSTEM-系统通知',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `business_id` bigint DEFAULT NULL COMMENT '业务ID',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 告警记录表
CREATE TABLE IF NOT EXISTS `alarm_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '告警ID',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `port_id` bigint DEFAULT NULL COMMENT '端口ID',
  `community_id` bigint DEFAULT NULL COMMENT '社区ID',
  `alarm_type` varchar(50) NOT NULL COMMENT '告警类型',
  `alarm_content` varchar(500) NOT NULL COMMENT '告警内容',
  `alarm_level` varchar(20) DEFAULT 'WARNING' COMMENT '告警级别: INFO-提示 WARNING-警告 ERROR-严重',
  `status` varchar(20) NOT NULL DEFAULT 'UNHANDLED' COMMENT '状态: UNHANDLED-未处理 HANDLED-已处理',
  `handled_by` bigint DEFAULT NULL COMMENT '处理人ID',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';

-- 充电费率规则表
CREATE TABLE IF NOT EXISTS `charging_fee_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `community_id` bigint NOT NULL COMMENT '社区ID',
  `rule_name` varchar(50) NOT NULL COMMENT '规则名称',
  `fee_per_hour` decimal(10,2) NOT NULL COMMENT '每小时费用(元)',
  `min_fee` decimal(10,2) DEFAULT 0.00 COMMENT '最低收费(元)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_community_id` (`community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电费率规则表';

SET FOREIGN_KEY_CHECKS = 1;
