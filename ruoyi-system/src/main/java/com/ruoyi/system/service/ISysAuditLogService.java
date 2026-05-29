package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysAuditLog;

/**
 * 业务审计日志Service接口
 *
 * @author ruoyi
 */
public interface ISysAuditLogService
{
    /**
     * 查询审计日志列表
     *
     * @param auditLog 查询条件
     * @return 审计日志集合
     */
    public List<SysAuditLog> selectAuditLogList(SysAuditLog auditLog);

    /**
     * 新增审计日志
     *
     * @param auditLog 审计日志
     * @return 结果
     */
    public int insertAuditLog(SysAuditLog auditLog);
}