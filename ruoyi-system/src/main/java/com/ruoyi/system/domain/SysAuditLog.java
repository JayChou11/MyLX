package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 业务审计日志对象 sys_audit_log
 *
 * 与 sys_oper_log（操作日志）的区别：
 * - oper_log 记录请求级别的信息（URL、请求参数、响应结果）
 * - audit_log 记录业务级别的变更（变更前数据、变更后数据、变更摘要）
 *
 * @author ruoyi
 */
public class SysAuditLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审计日志ID */
    private Long auditId;

    /** 业务类型（TRANSFER=调班,UPGRADE=升年级,DELETE=删除等） */
    @Excel(name = "业务类型")
    private String businessType;

    /** 业务描述 */
    @Excel(name = "业务描述")
    private String detail;

    /** 操作方法（类名.方法名） */
    @Excel(name = "操作方法")
    private String method;

    /** 操作人 */
    @Excel(name = "操作人")
    private String operName;

    /** 操作IP */
    @Excel(name = "操作IP")
    private String operIp;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operTime;

    /** 变更前数据（JSON） */
    private String beforeData;

    /** 变更后数据（JSON） */
    private String afterData;

    /** 变更摘要 */
    @Excel(name = "变更摘要")
    private String changeSummary;

    /** 操作状态（0成功 1失败） */
    @Excel(name = "状态", readConverterExp = "0=成功,1=失败")
    private Integer status;

    /** 错误消息 */
    private String errorMsg;

    public Long getAuditId()
    {
        return auditId;
    }

    public void setAuditId(Long auditId)
    {
        this.auditId = auditId;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public String getDetail()
    {
        return detail;
    }

    public void setDetail(String detail)
    {
        this.detail = detail;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public String getOperName()
    {
        return operName;
    }

    public void setOperName(String operName)
    {
        this.operName = operName;
    }

    public String getOperIp()
    {
        return operIp;
    }

    public void setOperIp(String operIp)
    {
        this.operIp = operIp;
    }

    public Date getOperTime()
    {
        return operTime;
    }

    public void setOperTime(Date operTime)
    {
        this.operTime = operTime;
    }

    public String getBeforeData()
    {
        return beforeData;
    }

    public void setBeforeData(String beforeData)
    {
        this.beforeData = beforeData;
    }

    public String getAfterData()
    {
        return afterData;
    }

    public void setAfterData(String afterData)
    {
        this.afterData = afterData;
    }

    public String getChangeSummary()
    {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary)
    {
        this.changeSummary = changeSummary;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }
}