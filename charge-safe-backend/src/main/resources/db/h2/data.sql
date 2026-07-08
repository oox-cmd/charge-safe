-- H2 测试数据

-- 角色
INSERT INTO sys_role (id, role_name, role_code, status) VALUES (1, '平台管理员', 'PLATFORM_ADMIN', 1);
INSERT INTO sys_role (id, role_name, role_code, status) VALUES (2, '物业管理员', 'PROPERTY_ADMIN', 1);
INSERT INTO sys_role (id, role_name, role_code, status) VALUES (3, '运维人员', 'MAINTAINER', 1);
INSERT INTO sys_role (id, role_name, role_code, status) VALUES (4, '巡检人员', 'INSPECTOR', 1);
INSERT INTO sys_role (id, role_name, role_code, status) VALUES (5, '居民用户', 'RESIDENT', 1);

-- 小区
INSERT INTO community (id, name, address, lat, lng, status) VALUES (1, '阳光花园小区', '北京市朝阳区阳光路100号', 39.9087, 116.4716, 1);
INSERT INTO community (id, name, address, lat, lng, status) VALUES (2, '翠苑新城', '北京市海淀区中关村南大街12号', 39.9836, 116.3112, 1);

-- 用户（密码都是 123456 的 BCrypt）
INSERT INTO sys_user (id, username, password, nickname, phone, status, community_id) VALUES
(1, 'admin', '$2a$10$Z.VVGSiWhi/kaIGVyz25aeiN37gGHblAP4A/qeUatd5NFMofiufj.', '平台管理员', '13800000001', 1, NULL),
(2, 'property01', '$2a$10$Z.VVGSiWhi/kaIGVyz25aeiN37gGHblAP4A/qeUatd5NFMofiufj.', '物业管理员', '13800000002', 1, 1),
(3, 'maintainer01', '$2a$10$Z.VVGSiWhi/kaIGVyz25aeiN37gGHblAP4A/qeUatd5NFMofiufj.', '运维人员', '13800000003', 1, 1),
(4, 'inspector01', '$2a$10$Z.VVGSiWhi/kaIGVyz25aeiN37gGHblAP4A/qeUatd5NFMofiufj.', '巡检人员', '13800000004', 1, 1),
(5, 'resident01', '$2a$10$Z.VVGSiWhi/kaIGVyz25aeiN37gGHblAP4A/qeUatd5NFMofiufj.', '居民赵六', '13800000005', 1, 1);

-- 用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1,1), (2,2), (3,3), (4,4), (5,5);

-- 车棚
INSERT INTO parking_area (id, community_id, name, location, port_count, status) VALUES
(1, 1, 'A栋充电车棚', '小区东门入口处', 8, 1),
(2, 1, 'B栋充电车棚', '小区西门入口处', 6, 1);

-- 设备
INSERT INTO charging_device (id, parking_area_id, device_code, device_name, device_type, port_count, status) VALUES
(1, 1, 'DEV-001', 'A栋1号充电桩', '智能充电桩', 4, 1),
(2, 1, 'DEV-002', 'A栋2号充电桩', '智能充电桩', 4, 1);

-- 端口
INSERT INTO charging_port (id, device_id, port_number, port_name, status, power_rating) VALUES
(1, 1, 1, 'A1-1号口', 'IDLE', 500.00),
(2, 1, 2, 'A1-2号口', 'CHARGING', 500.00),
(3, 1, 3, 'A1-3号口', 'FAULT', 500.00),
(4, 1, 4, 'A1-4号口', 'IDLE', 500.00),
(5, 2, 1, 'A2-1号口', 'IDLE', 800.00),
(6, 2, 2, 'A2-2号口', 'CHARGING', 800.00),
(7, 2, 3, 'A2-3号口', 'IDLE', 800.00),
(8, 2, 4, 'A2-4号口', 'DISABLED', 800.00);

-- 菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, sort_order, status) VALUES
(1, 0, '仪表盘', 'MENU', '/dashboard', 'dashboard/Index', 'Monitor', 1, 1),
(2, 0, '社区管理', 'MENU', '/community', 'community/Index', 'OfficeBuilding', 2, 1),
(3, 0, '设备管理', 'MENU', '/device', NULL, 'Cpu', 3, 1),
(4, 3, '设备列表', 'MENU', '/device/list', 'device/Index', 'Menu', 1, 1),
(5, 3, '端口管理', 'MENU', '/device/port', 'device/Port', 'Connection', 2, 1),
(6, 0, '充电管理', 'MENU', '/charging', NULL, 'ChromeFilled', 4, 1),
(7, 6, '预约记录', 'MENU', '/charging/reservation', 'charging/Reservation', 'Tickets', 1, 1),
(8, 6, '充电订单', 'MENU', '/charging/order', 'charging/Order', 'Document', 2, 1),
(9, 0, '巡检管理', 'MENU', '/inspection', NULL, 'Warning', 5, 1),
(10, 0, '报修管理', 'MENU', '/repair', NULL, 'Tools', 6, 1),
(11, 0, '统计报表', 'MENU', '/statistics', 'statistics/Index', 'DataLine', 7, 1),
(12, 0, '系统管理', 'MENU', '/system', NULL, 'Setting', 8, 1),
(13, 12, '用户管理', 'MENU', '/system/user', 'system/User', 'User', 1, 1),
(14, 12, '角色管理', 'MENU', '/system/role', 'system/Role', 'Avatar', 2, 1),
(15, 12, '菜单管理', 'MENU', '/system/menu', 'system/Menu', 'Menu', 3, 1),
(16, 0, '通知管理', 'MENU', '/notification', 'notification/Index', 'Bell', 9, 1),
(17, 0, '日志管理', 'MENU', '/log', 'log/Index', 'Document', 10, 1);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;
