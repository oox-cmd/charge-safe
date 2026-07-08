-- ChargeSafe V4: 补充物业管理员菜单权限
-- 为 PROPERTY_ADMIN(role_id=2) 补充 "社区列表"(menu_id=1210, permission_code=community:list:view) 权限，
-- 使其可查看所属社区列表。社区/车棚/设备等管理均依赖社区列表入口，原 V3 遗漏该按钮级菜单。
-- 保持 V1/V2/V3 不变，仅追加角色菜单关联。

SET NAMES utf8mb4;

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, `id` FROM `sys_menu` WHERE `id` = 1210 AND `deleted` = 0;
