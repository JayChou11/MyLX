package com.ruoyi.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysStudentTransferLog;
import com.ruoyi.system.service.ISysStudentTransferLogService;

/**
 * 学生调班记录Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/student/transferLog")
public class SysStudentTransferLogController extends BaseController
{
    @Autowired
    private ISysStudentTransferLogService transferLogService;

    @PreAuthorize("@ss.hasPermi('system:student:transfer:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysStudentTransferLog studentTransferLog)
    {
        startPage();
        List<SysStudentTransferLog> list = transferLogService.selectStudentTransferLogList(studentTransferLog);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:student:transfer:query')")
    @GetMapping(value = "/{transferId}")
    public AjaxResult getInfo(@PathVariable("transferId") Long transferId)
    {
        return success(transferLogService.selectStudentTransferLogByTransferId(transferId));
    }

    @PreAuthorize("@ss.hasPermi('system:student:transfer:export')")
    @Log(title = "学生调班记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysStudentTransferLog studentTransferLog)
    {
        List<SysStudentTransferLog> list = transferLogService.selectStudentTransferLogList(studentTransferLog);
        ExcelUtil<SysStudentTransferLog> util = new ExcelUtil<SysStudentTransferLog>(SysStudentTransferLog.class);
        util.exportExcel(response, list, "学生调班记录");
    }
}
