CREATE TABLE `sys_exam` (
                                     `exam_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '考试批次ID',
                                     `exam_name` varchar(100) NOT NULL COMMENT '考试名称',
                                     `semester` varchar(20) NOT NULL COMMENT '学期',
                                     `grade` varchar(20) NOT NULL COMMENT '年级',
                                     `exam_date` datetime NOT NULL COMMENT '考试日期',
                                     `exam_status` varchar(20) NOT NULL COMMENT '状态',
                                     `remark` varchar(200) DEFAULT NULL COMMENT '备注',
                                     PRIMARY KEY (`exam_id`),
                                     UNIQUE KEY `uk_sys_exam_name_grade_semester` (`exam_name`, `grade`, `semester`)
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8mb4 COMMENT='考试批次表';