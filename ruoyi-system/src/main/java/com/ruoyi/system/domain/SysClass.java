package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.xss.Xss;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 班级信息对象 sys_class
 *
 * @author ruoyi
 */
public class SysClass extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 班级ID */
    private Long classId;

    /** 班级名称 */
    @Excel(name = "班级名称")
    private String className;

    /** 年级 */
    @Excel(name = "年级")
    private String grade;

    /** 班主任 */
    @Excel(name = "班主任")
    private String teacherName;

    /** 教室 */
    @Excel(name = "教室")
    private String classroom;

    /** 人数上限 */
    @Excel(name = "人数上限")
    private Integer maxCount;

    /** 当前学生人数（非数据库字段，统计值） */
    private Integer currentCount;

    public Long getClassId()
    {
        return classId;
    }

    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    @Xss(message = "班级名称不能包含脚本字符")
    @NotBlank(message = "班级名称不能为空")
    @Size(max = 30, message = "班级名称长度不能超过30个字符")
    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    @Xss(message = "年级不能包含脚本字符")
    @NotBlank(message = "年级不能为空")
    @Size(max = 20, message = "年级长度不能超过20个字符")
    public String getGrade()
    {
        return grade;
    }

    public void setGrade(String grade)
    {
        this.grade = grade;
    }

    @Xss(message = "班主任不能包含脚本字符")
    @Size(max = 30, message = "班主任长度不能超过30个字符")
    public String getTeacherName()
    {
        return teacherName;
    }

    public void setTeacherName(String teacherName)
    {
        this.teacherName = teacherName;
    }

    @Xss(message = "教室不能包含脚本字符")
    @Size(max = 50, message = "教室长度不能超过50个字符")
    public String getClassroom()
    {
        return classroom;
    }

    public void setClassroom(String classroom)
    {
        this.classroom = classroom;
    }

    @Min(value = 1, message = "人数上限不能小于1")
    @Max(value = 999999, message = "人数上限不能超过999999")
    public Integer getMaxCount()
    {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount)
    {
        this.maxCount = maxCount;
    }

    public Integer getCurrentCount()
    {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount)
    {
        this.currentCount = currentCount;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("classId", getClassId())
            .append("className", getClassName())
            .append("grade", getGrade())
            .append("teacherName", getTeacherName())
            .append("classroom", getClassroom())
            .append("maxCount", getMaxCount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
