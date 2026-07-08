-- ChargeSafe 社区充电安全运维平台 - 初始数据
-- 密码均为: 123456 (BCrypt加密)

SET NAMES utf8mb4;

-- ----------------------------
-- 1. 初始化角色
-- ----------------------------
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`) VALUES
(1, '平台管理员', 'PLATFORM_ADMIN', '系统最高权限管理者', 1),
(2, '物业管理员', 'PROPERTY_ADMIN', '社区级管理者', 1),
(3, '运维人员', 'MAINTAINER', '设备维修执行者', 1),
(4, '巡检人员', 'INSPECTOR', '安全巡检执行者', 1),
(5, '居民用户', 'RESIDENT', '充电服务使用者', 1);

-- ----------------------------
-- 2. 初始化用户 (密码: 123456)
-- BCrypt加密: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PsLJfGqKCQHnHlQqj7Py6
-- ----------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `phone`, `status`, `community_id`) VALUES
(1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PsLJfGqKCQHnHlQqj7Py6', '平台管理员', '13800000001', 1, NULL),
(2, 'property01', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PsLJfGqKCQHnHlQqj7Py6', '物业管理员张三', '13800000002', 1, 1),
(3, 'maintainer01', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PsLJfGqKCQHnHlQqj7Py6', '运维人员李四', '13800000003', 1, 1),
(4, 'inspector01', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PsLJfGqKCQHnHlQqj7Py6', '巡检人员王五', '13800000004', 1, 1),
(5, 'resident01', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PsLJfGqKCQHnHlQqj7Py6', '居民赵六', '13800000005', 1, 1),
(6, 'resident02', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PsLJfGqKCQHnHlQqj7Py6', '居民钱七', '13800000006', 1, 1);

-- ----------------------------
-- 3. 初始化用户角色关联
-- ----------------------------
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> 平台管理员
(2, 2),  -- property01 -> 物业管理员
(3, 3),  -- maintainer01 -> 运维人员
(4, 4),  -- inspector01 -> 巡检人员
(5, 5),  -- resident01 -> 居民用户
(6, 5);  -- resident02 -> 居民用户

-- ----------------------------
-- 4. 初始化社区
-- ----------------------------
INSERT INTO `community` (`id`, `name`, `address`, `contact_name`, `contact_phone`, `status`) VALUES
(1, '阳光花园小区', '北京市朝阳区阳光路100号', '张三', '13800000002', 1);

-- ----------------------------
-- 5. 初始化车棚
-- ----------------------------
INSERT INTO `parking_area` (`id`, `community_id`, `name`, `location`, `port_count`, `status`) VALUES
(1, 1, 'A栋充电车棚', '小区东门入口处', 8, 1),
(2, 1, 'B栋充电车棚', '小区西门入口处', 6, 1);

-- ----------------------------
-- 6. 初始化充电设备
-- ----------------------------
INSERT INTO `charging_device` (`id`, `parking_area_id`, `device_code`, `device_name`, `device_type`, `port_count`, `status`) VALUES
(1, 1, 'DEV-001', 'A栋1号充电桩', '智能充电桩', 4, 1),
(2, 1, 'DEV-002', 'A栋2号充电桩', '智能充电桩', 4, 1),
(3, 2, 'DEV-003', 'B栋1号充电桩', '智能充电桩', 3, 1),
(4, 2, 'DEV-004', 'B栋2号充电桩', '智能充电桩', 3, 1);

-- ----------------------------
-- 7. 初始化充电端口
-- ----------------------------
INSERT INTO `charging_port` (`id`, `device_id`, `port_number`, `port_name`, `status`, `power_rating`) VALUES
-- A栋1号充电桩 4个端口
(1, 1, 1, 'A1-1号口', 'IDLE', 500.00),
(2, 1, 2, 'A1-2号口', 'IDLE', 500.00),
(3, 1, 3, 'A1-3号口', 'FAULT', 500.00),
(4, 1, 4, 'A1-4号口', 'IDLE', 500.00),
-- A栋2号充电桩 4个端口
(5, 2, 1, 'A2-1号口', 'IDLE', 500.00),
(6, 2, 2, 'A2-2号口', 'CHARGING', 500.00),
(7, 2, 3, 'A2-3号口', 'IDLE', 500.00),
(8, 2, 4, 'A2-4号口', 'DISABLED', 500.00),
-- B栋1号充电桩 3个端口
(9, 3, 1, 'B1-1号口', 'IDLE', 800.00),
(10, 3, 2, 'B1-2号口', 'IDLE', 800.00),
(11, 3, 3, 'B1-3号口', 'IDLE', 800.00),
-- B栋2号充电桩 3个端口
(12, 4, 1, 'B2-1号口', 'IDLE', 800.00),
(13, 4, 2, 'B2-2号口', 'MAINTENANCE', 800.00),
(14, 4, 3, 'B2-3号口', 'IDLE', 800.00);

-- ----------------------------
-- 8. 初始化居民档案
-- ----------------------------
INSERT INTO `resident_profile` (`id`, `user_id`, `community_id`, `real_name`, `phone`, `building`, `room_number`) VALUES
(1, 5, 1, '赵六', '13800000005', 'A栋', '101'),
(2, 6, 1, '钱七', '13800000006', 'B栋', '202');

-- ----------------------------
-- 9. 初始化车辆
-- ----------------------------
INSERT INTO `vehicle` (`id`, `user_id`, `plate_number`, `vehicle_type`, `brand`, `color`) VALUES
(1, 5, '京A12345', 'E_BIKE', '雅迪', '白色'),
(2, 6, '京B67890', 'E_BIKE', '爱玛', '黑色');

-- ----------------------------
-- 10. 初始化充电费率规则
-- ----------------------------
INSERT INTO `charging_fee_rule` (`id`, `community_id`, `rule_name`, `fee_per_hour`, `min_fee`, `status`) VALUES
(1, 1, '标准费率', 1.50, 0.50, 1);

-- ----------------------------
-- 11. 初始化部分测试数据
-- ----------------------------

-- 一个已完成的充电订单示例
INSERT INTO `charging_order` (`id`, `order_no`, `user_id`, `port_id`, `community_id`, `start_time`, `end_time`, `duration_minutes`, `amount`, `status`) VALUES
(1, 'ORD202601010001', 5, 1, 1, '2026-01-01 08:00:00', '2026-01-01 10:30:00', 150, 3.75, 'COMPLETED');

-- 一条待处理的报修记录
INSERT INTO `repair_report` (`id`, `reporter_id`, `community_id`, `device_id`, `port_id`, `fault_type`, `fault_desc`, `status`) VALUES
(1, 5, 1, 1, 3, 'DISPLAY_FAULT', '端口显示屏不亮，无法扫码', 'PENDING');

-- 一条待执行的巡检任务
INSERT INTO `inspection_task` (`id`, `community_id`, `task_name`, `task_desc`, `inspector_id`, `plan_start_time`, `plan_end_time`, `status`, `created_by`) VALUES
(1, 1, 'A栋车棚日常巡检', '检查A栋车棚所有充电设备运行状态', 4, '2026-07-01 09:00:00', '2026-07-01 18:00:00', 'PENDING', 2);

-- 一条未读通知
INSERT INTO `notification` (`id`, `user_id`, `title`, `content`, `type`, `business_type`, `business_id`, `is_read`) VALUES
(1, 5, '充电完成通知', '您在A栋1号充电桩1号口的充电已完成，请及时取车。', 'CHARGING', 'CHARGING_ORDER', 1, 0);
