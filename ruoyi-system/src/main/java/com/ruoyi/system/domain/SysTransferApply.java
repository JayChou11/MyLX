package com.ruoyi.system.domain;

import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 转班申请对象 sys_transfer_apply
 *
 * @author ruoyi
 */
public class SysTransferApply extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 申请ID */
    private Long applyId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long studentId;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 原班级ID */
    @Excel(name = "原班级ID")
    private Long beforeClassId;

    /** 原班级名称 */
    @Excel(name = "原班级名称")
    private String beforeClassName;

    /** 原年级 */
    @Excel(name = "原年级")
    private String beforeGrade;

    /** 目标班级ID */
    @Excel(name = "目标班级ID")
    private Long afterClassId;

    /** 目标班级名称 */
    @Excel(name = "目标班级名称")
    private String afterClassName;

    /** 目标年级 */
    @Excel(name = "目标年级")
    private String afterGrade;

    /** 申请原因 */
    @Excel(name = "申请原因")
    private String applyReason;

    /** 申请人 */
    @Excel(name = "申请人")
    private String applyBy;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    /** 状态（0待班主任审批 1待教务处审批 2已通过 3已拒绝） */
    @Excel(name = "状态", readConverterExp = "0=待班主任审批,1=待教务处审批,2=已通过,3=已拒绝")
    private String status;

    /** 拒绝原因 */
    @Excel(name = "拒绝原因")
    private String rejectReason;

    /** 审批记录列表（非数据库字段） */
    private List<SysTransferApprove> approveList;

    public Long getApplyId()
    {
        return applyId;
    }

    public void setApplyId(Long applyId)
    {
        this.applyId = applyId;
    }

    public Long getStudentId()
    {
        return studentId;
    }

    public void setStudentId(Long studentId)
    {
        this.studentId = studentId;
    }

    public String getStudentName()
    {
        return studentName;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public Long getBeforeClassId()
    {
        return beforeClassId;
    }

    public void setBeforeClassId(Long beforeClassId)
    {
        this.beforeClassId = beforeClassId;
    }

    public String getBeforeClassName()
    {
        return beforeClassName;
    }

    public void setBeforeClassName(String beforeClassName)
    {
        this.beforeClassName = beforeClassName;
    }

    public String getBeforeGrade()
    {
        return beforeGrade;
    }

    public void setBeforeGrade(String beforeGrade)
    {
        this.beforeGrade = beforeGrade;
    }

    public Long getAfterClassId()
    {
        return afterClassId;
    }

    public void setAfterClassId(Long afterClassId)
    {
        this.afterClassId = afterClassId;
    }

    public String getAfterClassName()
    {
        return afterClassName;
    }

    public void setAfterClassName(String afterClassName)
    {
        this.afterClassName = afterClassName;
    }

    public String getAfterGrade()
    {
        return afterGrade;
    }

    public void setAfterGrade(String afterGrade)
    {
        this.afterGrade = afterGrade;
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

    public String getRejectReason()
    {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason)
    {
        this.rejectReason = rejectReason;
    }

    public List<SysTransferApprove> getApproveList()
    {
        return approveList;
    }

    public void setApproveList(List<SysTransferApprove> approveList)
    {
        this.approveList = approveList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("applyId", getApplyId())
                .append("studentId", getStudentId())
                .append("studentName", getStudentName())
                .append("beforeClassId", getBeforeClassId())
                .append("beforeClassName", getBeforeClassName())
                .append("beforeGrade", getBeforeGrade())
                .append("afterClassId", getAfterClassId())
                .append("afterClassName", getAfterClassName())
                .append("afterGrade", getAfterGrade())
                .append("applyReason", getApplyReason())
                .append("applyBy", getApplyBy())
                .append("applyTime", getApplyTime())
                .append("status", getStatus())
                .append("rejectReason", getRejectReason())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
