package com.ruoyi.system.domain.vo;

import java.math.BigDecimal;

/**
 * 班级成绩统计视图对象
 *
 * VO 是 View Object，表示“页面需要看的数据”。
 * 它不一定和数据库表一一对应。
 *
 * 这个类对应班级成绩统计 SQL 的返回结果：
 * 一行代表“某个班级在某一次考试中的统计结果”，
 * 例如：一年级一班在 2026春季期中考试中的平均分、最高分、及格人数。
 *
 * @author ruoyi
 */
public class SysStudentScoreClassStat
{
    /** 班级ID */
    private Long classId;

    /** 年级 */
    private String grade;

    /** 班级 */
    private String className;

    /** 考试名称 */
    private String examName;

    /** 成绩人数，等价于该班级该考试下有多少条成绩记录 */
    private Long studentCount;

    /** 语文平均分 */
    private BigDecimal avgChineseScore;

    /** 数学平均分 */
    private BigDecimal avgMathScore;

    /** 英语平均分 */
    private BigDecimal avgEnglishScore;

    /** 班级平均分，这里统计的是每个学生 average_score 的平均值 */
    private BigDecimal avgScore;

    /** 最高总分 */
    private BigDecimal maxTotalScore;

    /** 最低总分 */
    private BigDecimal minTotalScore;

    /** 及格人数（按平均分 >= 60 统计） */
    private Long passCount;

    public Long getClassId()
    {
        return classId;
    }

    public void setClassId(Long classId)
    {
        this.classId = classId;
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

    public Long getStudentCount()
    {
        return studentCount;
    }

    public void setStudentCount(Long studentCount)
    {
        this.studentCount = studentCount;
    }

    public BigDecimal getAvgChineseScore()
    {
        return avgChineseScore;
    }

    public void setAvgChineseScore(BigDecimal avgChineseScore)
    {
        this.avgChineseScore = avgChineseScore;
    }

    public BigDecimal getAvgMathScore()
    {
        return avgMathScore;
    }

    public void setAvgMathScore(BigDecimal avgMathScore)
    {
        this.avgMathScore = avgMathScore;
    }

    public BigDecimal getAvgEnglishScore()
    {
        return avgEnglishScore;
    }

    public void setAvgEnglishScore(BigDecimal avgEnglishScore)
    {
        this.avgEnglishScore = avgEnglishScore;
    }

    public BigDecimal getAvgScore()
    {
        return avgScore;
    }

    public void setAvgScore(BigDecimal avgScore)
    {
        this.avgScore = avgScore;
    }

    public BigDecimal getMaxTotalScore()
    {
        return maxTotalScore;
    }

    public void setMaxTotalScore(BigDecimal maxTotalScore)
    {
        this.maxTotalScore = maxTotalScore;
    }

    public BigDecimal getMinTotalScore()
    {
        return minTotalScore;
    }

    public void setMinTotalScore(BigDecimal minTotalScore)
    {
        this.minTotalScore = minTotalScore;
    }

    public Long getPassCount()
    {
        return passCount;
    }

    public void setPassCount(Long passCount)
    {
        this.passCount = passCount;
    }
}
