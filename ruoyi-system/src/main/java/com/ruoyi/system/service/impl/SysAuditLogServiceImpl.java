package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysAuditLog;
import com.ruoyi.system.mapper.SysAuditLogMapper;
import com.ruoyi.system.service.ISysAuditLogService;

/**
 * 业务审计日志Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysAuditLogServiceImpl implements ISysAuditLogService
{
    @Autowired
    private SysAuditLogMapper auditLogMapper;

    @Override
    public List<SysAuditLog> selectAuditLogList(SysAuditLog auditLog)
    {
        return auditLogMapper.selectAuditLogList(auditLog);
    }

    @Override
    public int insertAuditLog(SysAuditLog auditLog)
    {
        return auditLogMapper.insertAuditLog(auditLog);
    }
}