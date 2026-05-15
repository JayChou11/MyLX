-- 学生信息模块初始化脚本
-- 1. 创建业务表
CREATE TABLE IF NOT EXISTS `sys_student` (
  `student_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `student_no` varchar(32) NOT NULL COMMENT '学号',
  `student_name` varchar(30) NOT NULL COMMENT '姓名',
  `id_card` varchar(18) NOT NULL COMMENT '身份证号',
  `age` int(3) NOT NULL COMMENT '年龄',
  `gender` char(1) NOT NULL DEFAULT '2' COMMENT '性别（0男 1女 2未知）',
  `grade` varchar(20) NOT NULL COMMENT '年级',
  `class_name` varchar(30) NOT NULL COMMENT '班级',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `uk_sys_student_no` (`student_no`),
  UNIQUE KEY `uk_sys_student_id_card` (`id_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

CREATE TABLE `sys_teacher` (
     `teacher_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '教师ID',
     `teacher_name` varchar(50) NOT NULL COMMENT '教师姓名',
     `gender` char(1) NOT NULL DEFAULT '2' COMMENT '性别（0男 1女 2未知）',
     `phone` varchar(20) COMMENT '手机号',
     `subject` varchar(100) COMMENT '教授科目',
     `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
     `update_time` datetime DEFAULT NULL COMMENT '更新时间',
     `remark` varchar(500) DEFAULT NULL COMMENT '备注',
     PRIMARY KEY (`teacher_id`),
     UNIQUE KEY `uk_sys_teacher_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

-- 2. 新增目录：系统管理 -> 学生管理
SET @student_parent_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND menu_name = '学生管理' LIMIT 1);
SET @student_parent_id = IFNULL(@student_parent_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @student_parent_id, '学生管理', 1, 10, 'student', '', '', '',
  1, 0, 'M', '0', '0', '', 'user', 'admin', sysdate(), '', NULL, '学生管理目录'
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = 1 AND menu_name = '学生管理'
);

SET @student_parent_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND menu_name = '学生管理' LIMIT 1);

-- 3. 新增菜单：学生管理 -> 学生信息
SET @student_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '学生信息' LIMIT 1);
SET @student_menu_id = IFNULL(@student_menu_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @student_menu_id, '学生信息', @student_parent_id, 1, 'info', 'system/student/index', '', 'StudentInfo',
  1, 0, 'C', '0', '0', 'system:student:list', 'user', 'admin', sysdate(), '', NULL, '学生信息菜单'
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '学生信息'
);

SET @student_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '学生信息' LIMIT 1);

-- 4. 新增按钮权限
SET @student_query_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:query' LIMIT 1);
SET @student_query_id = IFNULL(@student_query_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @student_query_id, '学生查询', @student_menu_id, 1, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:student:query', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:query'
);

SET @student_add_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:add' LIMIT 1);
SET @student_add_id = IFNULL(@student_add_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @student_add_id, '学生新增', @student_menu_id, 2, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:student:add', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:add'
);

SET @student_edit_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:edit' LIMIT 1);
SET @student_edit_id = IFNULL(@student_edit_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @student_edit_id, '学生修改', @student_menu_id, 3, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:student:edit', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:edit'
);

SET @student_remove_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:remove' LIMIT 1);
SET @student_remove_id = IFNULL(@student_remove_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @student_remove_id, '学生删除', @student_menu_id, 4, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:student:remove', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:remove'
);

SET @student_export_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:export' LIMIT 1);
SET @student_export_id = IFNULL(@student_export_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @student_export_id, '学生导出', @student_menu_id, 5, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:student:export', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:export'
);

-- 5. 给管理员角色补充菜单权限（普通角色请在角色管理中手工分配）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (
  @student_parent_id,
  @student_menu_id,
  @student_query_id,
  @student_add_id,
  @student_edit_id,
  @student_remove_id,
  @student_export_id
)
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
);

SET @student_import_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:import' LIMIT 1);
SET @student_import_id = IFNULL(@student_import_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @student_import_id, '学生导入', @student_menu_id, 6, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:student:import', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_menu_id AND perms = 'system:student:import'
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, @student_import_id
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = @student_import_id
);
