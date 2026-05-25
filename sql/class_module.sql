-- =============================================
-- 班级管理模块 - 数据库变更脚本
-- 执行前提：已执行 student_module_20260417.sql
-- =============================================

-- 1. 创建班级表
CREATE TABLE IF NOT EXISTS `sys_class` (
  `class_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(30) NOT NULL COMMENT '班级名称',
  `grade` varchar(20) NOT NULL COMMENT '年级',
  `teacher_name` varchar(30) DEFAULT NULL COMMENT '班主任',
  `classroom` varchar(50) DEFAULT NULL COMMENT '教室',
  `max_count` int(6) DEFAULT 50 COMMENT '人数上限',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `uk_sys_class_grade_name` (`grade`, `class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

-- 2. 学生表新增 class_id 外键字段
SET @class_id_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_student'
    AND column_name = 'class_id'
);
SET @class_id_sql = IF(@class_id_exists = 0,
  'ALTER TABLE sys_student ADD COLUMN class_id bigint(20) DEFAULT NULL COMMENT ''班级ID'' AFTER gender',
  'SELECT 1'
);
PREPARE class_id_stmt FROM @class_id_sql;
EXECUTE class_id_stmt;
DEALLOCATE PREPARE class_id_stmt;

-- 3. 将学生表中原有 grade + class_name 数据迁移到 sys_class，并回填 class_id
INSERT INTO sys_class (class_name, grade, create_by, create_time)
SELECT DISTINCT s.class_name, s.grade, 'admin', sysdate()
FROM sys_student s
WHERE s.grade IS NOT NULL AND s.class_name IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_class c WHERE c.grade = s.grade AND c.class_name = s.class_name
  );

UPDATE sys_student s
INNER JOIN sys_class c ON s.grade = c.grade AND s.class_name = c.class_name
SET s.class_id = c.class_id
WHERE s.class_id IS NULL;

-- 3.5 将学生表中旧的 grade、class_name 列改为可空（数据已通过 class_id 关联到 sys_class）
ALTER TABLE sys_student MODIFY COLUMN grade varchar(20) DEFAULT NULL COMMENT '年级（已由class_id关联，仅作历史冗余）';
ALTER TABLE sys_student MODIFY COLUMN class_name varchar(30) DEFAULT NULL COMMENT '班级（已由class_id关联，仅作历史冗余）';

-- 4. 新增菜单：学生管理 -> 班级管理
SET @student_parent_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND menu_name = '学生管理' LIMIT 1);

SET @class_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '班级管理' LIMIT 1);
SET @class_menu_id = IFNULL(@class_menu_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @class_menu_id, '班级管理', @student_parent_id, 3, 'class', 'system/class/index', '', 'SysClass',
  1, 0, 'C', '0', '0', 'system:class:list', 'tree', 'admin', sysdate(), '', NULL, '班级管理菜单'
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '班级管理'
);

SET @class_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '班级管理' LIMIT 1);

-- 5. 班级管理按钮权限
SET @class_query_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:query' LIMIT 1);
SET @class_query_id = IFNULL(@class_query_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @class_query_id, '班级查询', @class_menu_id, 1, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:class:query', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:query'
);

SET @class_add_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:add' LIMIT 1);
SET @class_add_id = IFNULL(@class_add_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @class_add_id, '班级新增', @class_menu_id, 2, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:class:add', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:add'
);

SET @class_edit_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:edit' LIMIT 1);
SET @class_edit_id = IFNULL(@class_edit_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @class_edit_id, '班级修改', @class_menu_id, 3, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:class:edit', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:edit'
);

SET @class_remove_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:remove' LIMIT 1);
SET @class_remove_id = IFNULL(@class_remove_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @class_remove_id, '班级删除', @class_menu_id, 4, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:class:remove', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:remove'
);

SET @class_export_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:export' LIMIT 1);
SET @class_export_id = IFNULL(@class_export_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @class_export_id, '班级导出', @class_menu_id, 5, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:class:export', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @class_menu_id AND perms = 'system:class:export'
);

-- 6. 给管理员角色补充班级管理权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (
  @class_menu_id,
  @class_query_id,
  @class_add_id,
  @class_edit_id,
  @class_remove_id,
  @class_export_id
)
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
);
