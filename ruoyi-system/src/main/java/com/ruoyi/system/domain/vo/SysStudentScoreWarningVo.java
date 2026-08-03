package com.ruoyi.system.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生成绩预警 VO。
 *
 * 这个对象不是数据库表实体，而是“页面要展示的预警结果”：
 * 1. 保留学生基础成绩信息，方便页面直接展示；
 * 2. 增加预警类型和预警原因，说明为什么被标记为预警；
 * 3. 增加班级均分和上一次排名，帮助用户快速判断问题有多大。
 */
public class SysStudentScoreWarningVo
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

    /** 语文成绩 */
    private BigDecimal chineseScore;

    /** 数学成绩 */
    private BigDecimal mathScore;

    /** 英语成绩 */
    private BigDecimal englishScore;

    /** 总分 */
    private BigDecimal totalScore;

    /** 平均分 */
    private BigDecimal averageScore;

    /** 班级排名 */
    private Long classRank;

    /** 年级排名 */
    private Long gradeRank;

    /** 班级平均总分，用来和当前总分做对比 */
    private BigDecimal classAvgTotalScore;

    /** 上一次考试的班级排名 */
    private Long previousClassRank;

    /** 上一次考试的年级排名 */
    private Long previousGradeRank;

    /** 预警类型，多个类型用“、”拼接 */
    private String warningTypes;

    /** 预警原因，页面直接展示给用户看 */
    private String warningReason;

    /** 录入时间 */
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

    public BigDecimal getChineseScore()
    {
        return chineseScore;
    }

    public void setChineseScore(BigDecimal chineseScore)
    {
        this.chineseScore = chineseScore;
    }

    public BigDecimal getMathScore()
    {
        return mathScore;
    }

    public void setMathScore(BigDecimal mathScore)
    {
        this.mathScore = mathScore;
    }

    public BigDecimal getEnglishScore()
    {
        return englishScore;
    }

    public void setEnglishScore(BigDecimal englishScore)
    {
        this.englishScore = englishScore;
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

    public BigDecimal getClassAvgTotalScore()
    {
        return classAvgTotalScore;
    }

    public void setClassAvgTotalScore(BigDecimal classAvgTotalScore)
    {
        this.classAvgTotalScore = classAvgTotalScore;
    }

    public Long getPreviousClassRank()
    {
        return previousClassRank;
    }

    public void setPreviousClassRank(Long previousClassRank)
    {
        this.previousClassRank = previousClassRank;
    }

    public Long getPreviousGradeRank()
    {
        return previousGradeRank;
    }

    public void setPreviousGradeRank(Long previousGradeRank)
    {
        this.previousGradeRank = previousGradeRank;
    }

    public String getWarningTypes()
    {
        return warningTypes;
    }

    public void setWarningTypes(String warningTypes)
    {
        this.warningTypes = warningTypes;
    }

    public String getWarningReason()
    {
        return warningReason;
    }

    public void setWarningReason(String warningReason)
    {
        this.warningReason = warningReason;
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
