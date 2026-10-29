-- 考试管理页面已补齐，启用原先停用的菜单和按钮。
-- 请先执行 education_menu_permissions.sql，再在同一个项目数据库执行本文件。
-- 可重复执行；普通角色仍需在“角色管理”中分配菜单和按钮，执行后请退出并重新登录。
SET NAMES utf8mb4;
START TRANSACTION;

UPDATE sys_menu
SET status = '0', visible = '0', remark = '考试信息管理',
    update_by = 'admin', update_time = NOW()
WHERE menu_type = 'C'
  AND component = 'system/exam/index'
  AND perms = 'system:exam:list';

UPDATE sys_menu button_menu
INNER JOIN sys_menu page_menu ON button_menu.parent_id = page_menu.menu_id
SET button_menu.status = '0', button_menu.update_by = 'admin', button_menu.update_time = NOW()
WHERE page_menu.menu_type = 'C'
  AND page_menu.component = 'system/exam/index'
  AND page_menu.perms = 'system:exam:list'
  AND button_menu.menu_type = 'F'
  AND button_menu.perms IN (
    'system:exam:query', 'system:exam:add', 'system:exam:edit',
    'system:exam:remove', 'system:exam:export'
  );

COMMIT;

SELECT menu_id, menu_name, component, perms, status
FROM sys_menu
WHERE menu_type = 'C' AND component = 'system/exam/index'
  AND perms = 'system:exam:list';
