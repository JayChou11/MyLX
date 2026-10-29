-- =============================================
-- 学生、班级、考试、成绩演示数据（MySQL 5.7 / 8.0）
-- 使用：先选择项目数据库，在同一连接中完整执行；客户端请设置遇错停止。
-- 前提：已执行学生、班级、成绩、考试建表脚本，成绩表已有 exam_id 字段。
-- 如果下面预检查提示 Unknown column 'ss.exam_id'，请先补充成绩表结构，
-- 不要跳过错误继续执行。本文件不自动修改业务表结构。
--
-- 空库预期：18 个演示班、540 名学生、18 个考试批次、1620 条成绩。
-- 默认每班 30 人，可把 @demo_class_size 改为 1~50 的整数。
-- 重复执行仅补充缺失数据，不清空表、不覆盖已有记录。
-- 班级名使用“演示1班”等，学号以 DEMO2026 开头，create_by 标记造数来源。
-- 身份证号使用 990000 虚构地区码，仅用于测试，不代表真实身份信息。
-- 直接插入成绩不会调用 Java Service，因此不会自动生成持久化预警记录；
-- 成绩页的实时预警分析可使用这些成绩。测试记录生成需通过成绩新增/修改接口。
-- =============================================

SET NAMES utf8mb4;

-- 0. 预检查必需字段；查询返回空结果即通过。
SELECT s.student_id, s.class_id, c.class_id, ss.exam_id, e.exam_id
FROM sys_student s
CROSS JOIN sys_class c
CROSS JOIN sys_student_score ss
CROSS JOIN sys_exam e
LIMIT 0;

SET @demo_class_size = 30;
SET @demo_creator = 'demo_seed_20261015';
SET @demo_semester = '2026-2027学年第一学期';

-- 临时表只属于当前连接；清理上次未执行完留下的辅助表。
DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_grade;
DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_number;
DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_student;
DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_exam;
DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_score;

CREATE TEMPORARY TABLE tmp_education_demo_grade (
  grade_no INT PRIMARY KEY,
  grade VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TEMPORARY TABLE tmp_education_demo_number (
  n INT PRIMARY KEY
) ENGINE=InnoDB;

CREATE TEMPORARY TABLE tmp_education_demo_student (
  student_no VARCHAR(32) PRIMARY KEY,
  student_name VARCHAR(30) NOT NULL,
  id_card VARCHAR(18) NOT NULL,
  age INT NOT NULL,
  gender CHAR(1) NOT NULL,
  grade VARCHAR(20) NOT NULL,
  class_name VARCHAR(30) NOT NULL,
  grade_no INT NOT NULL,
  class_no INT NOT NULL,
  seat_no INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TEMPORARY TABLE tmp_education_demo_exam (
  exam_no INT PRIMARY KEY,
  exam_title VARCHAR(40) NOT NULL,
  exam_date DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TEMPORARY TABLE tmp_education_demo_score (
  student_id BIGINT NOT NULL,
  exam_id BIGINT NOT NULL,
  exam_name VARCHAR(100) NOT NULL,
  chinese_score DECIMAL(5,2) NOT NULL,
  math_score DECIMAL(5,2) NOT NULL,
  english_score DECIMAL(5,2) NOT NULL,
  PRIMARY KEY (student_id, exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 所有业务数据插入位于同一事务中，执行出错后请在当前连接执行 ROLLBACK。
START TRANSACTION;

INSERT INTO tmp_education_demo_grade (grade_no, grade) VALUES
(1, '初一'), (2, '初二'), (3, '初三'), (4, '高一'), (5, '高二'), (6, '高三');

INSERT INTO tmp_education_demo_number (n)
SELECT tens.d * 10 + ones.d + 1
FROM (
  SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
) tens
CROSS JOIN (
  SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) ones;

INSERT INTO tmp_education_demo_exam (exam_no, exam_title, exam_date) VALUES
(1, '第一次月考', '2026-09-15 09:00:00'),
(2, '第二次月考', '2026-10-01 09:00:00'),
(3, '阶段测试',   '2026-10-14 09:00:00');

-- 1. 创建演示班级；不复用同名但来源不是本脚本的班级。
INSERT INTO sys_class
(class_name, grade, teacher_name, classroom, max_count, create_by, create_time, remark)
SELECT CONCAT('演示', k.class_no, '班'), g.grade,
       CONCAT(ELT(g.grade_no, '张', '李', '王', '赵', '陈', '刘'),
              ELT(k.class_no, '明', '芳', '华'), '老师'),
       CONCAT('教学楼', g.grade_no, '0', k.class_no), 50,
       @demo_creator, NOW(), '自动生成的演示班级'
FROM tmp_education_demo_grade g
CROSS JOIN (SELECT 1 AS class_no UNION ALL SELECT 2 UNION ALL SELECT 3) k
WHERE @demo_class_size BETWEEN 1 AND 50
  AND @demo_class_size = FLOOR(@demo_class_size)
  AND NOT EXISTS (
    SELECT 1 FROM sys_class c
    WHERE c.grade = g.grade AND c.class_name = CONCAT('演示', k.class_no, '班')
  );

-- 2. 按自然键生成学生；不依赖自增 ID 的起始值。
INSERT INTO tmp_education_demo_student
(student_no, student_name, id_card, age, gender, grade, class_name, grade_no, class_no, seat_no)
SELECT CONCAT('DEMO2026', g.grade_no, k.class_no, LPAD(n.n, 2, '0')),
       CONCAT(
         ELT(MOD(n.n + k.class_no + g.grade_no - 3, 10) + 1,
             '张', '王', '李', '赵', '陈', '刘', '周', '吴', '徐', '孙'),
         ELT(MOD(n.n * 3 + k.class_no + g.grade_no - 5, 20) + 1,
             '子涵', '浩然', '雨桐', '思远', '欣怡', '宇轩', '诗涵', '梓晨', '若曦', '一诺',
             '明哲', '可欣', '嘉宁', '文博', '安琪', '奕辰', '沐阳', '语嫣', '俊熙', '书瑶')
       ),
       CONCAT('990000', 2015 - g.grade_no, '0101',
              LPAD((g.grade_no - 1) * 150 + (k.class_no - 1) * 50 + n.n, 3, '0'),
              MOD(n.n, 2)),
       11 + g.grade_no,
       CASE WHEN MOD(n.n, 2) = 1 THEN '0' ELSE '1' END,
       g.grade, CONCAT('演示', k.class_no, '班'), g.grade_no, k.class_no, n.n
FROM tmp_education_demo_grade g
CROSS JOIN (SELECT 1 AS class_no UNION ALL SELECT 2 UNION ALL SELECT 3) k
CROSS JOIN tmp_education_demo_number n
WHERE @demo_class_size BETWEEN 1 AND 50
  AND @demo_class_size = FLOOR(@demo_class_size)
  AND n.n <= @demo_class_size;

INSERT INTO sys_student
(student_no, student_name, id_card, age, gender, class_id, grade, class_name,
 photo, create_by, create_time, remark)
SELECT t.student_no, t.student_name, t.id_card, t.age, t.gender, c.class_id,
       t.grade, t.class_name, NULL, @demo_creator, NOW(), '自动生成的演示学生，身份证号为虚构测试值'
FROM tmp_education_demo_student t
INNER JOIN sys_class c ON c.grade = t.grade AND c.class_name = t.class_name
WHERE c.create_by = @demo_creator
  AND NOT EXISTS (
    SELECT 1 FROM sys_student s WHERE s.student_no = t.student_no OR s.id_card = t.id_card
  );

-- 3. 每个年级创建 3 场考试。名称含年级，也兼容旧版 student_id + exam_name 唯一索引。
INSERT INTO sys_exam (exam_name, semester, grade, exam_date, exam_status, remark)
SELECT CONCAT('演示-', g.grade, '-', x.exam_title), @demo_semester, g.grade,
       x.exam_date, '0', @demo_creator
FROM tmp_education_demo_grade g
CROSS JOIN tmp_education_demo_exam x
WHERE @demo_class_size BETWEEN 1 AND 50
  AND @demo_class_size = FLOOR(@demo_class_size)
  AND NOT EXISTS (
    SELECT 1 FROM sys_exam e
    WHERE e.exam_name = CONCAT('演示-', g.grade, '-', x.exam_title)
      AND e.grade = g.grade AND e.semester = @demo_semester
  );

-- 4. 生成分数：每 10 个座号一组，包含优秀、低分、明显退步、明显进步等样例。
-- 使用座号和考试序号生成稳定分数，避免重复执行产生不同结果。
INSERT INTO tmp_education_demo_score
(student_id, exam_id, exam_name, chinese_score, math_score, english_score)
SELECT s.student_id, e.exam_id, e.exam_name,
       CASE MOD(t.seat_no, 10)
         WHEN 0 THEN 95 + MOD(t.seat_no + x.exam_no, 6)
         WHEN 1 THEN 30 + MOD(t.seat_no + x.exam_no * 2, 10)
         WHEN 2 THEN 90 - (x.exam_no - 1) * 25
         WHEN 3 THEN 40 + (x.exam_no - 1) * 25
         ELSE 62 + MOD(t.seat_no * 17 + t.grade_no * 11 + x.exam_no * 23, 300) / 10.0
       END,
       CASE MOD(t.seat_no, 10)
         WHEN 0 THEN 95 + MOD(t.seat_no + x.exam_no + 1, 6)
         WHEN 1 THEN 32 + MOD(t.seat_no + x.exam_no * 3, 8)
         WHEN 2 THEN 92 - (x.exam_no - 1) * 25
         WHEN 3 THEN 42 + (x.exam_no - 1) * 25
         WHEN 4 THEN 45 + MOD(t.seat_no + x.exam_no, 10)
         ELSE 61 + MOD(t.seat_no * 19 + t.class_no * 13 + x.exam_no * 29, 310) / 10.0
       END,
       CASE MOD(t.seat_no, 10)
         WHEN 0 THEN 95 + MOD(t.seat_no + x.exam_no + 2, 6)
         WHEN 1 THEN 31 + MOD(t.seat_no + x.exam_no * 5, 9)
         WHEN 2 THEN 88 - (x.exam_no - 1) * 25
         WHEN 3 THEN 38 + (x.exam_no - 1) * 25
         ELSE 60 + MOD(t.seat_no * 23 + t.grade_no * 7 + x.exam_no * 31, 320) / 10.0
       END
FROM tmp_education_demo_student t
INNER JOIN sys_student s ON s.student_no = t.student_no AND s.create_by = @demo_creator
INNER JOIN sys_class c ON c.class_id = s.class_id
  AND c.grade = t.grade AND c.class_name = t.class_name AND c.create_by = @demo_creator
CROSS JOIN tmp_education_demo_exam x
INNER JOIN sys_exam e ON e.grade = t.grade AND e.semester = @demo_semester
  AND e.exam_name = CONCAT('演示-', t.grade, '-', x.exam_title)
  AND e.remark = @demo_creator;

INSERT INTO sys_student_score
(student_id, exam_id, exam_name, chinese_score, math_score, english_score,
 total_score, average_score, create_by, create_time, remark)
SELECT t.student_id, t.exam_id, t.exam_name, t.chinese_score, t.math_score, t.english_score,
       ROUND(t.chinese_score + t.math_score + t.english_score, 2),
       ROUND((t.chinese_score + t.math_score + t.english_score) / 3, 2),
       @demo_creator, NOW(), '自动生成的演示成绩，含优秀/低分/进步/退步样例'
FROM tmp_education_demo_score t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_student_score ss
  WHERE ss.student_id = t.student_id
    AND (ss.exam_id = t.exam_id OR ss.exam_name = t.exam_name)
);

COMMIT;

-- 5. 核对来源标记下的数据总量；若同名数据被占用或演示学生已调班，可能少于预期。
SELECT DATABASE() AS database_name,
       @demo_class_size AS students_per_class,
       (SELECT COUNT(*) FROM sys_class WHERE create_by = @demo_creator) AS demo_classes,
       (SELECT COUNT(*) FROM sys_student WHERE create_by = @demo_creator) AS demo_students,
       (SELECT COUNT(*) FROM sys_exam WHERE remark = @demo_creator) AS demo_exams,
       (SELECT COUNT(*) FROM sys_student_score WHERE create_by = @demo_creator) AS demo_scores;

SELECT c.grade, c.class_name, c.max_count, COUNT(s.student_id) AS student_count
FROM sys_class c
LEFT JOIN sys_student s ON s.class_id = c.class_id
WHERE c.create_by = @demo_creator
GROUP BY c.class_id, c.grade, c.class_name, c.max_count
ORDER BY c.class_id;

DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_score;
DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_exam;
DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_student;
DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_number;
DROP TEMPORARY TABLE IF EXISTS tmp_education_demo_grade;
