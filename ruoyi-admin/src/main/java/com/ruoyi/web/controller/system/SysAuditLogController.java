package com.ruoyi.web.controller.system;

import java.util.List;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysAuditLog;
import com.ruoyi.system.service.ISysAuditLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 业务审计日志 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/student/audit")
public class SysAuditLogController extends BaseController
{
    @Autowired
    private ISysAuditLogService auditLogService;

    /**
     * 查询审计日志列表
     */
    @PreAuthorize("@ss.hasPermi('system:student:audit:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysAuditLog auditLog)
    {
        startPage();
        List<SysAuditLog> list = auditLogService.selectAuditLogList(auditLog);
        return getDataTable(list);
    }

    /**
     * 导出审计日志
     */
    @PreAuthorize("@ss.hasPermi('system:student:audit:export')")
    @Log(title = "审计日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysAuditLog auditLog)
    {
        ExcelUtil<SysAuditLog> util = new ExcelUtil<SysAuditLog>(SysAuditLog.class);
        util.exportExcel(response, auditLogService.selectAuditLogList(auditLog), "审计日志数据");
    }
}