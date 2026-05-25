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
