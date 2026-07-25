-- AI 助手统计仅供平台管理员查看；作为隐藏权限登记，不新增未配置路由的管理菜单。
INSERT INTO `sys_menu`
(`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission_code`, `icon`, `sort_order`, `status`)
SELECT 0, 'AI 助手统计', 3, NULL, NULL, 'assistant:stats:view', NULL, 920, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu`
  WHERE `permission_code` = 'assistant:stats:view' AND `deleted` = 0
);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role_record.`id`, permission_record.`id`
FROM `sys_role` role_record
JOIN `sys_menu` permission_record
  ON permission_record.`permission_code` = 'assistant:stats:view'
 AND permission_record.`deleted` = 0
WHERE role_record.`role_code` = 'PLATFORM_ADMIN'
  AND role_record.`deleted` = 0;
