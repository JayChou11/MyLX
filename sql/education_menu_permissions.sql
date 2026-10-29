-- =============================================
-- 教师管理、考试管理、成绩预警：菜单和按钮权限补充脚本
-- 兼容 MySQL 5.7 / 8.0；请先在客户端选择项目数据库，再完整执行本文件。
-- 执行前提：已执行 ry_20260321.sql、student_module_20260417.sql、
-- sys_exam.sql 和 sys_score_warning_record.sql。
-- 菜单挂载位置：系统管理 -> 学生管理，与现有业务模块保持一致。
-- 权限标识与当前 Controller 一致，重复执行不会重复插入菜单或授权。
-- 仅补充缺失项，保留已存在菜单的路径、状态及角色授权。
--
-- 当前前端页面情况：
-- 教师管理：system/teacher/index 已存在，新菜单默认正常。
-- 考试管理：system/exam/index 已存在，新菜单及其按钮正常启用。
-- 成绩预警：system/warningRecord/index 已存在，新菜单及其按钮默认正常。
-- 若曾执行旧版脚本，请补执行 warning_record_menu_enable.sql 启用原先停用的预警菜单和按钮。
-- 若曾执行旧版脚本，请补执行 exam_menu_enable.sql 启用原先停用的考试菜单和按钮。
-- 现有学生成绩页面中的“成绩预警”弹窗仍使用 system:studentScore:query，
-- 此处的“成绩预警”菜单对应 /system/warning-record 的预警记录管理接口。
-- =============================================

SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 补齐学生管理目录（正常情况下已由学生模块脚本创建）。
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
SELECT '学生管理', 1, 10, 'student', '', '', '',
       1, 0, 'M', '0', '0', '', 'user',
       'admin', NOW(), '', NULL, '学生管理目录'
FROM dual
WHERE EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1 AND menu_type = 'M')
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE parent_id = 1 AND menu_name = '学生管理'
  );

SET @education_parent_id = (
  SELECT menu_id FROM sys_menu
  WHERE parent_id = 1 AND menu_name = '学生管理' AND menu_type = 'M'
  ORDER BY menu_id LIMIT 1
);

-- 2. 教师管理菜单。
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
SELECT '教师管理', @education_parent_id, 6, 'teacher', 'system/teacher/index', '', 'SysTeacher',
       1, 0, 'C', '0', '0', 'system:teacher:list', 'user',
       'admin', NOW(), '', NULL, '教师信息管理'
FROM dual
WHERE @education_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu
    WHERE parent_id = @education_parent_id AND menu_type = 'C'
      AND (perms = 'system:teacher:list' OR menu_name = '教师管理')
  );

SET @education_teacher_id = (
  SELECT menu_id FROM sys_menu
  WHERE parent_id = @education_parent_id AND menu_type = 'C'
    AND (perms = 'system:teacher:list' OR menu_name = '教师管理')
  ORDER BY menu_id LIMIT 1
);

-- 3. 考试管理菜单：预留组件路径，默认停用。
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
SELECT '考试管理', @education_parent_id, 7, 'exam', 'system/exam/index', '', 'SysExam',
       1, 0, 'C', '0', '0', 'system:exam:list', 'education',
       'admin', NOW(), '', NULL, '考试信息管理'
FROM dual
WHERE @education_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu
    WHERE parent_id = @education_parent_id AND menu_type = 'C'
      AND (perms = 'system:exam:list' OR menu_name = '考试管理')
  );

SET @education_exam_id = (
  SELECT menu_id FROM sys_menu
  WHERE parent_id = @education_parent_id AND menu_type = 'C'
    AND (perms = 'system:exam:list' OR menu_name = '考试管理')
  ORDER BY menu_id LIMIT 1
);

-- 4. 成绩预警菜单：独立预警记录页面。
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
SELECT '成绩预警', @education_parent_id, 9, 'warningRecord', 'system/warningRecord/index', '', 'SysWarningRecord',
       1, 0, 'C', '0', '0', 'system:warning-record:list', 'message',
       'admin', NOW(), '', NULL, '预警记录管理：查询、详情、处理、删除、导出'
FROM dual
WHERE @education_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu
    WHERE parent_id = @education_parent_id AND menu_type = 'C'
      AND (perms = 'system:warning-record:list' OR menu_name = '成绩预警')
  );

SET @education_warning_id = (
  SELECT menu_id FROM sys_menu
  WHERE parent_id = @education_parent_id AND menu_type = 'C'
    AND (perms = 'system:warning-record:list' OR menu_name = '成绩预警')
  ORDER BY menu_id LIMIT 1
);

-- 5. 按 Controller 的真实接口补齐按钮权限。
-- 新按钮沿用所属菜单的状态，已存在按钮保持原状。
-- 预警记录由成绩业务生成，没有新增接口，所以不创建 add 权限。
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
SELECT p.menu_name, p.parent_id, p.order_num, '#', '', '', '',
       1, 0, 'F', '0', parent_menu.status, p.perms, '#',
       'admin', NOW(), '', NULL, ''
FROM (
  SELECT '教师查询' AS menu_name, @education_teacher_id AS parent_id, 1 AS order_num, 'system:teacher:query' AS perms
  UNION ALL SELECT '教师新增', @education_teacher_id, 2, 'system:teacher:add'
  UNION ALL SELECT '教师修改', @education_teacher_id, 3, 'system:teacher:edit'
  UNION ALL SELECT '教师删除', @education_teacher_id, 4, 'system:teacher:remove'
  UNION ALL SELECT '教师导出', @education_teacher_id, 5, 'system:teacher:export'
  UNION ALL SELECT '考试查询', @education_exam_id, 1, 'system:exam:query'
  UNION ALL SELECT '考试新增', @education_exam_id, 2, 'system:exam:add'
  UNION ALL SELECT '考试修改', @education_exam_id, 3, 'system:exam:edit'
  UNION ALL SELECT '考试删除', @education_exam_id, 4, 'system:exam:remove'
  UNION ALL SELECT '考试导出', @education_exam_id, 5, 'system:exam:export'
  UNION ALL SELECT '预警查询', @education_warning_id, 1, 'system:warning-record:query'
  UNION ALL SELECT '预警处理', @education_warning_id, 2, 'system:warning-record:edit'
  UNION ALL SELECT '预警删除', @education_warning_id, 3, 'system:warning-record:remove'
  UNION ALL SELECT '预警导出', @education_warning_id, 4, 'system:warning-record:export'
) p
INNER JOIN sys_menu parent_menu ON parent_menu.menu_id = p.parent_id
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu m
  WHERE m.parent_id = p.parent_id AND m.perms = p.perms
);

-- 6. 给 admin 角色补充目录、菜单和上述按钮授权。
-- 普通角色请在角色管理中按需分配，不自动授予班主任或教务处角色。
SET @education_admin_role_id = (
  SELECT role_id FROM sys_role
  WHERE role_key = 'admin' AND del_flag = '0'
  ORDER BY role_id LIMIT 1
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @education_admin_role_id, m.menu_id
FROM sys_menu m
WHERE @education_admin_role_id IS NOT NULL
  AND (
    m.menu_id IN (1, @education_parent_id, @education_teacher_id, @education_exam_id, @education_warning_id)
    OR (m.parent_id = @education_teacher_id AND m.menu_type = 'F' AND m.perms IN (
      'system:teacher:query', 'system:teacher:add', 'system:teacher:edit', 'system:teacher:remove', 'system:teacher:export'
    ))
    OR (m.parent_id = @education_exam_id AND m.menu_type = 'F' AND m.perms IN (
      'system:exam:query', 'system:exam:add', 'system:exam:edit', 'system:exam:remove', 'system:exam:export'
    ))
    OR (m.parent_id = @education_warning_id AND m.menu_type = 'F' AND m.perms IN (
      'system:warning-record:query', 'system:warning-record:edit', 'system:warning-record:remove', 'system:warning-record:export'
    ))
  )
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = @education_admin_role_id AND rm.menu_id = m.menu_id
  );

COMMIT;

-- 7. 核对生成结果：正常为 3 个菜单、14 个按钮。
-- 若前置脚本不完整导致父目录或 admin 角色缺失，对应检查值会是 NULL。
SELECT DATABASE() AS database_name, @education_parent_id AS student_directory_id,
       @education_admin_role_id AS admin_role_id;

SELECT menu_id, parent_id, menu_name, menu_type, component, perms, status
FROM sys_menu
WHERE menu_id IN (@education_teacher_id, @education_exam_id, @education_warning_id)
   OR (menu_type = 'F' AND parent_id IN (@education_teacher_id, @education_exam_id, @education_warning_id))
ORDER BY parent_id, order_num, menu_id;
