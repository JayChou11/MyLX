package com.ruoyi.system.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 学生调班记录 sys_student_transfer_log
 *
 * @author ruoyi
 */
public class SysStudentTransferLog
{
    private Long transferId;

    private Map<String, Object> params;

    @Excel(name = "学生ID集合")
    private String studentIds;

    @Excel(name = "调班人数")
    private Integer studentCount;

    @Excel(name = "调班前年级")
    private String beforeGrade;

    @Excel(name = "调班前班级")
    private String beforeClassName;

    @Excel(name = "调班后年级")
    private String afterGrade;

    @Excel(name = "调班后班级")
    private String afterClassName;

    @Excel(name = "调班原因")
    private String remark;

    @Excel(name = "操作人")
    private String transferBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "调班时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date transferTime;

    public Long getTransferId()
    {
        return transferId;
    }

    public void setTransferId(Long transferId)
    {
        this.transferId = transferId;
    }

    public Map<String, Object> getParams()
    {
        if (params == null)
        {
            params = new HashMap<String, Object>();
        }
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }

    public String getStudentIds()
    {
        return studentIds;
    }

    public void setStudentIds(String studentIds)
    {
        this.studentIds = studentIds;
    }

    public Integer getStudentCount()
    {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount)
    {
        this.studentCount = studentCount;
    }

    public String getBeforeGrade()
    {
        return beforeGrade;
    }

    public void setBeforeGrade(String beforeGrade)
    {
        this.beforeGrade = beforeGrade;
    }

    public String getBeforeClassName()
    {
        return beforeClassName;
    }

    public void setBeforeClassName(String beforeClassName)
    {
        this.beforeClassName = beforeClassName;
    }

    public String getAfterGrade()
    {
        return afterGrade;
    }

    public void setAfterGrade(String afterGrade)
    {
        this.afterGrade = afterGrade;
    }

    public String getAfterClassName()
    {
        return afterClassName;
    }

    public void setAfterClassName(String afterClassName)
    {
        this.afterClassName = afterClassName;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getTransferBy()
    {
        return transferBy;
    }

    public void setTransferBy(String transferBy)
    {
        this.transferBy = transferBy;
    }

    public Date getTransferTime()
    {
        return transferTime;
    }

    public void setTransferTime(Date transferTime)
    {
        this.transferTime = transferTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("transferId", getTransferId())
            .append("studentIds", getStudentIds())
            .append("studentCount", getStudentCount())
            .append("beforeGrade", getBeforeGrade())
            .append("beforeClassName", getBeforeClassName())
            .append("afterGrade", getAfterGrade())
            .append("afterClassName", getAfterClassName())
            .append("remark", getRemark())
            .append("transferBy", getTransferBy())
            .append("transferTime", getTransferTime())
            .toString();
    }
}
