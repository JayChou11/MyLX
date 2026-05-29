-- =============================================
-- 学生年级班级数据修复脚本（兼容 MySQL 5.7）
-- 执行前提：已执行 grade_upgrade.sql（字典 student_grade_order 已存在）
-- =============================================

-- ========== 第一步：诊断 - 查看当前数据状态 ==========

-- 1.1 查看 sys_class 当前所有班级数据
SELECT class_id, grade, class_name, teacher_name, classroom, max_count
FROM sys_class
ORDER BY grade, class_name;

-- 1.2 查看 sys_student 中 class_id 分布
SELECT
    CASE
        WHEN class_id IS NULL THEN '未关联班级'
        WHEN class_id NOT IN (SELECT class_id FROM sys_class) THEN '关联到不存在班级'
        ELSE '正常关联'
    END AS status,
    COUNT(*) AS count
FROM sys_student
GROUP BY status;

-- 1.3 查看 sys_student 旧字段与 sys_class 的对照
SELECT
    s.student_id, s.student_no, s.student_name,
    s.grade AS old_grade, s.class_name AS old_class_name,
    s.class_id,
    c.grade AS new_grade, c.class_name AS new_class_name
FROM sys_student s
LEFT JOIN sys_class c ON s.class_id = c.class_id
ORDER BY s.student_id;

-- ========== 第二步：修复数据 ==========

-- 2.1 清空现有班级数据
DELETE FROM sys_class;

-- 2.2 重新插入规范化的班级数据（6年级×3班=18个班级）
INSERT INTO sys_class (class_name, grade, teacher_name, classroom, max_count, create_by, create_time) VALUES
('1班', '初一', NULL, NULL, 50, 'admin', sysdate()),
('2班', '初一', NULL, NULL, 50, 'admin', sysdate()),
('3班', '初一', NULL, NULL, 50, 'admin', sysdate()),
('1班', '初二', NULL, NULL, 50, 'admin', sysdate()),
('2班', '初二', NULL, NULL, 50, 'admin', sysdate()),
('3班', '初二', NULL, NULL, 50, 'admin', sysdate()),
('1班', '初三', NULL, NULL, 50, 'admin', sysdate()),
('2班', '初三', NULL, NULL, 50, 'admin', sysdate()),
('3班', '初三', NULL, NULL, 50, 'admin', sysdate()),
('1班', '高一', NULL, NULL, 50, 'admin', sysdate()),
('2班', '高一', NULL, NULL, 50, 'admin', sysdate()),
('3班', '高一', NULL, NULL, 50, 'admin', sysdate()),
('1班', '高二', NULL, NULL, 50, 'admin', sysdate()),
('2班', '高二', NULL, NULL, 50, 'admin', sysdate()),
('3班', '高二', NULL, NULL, 50, 'admin', sysdate()),
('1班', '高三', NULL, NULL, 50, 'admin', sysdate()),
('2班', '高三', NULL, NULL, 50, 'admin', sysdate()),
('3班', '高三', NULL, NULL, 50, 'admin', sysdate());

-- 2.3 清空 sys_student 的旧年级班级字段和 class_id
UPDATE sys_student SET grade = NULL, class_name = NULL, class_id = NULL;

-- ========== 第三步：验证班级重建 ==========

SELECT class_id, grade, class_name
FROM sys_class
ORDER BY grade, class_name;

-- ========== 第四步：批量分配学生到班级 ==========

-- MySQL 5.7 不支持 ROW_NUMBER() 窗口函数，
-- 这里用"临时表 + AUTO_INCREMENT"模拟行号：
-- 1. 创建临时表，auto_increment 字段自动为每条记录生成连续序号
-- 2. INSERT ... SELECT ... ORDER BY 保证序号按 student_id 顺序排列
-- 3. 用序号 % 18 轮询分配到18个班级

-- 4.1 创建临时表（auto_increment 模拟行号）
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_student_rank (
    rn INT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL
);

-- 4.2 清空临时表，按 student_id 排序写入所有未分配的学生
-- AUTO_INCREMENT 会自动为每行分配 1, 2, 3... 的序号
DELETE FROM tmp_student_rank;

INSERT INTO tmp_student_rank (student_id)
SELECT student_id FROM sys_student WHERE class_id IS NULL ORDER BY student_id;

-- 4.3 验证临时表数据（可选，看看序号分配是否正确）
-- SELECT rn, student_id FROM tmp_student_rank ORDER BY rn LIMIT 20;

-- 4.4 把18个班级的 class_id 存到变量里（避免 UPDATE 中重复子查询）
SET @c11 = (SELECT class_id FROM sys_class WHERE grade='初一' AND class_name='1班');
SET @c12 = (SELECT class_id FROM sys_class WHERE grade='初一' AND class_name='2班');
SET @c13 = (SELECT class_id FROM sys_class WHERE grade='初一' AND class_name='3班');
SET @c21 = (SELECT class_id FROM sys_class WHERE grade='初二' AND class_name='1班');
SET @c22 = (SELECT class_id FROM sys_class WHERE grade='初二' AND class_name='2班');
SET @c23 = (SELECT class_id FROM sys_class WHERE grade='初二' AND class_name='3班');
SET @c31 = (SELECT class_id FROM sys_class WHERE grade='初三' AND class_name='1班');
SET @c32 = (SELECT class_id FROM sys_class WHERE grade='初三' AND class_name='2班');
SET @c33 = (SELECT class_id FROM sys_class WHERE grade='初三' AND class_name='3班');
SET @g11 = (SELECT class_id FROM sys_class WHERE grade='高一' AND class_name='1班');
SET @g12 = (SELECT class_id FROM sys_class WHERE grade='高一' AND class_name='2班');
SET @g13 = (SELECT class_id FROM sys_class WHERE grade='高一' AND class_name='3班');
SET @g21 = (SELECT class_id FROM sys_class WHERE grade='高二' AND class_name='1班');
SET @g22 = (SELECT class_id FROM sys_class WHERE grade='高二' AND class_name='2班');
SET @g23 = (SELECT class_id FROM sys_class WHERE grade='高二' AND class_name='3班');
SET @g31 = (SELECT class_id FROM sys_class WHERE grade='高三' AND class_name='1班');
SET @g32 = (SELECT class_id FROM sys_class WHERE grade='高三' AND class_name='2班');
SET @g33 = (SELECT class_id FROM sys_class WHERE grade='高三' AND class_name='3班');

-- 4.5 核心更新：用临时表的 rn 序号 + 变量分配班级
-- 算法：(rn - 1) % 18 把学生轮询分配到18个班级
--   rn % 18 = 0  → 初一1班
--   rn % 18 = 1  → 初一2班
--   rn % 18 = 2  → 初一3班
--   rn % 18 = 3  → 初二1班
--   ...
--   rn % 18 = 17 → 高三3班

UPDATE sys_student s
INNER JOIN tmp_student_rank t ON s.student_id = t.student_id
SET s.class_id = CASE
    WHEN (t.rn - 1) % 18 = 0  THEN @c11
    WHEN (t.rn - 1) % 18 = 1  THEN @c12
    WHEN (t.rn - 1) % 18 = 2  THEN @c13
    WHEN (t.rn - 1) % 18 = 3  THEN @c21
    WHEN (t.rn - 1) % 18 = 4  THEN @c22
    WHEN (t.rn - 1) % 18 = 5  THEN @c23
    WHEN (t.rn - 1) % 18 = 6  THEN @c31
    WHEN (t.rn - 1) % 18 = 7  THEN @c32
    WHEN (t.rn - 1) % 18 = 8  THEN @c33
    WHEN (t.rn - 1) % 18 = 9  THEN @g11
    WHEN (t.rn - 1) % 18 = 10 THEN @g12
    WHEN (t.rn - 1) % 18 = 11 THEN @g13
    WHEN (t.rn - 1) % 18 = 12 THEN @g21
    WHEN (t.rn - 1) % 18 = 13 THEN @g22
    WHEN (t.rn - 1) % 18 = 14 THEN @g23
    WHEN (t.rn - 1) % 18 = 15 THEN @g31
    WHEN (t.rn - 1) % 18 = 16 THEN @g32
    WHEN (t.rn - 1) % 18 = 17 THEN @g33
END;

-- 4.6 清理临时表
DROP TEMPORARY TABLE IF EXISTS tmp_student_rank;

-- ========== 第五步：验证分配结果 ==========

-- 5.1 查看每个班级的学生人数
SELECT c.grade, c.class_name, COUNT(s.student_id) AS student_count
FROM sys_class c
LEFT JOIN sys_student s ON s.class_id = c.class_id
GROUP BY c.class_id, c.grade, c.class_name
ORDER BY c.grade, c.class_name;

-- 5.2 查看是否还有未分配的学生
SELECT COUNT(*) AS unassigned_count
FROM sys_student WHERE class_id IS NULL;

-- 5.3 查看分配后的学生年级班级对照
SELECT s.student_id, s.student_no, s.student_name, c.grade, c.class_name
FROM sys_student s
LEFT JOIN sys_class c ON s.class_id = c.class_id
ORDER BY c.grade, c.class_name, s.student_id;

-- ========== 注意事项 ==========
-- 1. 第四步需要一次性完整执行（4.1 ~ 4.6），不能只执行其中某条
-- 2. 临时表 tmp_student_rank 只在当前会话存在，断开连接后自动消失
-- 3. 如果你的学生数不是18的倍数，最后几个班可能少1人，不影响功能
-- 4. 分配后学生的 grade 和 class_name 会自动通过 LEFT JOIN 正确显示