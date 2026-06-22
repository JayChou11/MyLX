package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.xss.Xss;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 学生成绩对象 sys_student_score
 *
 * 这个类既承担“数据库表实体”的职责，也承担“Excel 导入导出模型”的职责：
 * 1. scoreId、studentId、examName、三科成绩、总分、平均分等字段来自 sys_student_score 表；
 * 2. studentNo、studentName、grade、className 等字段来自关联查询或 Excel 导入，不直接存到成绩表；
 * 3. 带 @Excel 的字段会出现在导入模板和导出文件中；
 * 4. getter 上的校验注解会在 Controller 的 @Validated 或导入校验时生效。
 *
 * @author ruoyi
 */
public class SysStudentScore extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 成绩ID */
    private Long scoreId;

    /**
     * 学生ID
     *
     * 数据库真正保存的是 student_id，而不是学生姓名。
     * 这样即使学生改名，成绩记录仍然能稳定关联到同一个学生。
     */
    private Long studentId;

    /**
     * 学号（关联查询/导入时使用，非数据库字段）
     *
     * 列表查询时：从 sys_student 表 join 出来给页面展示；
     * Excel 导入时：用户填写学号，Service 再根据学号反查 studentId。
     */
    @Excel(name = "学号")
    private String studentNo;

    /** 姓名（关联查询时填充，非数据库字段，只展示不入成绩表） */
    @Excel(name = "姓名")
    private String studentName;

    /** 年级（关联 sys_class 查询时填充，方便页面筛选和导出） */
    @Excel(name = "年级")
    private String grade;

    /** 班级ID（关联查询时填充，查询条件会用到，但不直接存入成绩表） */
    private Long classId;

    /** 班级名称（关联 sys_class 查询时填充，只展示不入成绩表） */
    @Excel(name = "班级")
    private String className;

    /** 考试名称 */
    @Excel(name = "考试名称")
    private String examName;

    /** 语文成绩 */
    @Excel(name = "语文成绩")
    private BigDecimal chineseScore;

    /** 数学成绩 */
    @Excel(name = "数学成绩")
    private BigDecimal mathScore;

    /** 英语成绩 */
    @Excel(name = "英语成绩")
    private BigDecimal englishScore;

    /**
     * 总分
     *
     * 这个字段虽然存入数据库，但不是前端手填的。
     * Service 会根据三科成绩统一计算，保证新增、修改、导入三种入口规则一致。
     */
    @Excel(name = "总分")
    private BigDecimal totalScore;

    /** 平均分，同样由 Service 根据总分计算后回填 */
    @Excel(name = "平均分")
    private BigDecimal averageScore;

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

    public Long getClassId()
    {
        return classId;
    }

    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    @Xss(message = "考试名称不能包含脚本字符")
    @NotBlank(message = "考试名称不能为空")
    @Size(max = 100, message = "考试名称长度不能超过100个字符")
    public String getExamName()
    {
        return examName;
    }

    public void setExamName(String examName)
    {
        this.examName = examName;
    }

    @NotNull(message = "语文成绩不能为空")
    @DecimalMin(value = "0", message = "语文成绩不能小于0")
    @DecimalMax(value = "100", message = "语文成绩不能大于100")
    public BigDecimal getChineseScore()
    {
        return chineseScore;
    }

    public void setChineseScore(BigDecimal chineseScore)
    {
        this.chineseScore = chineseScore;
    }

    @NotNull(message = "数学成绩不能为空")
    @DecimalMin(value = "0", message = "数学成绩不能小于0")
    @DecimalMax(value = "100", message = "数学成绩不能大于100")
    public BigDecimal getMathScore()
    {
        return mathScore;
    }

    public void setMathScore(BigDecimal mathScore)
    {
        this.mathScore = mathScore;
    }

    @NotNull(message = "英语成绩不能为空")
    @DecimalMin(value = "0", message = "英语成绩不能小于0")
    @DecimalMax(value = "100", message = "英语成绩不能大于100")
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("scoreId", getScoreId())
            .append("studentId", getStudentId())
            .append("studentNo", getStudentNo())
            .append("studentName", getStudentName())
            .append("grade", getGrade())
            .append("classId", getClassId())
            .append("className", getClassName())
            .append("examName", getExamName())
            .append("chineseScore", getChineseScore())
            .append("mathScore", getMathScore())
            .append("englishScore", getEnglishScore())
            .append("totalScore", getTotalScore())
            .append("averageScore", getAverageScore())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
