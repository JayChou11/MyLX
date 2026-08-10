package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 考试批次 对象 sys_exam
 *
 * @author ruoyi
 * @date 2026-05-11
 */
public class SysExam extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 考试批次ID */
    private Long examId;

    /** 考试姓名 */
    @Excel(name = "考试姓名")
    private String examName;/** 考试姓名 */

    @Excel(name = "年级")
    private String grade;

    @Excel(name = "学期")
    private String semester;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "考试日期")
    private Date examDate;

    @Excel(name = "状态")
    private String examStatus;

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getExamStatus() {
        return examStatus;
    }

    public void setExamStatus(String examStatus) {
        this.examStatus = examStatus;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("examId", getExamId())
                .append("examName", getExamName())
                .append("grade", getGrade())
                .append("semester", getSemester())
                .append("examDate", getExamDate())
                .append("examStatus", getExamStatus())
                .append("remark", getRemark())
                .toString();
    }
}
