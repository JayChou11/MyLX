package com.ruoyi.system.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 学生档案中的转班申请摘要
 *
 * @author ruoyi
 */
public class SysStudentProfileTransferApplyVo
{
    private Long applyId;

    private String beforeGrade;

    private String beforeClassName;

    private String afterGrade;

    private String afterClassName;

    private String applyReason;

    private String applyBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    private String status;

    private String statusLabel;

    private String rejectReason;

    public Long getApplyId()
    {
        return applyId;
    }

    public void setApplyId(Long applyId)
    {
        this.applyId = applyId;
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

    public String getApplyReason()
    {
        return applyReason;
    }

    public void setApplyReason(String applyReason)
    {
        this.applyReason = applyReason;
    }

    public String getApplyBy()
    {
        return applyBy;
    }

    public void setApplyBy(String applyBy)
    {
        this.applyBy = applyBy;
    }

    public Date getApplyTime()
    {
        return applyTime;
    }

    public void setApplyTime(Date applyTime)
    {
        this.applyTime = applyTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatusLabel()
    {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel)
    {
        this.statusLabel = statusLabel;
    }

    public String getRejectReason()
    {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason)
    {
        this.rejectReason = rejectReason;
    }
}
