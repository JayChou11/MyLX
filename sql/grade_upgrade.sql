-- =============================================
-- 自动升年级模块 - 数据库变更脚本
-- 执行前提：已执行 class_module.sql
-- =============================================

-- 1. 新增年级顺序字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('年级顺序', 'student_grade_order', '0', 'admin', sysdate(), '年级升序排列，用于自动升年级');

-- 2. 新增年级顺序字典数据（dict_sort 决定顺序，数字越小年级越低）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1, '初一', '初一', 'student_grade_order', '', 'default', 'N', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (2, '初二', '初二', 'student_grade_order', '', 'default', 'N', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (3, '初三', '初三', 'student_grade_order', '', 'default', 'N', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (4, '高一', '高一', 'student_grade_order', '', 'default', 'N', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (5, '高二', '高二', 'student_grade_order', '', 'default', 'N', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (6, '高三', '高三', 'student_grade_order', '', 'default', 'N', '0', 'admin', sysdate(), '');

-- 3. 新增升年级按钮权限（挂在学生信息菜单下）
SET @student_parent_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND menu_name = '学生管理' LIMIT 1);
SET @student_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '学生信息' LIMIT 1);

SET @student_upgrade_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:upgrade' LIMIT 1);
SET @student_upgrade_id = IFNULL(@student_upgrade_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @student_upgrade_id, '升年级', @student_menu_id, 7, '', '', '', '',
  1, 0, 'F', '0', '0', 'system:student:upgrade', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:upgrade'
);

-- 4. 给管理员角色授权升年级权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.perms = 'system:student:upgrade'
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
);

-- 5. 注册定时任务（每年7月1日0点执行，默认暂停状态，需手动启用）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('自动升年级', 'DEFAULT', 'gradeTask.upgradeGrade', '0 0 0 1 7 ?', '3', '1', '1', 'admin', sysdate(), '每年7月1日0点自动执行学生升年级，默认暂停需手动启用');