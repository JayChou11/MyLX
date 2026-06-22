-- 学生成绩模块增量脚本
-- 功能：创建成绩表、菜单、按钮权限，并基于已有学生自动生成练习成绩。
--
-- 使用方式：
-- 1. 先确认学生表 sys_student 和班级表 sys_class 已存在，并且已有一些学生数据；
-- 2. 执行本脚本；
-- 3. 重新登录或刷新前端路由缓存，就能在“学生管理”下看到“学生成绩”菜单。
--
-- 安全性说明：
-- 1. CREATE TABLE IF NOT EXISTS 不会删除已有成绩表；
-- 2. 菜单和权限使用 WHERE NOT EXISTS，重复执行不会重复插入相同菜单；
-- 3. 假成绩数据也使用 WHERE NOT EXISTS，重复执行不会重复插入同一学生同一场考试。

CREATE TABLE IF NOT EXISTS `sys_student_score` (
  `score_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `exam_name` varchar(100) NOT NULL COMMENT '考试名称',
  `chinese_score` decimal(5,2) NOT NULL COMMENT '语文成绩',
  `math_score` decimal(5,2) NOT NULL COMMENT '数学成绩',
  `english_score` decimal(5,2) NOT NULL COMMENT '英语成绩',
  `total_score` decimal(6,2) NOT NULL COMMENT '总分',
  `average_score` decimal(5,2) NOT NULL COMMENT '平均分',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`score_id`),
  UNIQUE KEY `uk_sys_student_score_exam` (`student_id`, `exam_name`),
  KEY `idx_sys_student_score_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生成绩表';

-- 如果环境里还没有“学生管理”目录，先补一个目录。
-- parent_id = 1 表示挂到“系统管理”下面，这是当前学生模块已有菜单的挂载方式。
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '学生管理', 1, 10, 'student', '', '', '', 1, 0, 'M', '0', '0', '', 'user', 'admin', NOW(), '', NULL, '学生管理目录'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = 1 AND menu_name = '学生管理'
);

SET @student_parent_id = (
  SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND menu_name = '学生管理' LIMIT 1
);

-- 新增菜单：学生管理 -> 学生成绩
-- component = 'system/student/score/index' 对应前端文件：
-- ruoyi-ui/src/views/system/student/score/index.vue
-- perms = 'system:studentScore:list' 是菜单访问权限，前端路由和后端接口权限都依赖它。
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '学生成绩', @student_parent_id, 8, 'score', 'system/student/score/index', '', 'StudentScore', 1, 0, 'C', '0', '0', 'system:studentScore:list', 'education', 'admin', NOW(), '', NULL, '学生成绩菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '学生成绩'
);

SET @score_menu_id = (
  SELECT menu_id FROM sys_menu WHERE parent_id = @student_parent_id AND menu_name = '学生成绩' LIMIT 1
);

-- 按钮权限：查询
-- F 类型表示按钮权限，不会显示成左侧菜单，只用于控制按钮和接口权限。
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '成绩查询', @score_menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:studentScore:query', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @score_menu_id AND perms = 'system:studentScore:query'
);

-- 按钮权限：新增
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '成绩新增', @score_menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:studentScore:add', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @score_menu_id AND perms = 'system:studentScore:add'
);

-- 按钮权限：修改
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '成绩修改', @score_menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:studentScore:edit', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @score_menu_id AND perms = 'system:studentScore:edit'
);

-- 按钮权限：删除
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '成绩删除', @score_menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:studentScore:remove', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @score_menu_id AND perms = 'system:studentScore:remove'
);

-- 按钮权限：导出
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '成绩导出', @score_menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:studentScore:export', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @score_menu_id AND perms = 'system:studentScore:export'
);

-- 按钮权限：导入
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '成绩导入', @score_menu_id, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:studentScore:import', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @score_menu_id AND perms = 'system:studentScore:import'
);

-- 按钮权限：统计
INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '成绩统计', @score_menu_id, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:studentScore:stat', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE parent_id = @score_menu_id AND perms = 'system:studentScore:stat'
);

-- 给管理员角色补充菜单和按钮权限；普通角色可在角色管理里手动分配。
-- role_id = 1 通常是超级管理员角色。
-- 如果你用普通角色测试，需要进入“系统管理 -> 角色管理”给角色分配“学生成绩”相关权限。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (
  @student_parent_id,
  @score_menu_id
)
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.parent_id = @score_menu_id
AND NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
);

-- 练习数据：基于现有学生自动生成两场考试成绩。
-- 说明：如果没有学生数据，这里不会插入成绩；如果重复执行脚本，也不会重复插入同一学生同一场考试。
--
-- 这里没有手写固定 student_id，而是从 sys_student 表查询现有学生：
-- 1. 每个学生生成“2026春季期中考试”和“2026春季期末考试”两条成绩；
-- 2. mod(s.student_id * 7, 41) 这类表达式用 student_id 生成 0-40 之间的变化值；
-- 3. 再加 60，所以成绩范围是 60-100，适合练习列表、导出和班级统计；
-- 4. total_score 和 average_score 在 SQL 中同步算出来，和后端 Service 的计算规则保持一致。
INSERT INTO sys_student_score
(student_id, exam_name, chinese_score, math_score, english_score, total_score, average_score, create_by, create_time, remark)
SELECT t.student_id,
       t.exam_name,
       t.chinese_score,
       t.math_score,
       t.english_score,
       round(t.chinese_score + t.math_score + t.english_score, 2) as total_score,
       round((t.chinese_score + t.math_score + t.english_score) / 3, 2) as average_score,
       'admin',
       NOW(),
       '系统自动生成的学生成绩练习数据'
FROM (
  SELECT s.student_id,
         '2026春季期中考试' as exam_name,
         cast(60 + mod(s.student_id * 7, 41) as decimal(5,2)) as chinese_score,
         cast(60 + mod(s.student_id * 11, 41) as decimal(5,2)) as math_score,
         cast(60 + mod(s.student_id * 13, 41) as decimal(5,2)) as english_score
  FROM sys_student s
  UNION ALL
  SELECT s.student_id,
         '2026春季期末考试' as exam_name,
         cast(60 + mod(s.student_id * 17, 41) as decimal(5,2)) as chinese_score,
         cast(60 + mod(s.student_id * 19, 41) as decimal(5,2)) as math_score,
         cast(60 + mod(s.student_id * 23, 41) as decimal(5,2)) as english_score
  FROM sys_student s
) t
WHERE NOT EXISTS (
  SELECT 1
  FROM sys_student_score ss
  WHERE ss.student_id = t.student_id
    AND ss.exam_name = t.exam_name
);
