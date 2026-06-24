package com.ruoyi.system.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生成绩趋势视图对象
 *
 * 这个 VO 专门服务于“成绩分析”弹窗：
 * 1. 一行代表某个学生某一次考试的成绩；
 * 2. 按时间顺序排列后，前端可以画出总分/平均分变化趋势；
 * 3. 同时带上班级排名和年级排名，方便观察成绩变化和排名变化是否一致。
 *
 * @author ruoyi
 */
public class SysStudentScoreTrendVo
{
    /** 成绩ID */
    private Long scoreId;

    /** 学生ID */
    private Long studentId;

    /** 学号 */
    private String studentNo;

    /** 姓名 */
    private String studentName;

    /** 年级 */
    private String grade;

    /** 班级 */
    private String className;

    /** 考试名称 */
    private String examName;

    /** 总分 */
    private BigDecimal totalScore;

    /** 平均分 */
    private BigDecimal averageScore;

    /** 班级排名 */
    private Long classRank;

    /** 年级排名 */
    private Long gradeRank;

    /** 成绩创建时间，用于趋势排序 */
    private Date createTime;

    public Long getScoreId()
    {
        return scoreId;
    }

    public void setScoreId(Long scoreId)
    {
        this.scoreId = scoreId;
    }

    public Long getStudentId()
    {
        return studentId;
    }

    public void setStudentId(Long studentId)
    {
        this.studentId = studentId;
    }

    public String getStudentNo()
    {
        return studentNo;
    }

    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
    }

    public String getStudentName()
    {
        return studentName;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public String getGrade()
    {
        return grade;
    }

    public void setGrade(String grade)
    {
        this.grade = grade;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getExamName()
    {
        return examName;
    }

    public void setExamName(String examName)
    {
        this.examName = examName;
    }

    public BigDecimal getTotalScore()
    {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore)
    {
        this.totalScore = totalScore;
    }

    public BigDecimal getAverageScore()
    {
        return averageScore;
    }

    public void setAverageScore(BigDecimal averageScore)
    {
        this.averageScore = averageScore;
    }

    public Long getClassRank()
    {
        return classRank;
    }

    public void setClassRank(Long classRank)
    {
        this.classRank = classRank;
    }

    public Long getGradeRank()
    {
        return gradeRank;
    }

    public void setGradeRank(Long gradeRank)
    {
        this.gradeRank = gradeRank;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
