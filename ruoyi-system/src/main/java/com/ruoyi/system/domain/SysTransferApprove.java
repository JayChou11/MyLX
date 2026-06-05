package com.ruoyi.system.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审批记录对象 sys_transfer_approve
 *
 * @author ruoyi
 */
public class SysTransferApprove extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审批ID */
    private Long approveId;

    /** 申请ID */
    @Excel(name = "申请ID")
    private Long applyId;

    /** 审批层级（1班主任 2教务处） */
    @Excel(name = "审批层级", readConverterExp = "1=班主任,2=教务处")
    private Integer approveLevel;

    /** 审批人 */
    @Excel(name = "审批人")
    private String approveBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批结果（0通过 1拒绝） */
    @Excel(name = "审批结果", readConverterExp = "0=通过,1=拒绝")
    private String approveResult;

    /** 审批意见 */
    @Excel(name = "审批意见")
    private String approveRemark;

    /** 审批层级名称（非数据库字段，用于前端展示） */
    private String approveLevelName;

    /** 审批结果名称（非数据库字段，用于前端展示） */
    private String approveResultName;

    public Long getApproveId()
    {
        return approveId;
    }

    public void setApproveId(Long approveId)
    {
        this.approveId = approveId;
    }

    public Long getApplyId()
    {
        return applyId;
    }

    public void setApplyId(Long applyId)
    {
        this.applyId = applyId;
    }

    public Integer getApproveLevel()
    {
        return approveLevel;
    }

    public void setApproveLevel(Integer approveLevel)
    {
        this.approveLevel = approveLevel;
    }

    public String getApproveBy()
    {
        return approveBy;
    }

    public void setApproveBy(String approveBy)
    {
        this.approveBy = approveBy;
    }

    public Date getApproveTime()
    {
        return approveTime;
    }

    public void setApproveTime(Date approveTime)
    {
        this.approveTime = approveTime;
    }

    public String getApproveResult()
    {
        return approveResult;
    }

    public void setApproveResult(String approveResult)
    {
        this.approveResult = approveResult;
    }

    public String getApproveRemark()
    {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark)
    {
        this.approveRemark = approveRemark;
    }

    public String getApproveLevelName()
    {
        return approveLevelName;
    }

    public void setApproveLevelName(String approveLevelName)
    {
        this.approveLevelName = approveLevelName;
    }

    public String getApproveResultName()
    {
        return approveResultName;
    }

    public void setApproveResultName(String approveResultName)
    {
        this.approveResultName = approveResultName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("approveId", getApproveId())
                .append("applyId", getApplyId())
                .append("approveLevel", getApproveLevel())
                .append("approveBy", getApproveBy())
                .append("approveTime", getApproveTime())
                .append("approveResult", getApproveResult())
                .append("approveRemark", getApproveRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .toString();
    }
}
