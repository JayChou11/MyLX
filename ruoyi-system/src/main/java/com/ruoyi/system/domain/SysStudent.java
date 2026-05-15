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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 学生信息对象 sys_student
 * 
 * @author ruoyi
 */
public class SysStudent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学生ID */
    private Long studentId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentNo;

    /** 姓名 */
    @Excel(name = "姓名")
    private String studentName;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 年龄 */
    @Excel(name = "年龄")
    private Integer age;

    /** 性别 */
    @Excel(name = "性别", dictType = "sys_user_sex", comboReadDict = true)
    private String gender;

    /** 年级 */
    @Excel(name = "年级")
    private String grade;

    /** 班级 */
    @Excel(name = "班级")
    private String className;

    public Long getStudentId()
    {
        return studentId;
    }

    public void setStudentId(Long studentId)
    {
        this.studentId = studentId;
    }

    @Xss(message = "学号不能包含脚本字符")
    @NotBlank(message = "学号不能为空")
    @Size(max = 32, message = "学号长度不能超过32个字符")
    public String getStudentNo()
    {
        return studentNo;
    }

    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
    }

    @Xss(message = "姓名不能包含脚本字符")
    @NotBlank(message = "姓名不能为空")
    @Size(max = 30, message = "姓名长度不能超过30个字符")
    public String getStudentName()
    {
        return studentName;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    @NotBlank(message = "身份证号不能为空")
    @Size(max = 18, message = "身份证号长度不能超过18个字符")
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{17}([0-9Xx])$)", message = "身份证号格式不正确")
    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄不能小于1")
    @Max(value = 150, message = "年龄不能大于150")
    public Integer getAge()
    {
        return age;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "0|1|2", message = "性别值不合法")
    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
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

    @Xss(message = "班级不能包含脚本字符")
    @NotBlank(message = "班级不能为空")
    @Size(max = 30, message = "班级长度不能超过30个字符")
    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("studentId", getStudentId())
            .append("studentNo", getStudentNo())
            .append("studentName", getStudentName())
            .append("idCard", getIdCard())
            .append("age", getAge())
            .append("gender", getGender())
            .append("grade", getGrade())
            .append("className", getClassName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
