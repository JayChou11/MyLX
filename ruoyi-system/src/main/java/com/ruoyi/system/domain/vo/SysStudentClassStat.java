package com.ruoyi.system.domain.vo;

/**
 * 学生班级统计视图对象
 *
 * @author ruoyi
 */
public class SysStudentClassStat
{
    /** 班级ID */
    private Long classId;

    /** 年级 */
    private String grade;

    /** 班级 */
    private String className;

    /** 学生人数 */
    private Long studentCount;

    /** 人数上限 */
    private Long maxCount;

    /** 剩余名额 */
    private Long remainingCount;

    /** 容量状态 */
    private String capacityStatus;

    /** 容量状态名称 */
    private String capacityStatusLabel;

    /** 男生人数 */
    private Long maleCount;

    /** 女生人数 */
    private Long femaleCount;

    /** 未知性别人数 */
    private Long unknownCount;

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

    public Long getStudentCount()
    {
        return studentCount;
    }

    public void setStudentCount(Long studentCount)
    {
        this.studentCount = studentCount;
    }

    public Long getMaxCount()
    {
        return maxCount;
    }

    public void setMaxCount(Long maxCount)
    {
        this.maxCount = maxCount;
    }

    public Long getRemainingCount()
    {
        return remainingCount;
    }

    public void setRemainingCount(Long remainingCount)
    {
        this.remainingCount = remainingCount;
    }

    public String getCapacityStatus()
    {
        return capacityStatus;
    }

    public void setCapacityStatus(String capacityStatus)
    {
        this.capacityStatus = capacityStatus;
    }

    public String getCapacityStatusLabel()
    {
        return capacityStatusLabel;
    }

    public void setCapacityStatusLabel(String capacityStatusLabel)
    {
        this.capacityStatusLabel = capacityStatusLabel;
    }

    public Long getMaleCount()
    {
        return maleCount;
    }

    public void setMaleCount(Long maleCount)
    {
        this.maleCount = maleCount;
    }

    public Long getFemaleCount()
    {
        return femaleCount;
    }

    public void setFemaleCount(Long femaleCount)
    {
        this.femaleCount = femaleCount;
    }

    public Long getUnknownCount()
    {
        return unknownCount;
    }

    public void setUnknownCount(Long unknownCount)
    {
        this.unknownCount = unknownCount;
    }
}
