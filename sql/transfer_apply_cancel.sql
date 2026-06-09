-- 转班申请撤回功能增量脚本
-- 用途：给已有数据库补充“已撤回”状态字典和“撤回”按钮权限。

INSERT INTO sys_dict_data
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '已撤回', '4', 'transfer_apply_status', '', 'info', 'N', '0', 'admin', NOW(), ''
WHERE NOT EXISTS (
  SELECT 1
  FROM sys_dict_data
  WHERE dict_type = 'transfer_apply_status'
    AND dict_value = '4'
);

SET @transfer_apply_menu_id = (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'system:student:transferApprove:list'
  LIMIT 1
);

INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '转班审批撤回', @transfer_apply_menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:student:transferApprove:cancel', '#', 'admin', NOW(), 'admin', NOW(), ''
WHERE @transfer_apply_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE parent_id = @transfer_apply_menu_id
      AND perms = 'system:student:transferApprove:cancel'
  );

SET @cancel_menu_id = (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'system:student:transferApprove:cancel'
  LIMIT 1
);

SET @admin_role_id = (
  SELECT role_id
  FROM sys_role
  WHERE role_key = 'admin'
  LIMIT 1
);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, @cancel_menu_id
WHERE @admin_role_id IS NOT NULL
  AND @cancel_menu_id IS NOT NULL;

SET @class_teacher_role_id = (
  SELECT role_id
  FROM sys_role
  WHERE role_key = 'class_teacher'
  LIMIT 1
);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @class_teacher_role_id, @cancel_menu_id
WHERE @class_teacher_role_id IS NOT NULL
  AND @cancel_menu_id IS NOT NULL;
