-- 独立预警记录页面已补齐，启用原先停用的菜单和按钮。
-- 请先执行 education_menu_permissions.sql，再在同一个项目数据库执行本文件。
-- 可重复执行；仅启用与此页面对应的菜单及四项按钮权限，不改动其他模块。
-- 普通角色仍需在“角色管理”中分配菜单和按钮；执行后退出并重新登录。
SET NAMES utf8mb4;
START TRANSACTION;

UPDATE sys_menu
SET status = '0', visible = '0',
    remark = '预警记录管理：查询、详情、处理、删除、导出',
    update_by = 'admin', update_time = NOW()
WHERE menu_type = 'C'
  AND component = 'system/warningRecord/index'
  AND perms = 'system:warning-record:list';

UPDATE sys_menu button_menu
INNER JOIN sys_menu page_menu ON button_menu.parent_id = page_menu.menu_id
SET button_menu.status = '0', button_menu.update_by = 'admin', button_menu.update_time = NOW()
WHERE page_menu.menu_type = 'C'
  AND page_menu.component = 'system/warningRecord/index'
  AND page_menu.perms = 'system:warning-record:list'
  AND button_menu.menu_type = 'F'
  AND button_menu.perms IN (
    'system:warning-record:query', 'system:warning-record:edit',
    'system:warning-record:remove', 'system:warning-record:export'
  );

COMMIT;

-- 应能查到一个菜单，status 应为 0；若没有结果，请先执行前置菜单脚本。
SELECT menu_id, menu_name, component, perms, status
FROM sys_menu
WHERE menu_type = 'C' AND component = 'system/warningRecord/index'
  AND perms = 'system:warning-record:list';
