-- =============================================
-- 转班审批模块初始化脚本
-- 执行前提：已执行班级、学生相关模块脚本
-- =============================================

-- 1. 转班申请表
CREATE TABLE IF NOT EXISTS `sys_transfer_apply` (
  `apply_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `student_name` varchar(30) DEFAULT NULL COMMENT '学生姓名',
  `before_class_id` bigint(20) NOT NULL COMMENT '原班级ID',
  `before_class_name` varchar(30) DEFAULT NULL COMMENT '原班级名称',
  `before_grade` varchar(20) DEFAULT NULL COMMENT '原年级',
  `after_class_id` bigint(20) NOT NULL COMMENT '目标班级ID',
  `after_class_name` varchar(30) DEFAULT NULL COMMENT '目标班级名称',
  `after_grade` varchar(20) DEFAULT NULL COMMENT '目标年级',
  `apply_reason` varchar(500) NOT NULL COMMENT '申请原因',
  `apply_by` varchar(64) NOT NULL COMMENT '申请人',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0待班主任审批 1待教务处审批 2已通过 3已拒绝）',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`apply_id`),
  KEY `idx_transfer_apply_student` (`student_id`),
  KEY `idx_transfer_apply_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转班申请表';

-- 2. 审批记录表
CREATE TABLE IF NOT EXISTS `sys_transfer_approve` (
  `approve_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '审批ID',
  `apply_id` bigint(20) NOT NULL COMMENT '申请ID',
  `approve_level` int(1) NOT NULL COMMENT '审批层级（1班主任 2教务处）',
  `approve_by` varchar(64) NOT NULL COMMENT '审批人',
  `approve_time` datetime NOT NULL COMMENT '审批时间',
  `approve_result` char(1) NOT NULL COMMENT '审批结果（0通过 1拒绝）',
  `approve_remark` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`approve_id`),
  KEY `idx_transfer_approve_apply` (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转班审批记录表';

-- 3. 审批状态字典
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'transfer_apply_status';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'transfer_apply_status';

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('转班审批状态', 'transfer_apply_status', '0', 'admin', NOW(), '转班申请审批状态字典');

INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
VALUES
(1, '待班主任审批', '0', 'transfer_apply_status', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(2, '待教务处审批', '1', 'transfer_apply_status', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(3, '已通过', '2', 'transfer_apply_status', '', 'success', 'N', '0', 'admin', NOW(), ''),
(4, '已拒绝', '3', 'transfer_apply_status', '', 'danger', 'N', '0', 'admin', NOW(), '');

-- 4. 创建菜单
SET @student_parent_id = (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = 1 AND menu_name = '学生管理'
  LIMIT 1
);

INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '转班审批', @student_parent_id, 5, 'transferApprove', 'system/student/transferApprove/index', '', '', 1, 0, 'C', '0', '0', 'system:student:transferApprove:list', 'education', 'admin', NOW(), 'admin', NOW(), ''
WHERE @student_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE menu_name = '转班审批'
      AND parent_id = @student_parent_id
  );

SET @parent_menu_id = (
  SELECT menu_id
  FROM sys_menu
  WHERE menu_name = '转班审批' AND parent_id = @student_parent_id
  LIMIT 1
);

INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '转班审批查询', @parent_menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:student:transferApprove:query', '#', 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @parent_menu_id AND perms = 'system:student:transferApprove:query'
);

INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '转班审批新增', @parent_menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:student:transferApprove:add', '#', 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @parent_menu_id AND perms = 'system:student:transferApprove:add'
);

INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '转班审批审批', @parent_menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:student:transferApprove:approve', '#', 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @parent_menu_id AND perms = 'system:student:transferApprove:approve'
);

INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '转班审批删除', @parent_menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:student:transferApprove:remove', '#', 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @parent_menu_id AND perms = 'system:student:transferApprove:remove'
);

INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '转班审批导出', @parent_menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:student:transferApprove:export', '#', 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @parent_menu_id AND perms = 'system:student:transferApprove:export'
);

-- 5. 创建角色
INSERT INTO sys_role
(`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '班主任', 'class_teacher', 3, '1', 1, 1, '0', '0', 'admin', NOW(), '', NULL, '转班审批一级审批角色'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role WHERE role_key = 'class_teacher'
);

INSERT INTO sys_role
(`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '教务处', 'academic_office', 4, '1', 1, 1, '0', '0', 'admin', NOW(), '', NULL, '转班审批二级审批角色'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role WHERE role_key = 'academic_office'
);

-- 6. 给 admin 角色授予全部菜单权限
SET @admin_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'admin' LIMIT 1);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
VALUES (@admin_role_id, @parent_menu_id);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE parent_id = @parent_menu_id;

-- 7. 给班主任角色授予一级审批相关权限
SET @class_teacher_role_id = (
  SELECT role_id FROM sys_role WHERE role_key = 'class_teacher' LIMIT 1
);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
VALUES (@class_teacher_role_id, @parent_menu_id);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @class_teacher_role_id, menu_id
FROM sys_menu
WHERE parent_id = @parent_menu_id
  AND perms IN (
    'system:student:transferApprove:query',
    'system:student:transferApprove:add',
    'system:student:transferApprove:approve',
    'system:student:transferApprove:remove'
  );

-- 列表页权限来自目录 perms，这里补上目录本身即可

-- 8. 给教务处角色授予二级审批相关权限
SET @academic_office_role_id = (
  SELECT role_id FROM sys_role WHERE role_key = 'academic_office' LIMIT 1
);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
VALUES (@academic_office_role_id, @parent_menu_id);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @academic_office_role_id, menu_id
FROM sys_menu
WHERE parent_id = @parent_menu_id
  AND perms IN (
    'system:student:transferApprove:query',
    'system:student:transferApprove:approve',
    'system:student:transferApprove:remove',
    'system:student:transferApprove:export'
  );

SELECT '转班审批模块初始化完成' AS result;
