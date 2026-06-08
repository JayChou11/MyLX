package com.ruoyi.system.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 学生档案中的转班记录摘要
 *
 * @author ruoyi
 */
public class SysStudentProfileTransferLogVo
{
    private Long transferId;

    private String beforeGrade;

    private String beforeClassName;

    private String afterGrade;

    private String afterClassName;

    private String remark;

    private String transferBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date transferTime;

    public Long getTransferId()
    {
        return transferId;
    }

    public void setTransferId(Long transferId)
    {
        this.transferId = transferId;
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
}
