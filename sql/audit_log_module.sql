-- =============================================
-- 审计日志增强模块 - 数据库变更脚本
-- 执行前提：已执行 student_module_20260417.sql
-- =============================================

-- 1. 创建审计日志表
CREATE TABLE IF NOT EXISTS `sys_audit_log` (
  `audit_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型（TRANSFER=调班,UPGRADE=升年级,DELETE=删除等）',
  `detail` varchar(200) DEFAULT NULL COMMENT '业务描述（如：批量调班、执行升年级）',
  `method` varchar(200) DEFAULT NULL COMMENT '操作方法（类名.方法名）',
  `oper_name` varchar(64) DEFAULT '' COMMENT '操作人',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '操作IP',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `before_data` text COMMENT '变更前数据（JSON序列化的方法参数）',
  `after_data` text COMMENT '变更后数据（JSON序列化的方法返回值）',
  `change_summary` varchar(500) DEFAULT NULL COMMENT '变更摘要（如：2名学生从初一1班调到初二2班）',
  `status` int(1) DEFAULT 0 COMMENT '操作状态（0成功 1失败）',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '错误消息',
  PRIMARY KEY (`audit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务审计日志表';

-- 2. 新增菜单：学生管理 -> 审计日志
SET @student_parent_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND menu_name = '学生管理' LIMIT 1);

SET @audit_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '审计日志' LIMIT 1);
SET @audit_menu_id = IFNULL(@audit_menu_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @audit_menu_id, '审计日志', @student_parent_id, 4, 'auditLog', 'system/student/auditLog/index', '', 'StudentAuditLog',
  1, 0, 'C', '0', '0', 'system:student:audit:list', 'log', 'admin', sysdate(), '', NULL, '业务审计日志菜单'
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '审计日志'
);

SET @audit_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '审计日志' LIMIT 1);

-- 3. 审计日志按钮权限
SET @audit_query_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @audit_menu_id AND perms = 'system:student:audit:query' LIMIT 1);
SET @audit_query_id = IFNULL(@audit_query_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @audit_query_id, '审计查询', @audit_menu_id, 1, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:student:audit:query', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @audit_menu_id AND perms = 'system:student:audit:query'
);

SET @audit_export_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @audit_menu_id AND perms = 'system:student:audit:export' LIMIT 1);
SET @audit_export_id = IFNULL(@audit_export_id, (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu));
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
)
SELECT
  @audit_export_id, '审计导出', @audit_menu_id, 2, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:student:audit:export', '#', 'admin', sysdate(), '', NULL, ''
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @audit_menu_id AND perms = 'system:student:audit:export'
);

-- 4. 给管理员角色授权审计日志权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (
  @audit_menu_id,
  @audit_query_id,
  @audit_export_id
)
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
);