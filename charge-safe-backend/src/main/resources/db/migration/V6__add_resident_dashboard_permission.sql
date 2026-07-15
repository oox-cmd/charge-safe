-- ChargeSafe V6: hidden permission for the authenticated resident dashboard.
-- This permission is not attached to the management menu tree.

INSERT INTO `sys_menu`
(`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission_code`, `icon`, `sort_order`, `status`)
SELECT 0, '居民充电首页访问', 3, NULL, NULL, 'resident:dashboard:view', NULL, 900, 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu`
    WHERE `permission_code` = 'resident:dashboard:view'
      AND `deleted` = 0
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role_record.`id`, permission_record.`id`
FROM `sys_role` role_record
JOIN `sys_menu` permission_record
  ON permission_record.`permission_code` = 'resident:dashboard:view'
 AND permission_record.`deleted` = 0
WHERE role_record.`role_code` = 'RESIDENT'
  AND role_record.`deleted` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` existing_binding
      WHERE existing_binding.`role_id` = role_record.`id`
        AND existing_binding.`menu_id` = permission_record.`id`
  );
