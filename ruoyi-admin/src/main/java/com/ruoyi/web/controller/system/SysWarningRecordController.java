package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysWarningRecord;
import com.ruoyi.system.service.ISysWarningRecordService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预警信息Controller
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
@RestController
@RequestMapping("/system/warning-record")
public class SysWarningRecordController extends BaseController
{
    @Autowired
    private ISysWarningRecordService sysWarningRecordService;

    /**
     * 查询预警信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:warning-record:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysWarningRecord sysWarningRecord)
    {
        startPage();
        List<SysWarningRecord> list = sysWarningRecordService.selectSysWarningRecordList(sysWarningRecord);
        return getDataTable(list);
    }

    /**
     * 导出预警信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:warning-record:export')")
    @Log(title = "预警信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysWarningRecord sysWarningRecord)
    {
        List<SysWarningRecord> list = sysWarningRecordService.selectSysWarningRecordList(sysWarningRecord);
        ExcelUtil<SysWarningRecord> util = new ExcelUtil<>(SysWarningRecord.class);
        util.exportExcel(response, list, "预警信息数据");
    }

    /**
     * 获取预警信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:warning-record:query')")
    @GetMapping(value = "/{warningId}")
    public AjaxResult getInfo(@PathVariable("warningId") Long warningId)
    {
        return success(sysWarningRecordService.selectSysWarningRecordByWarningId(warningId));
    }

    /**
     * 处理预警信息
     */
    @PreAuthorize("@ss.hasPermi('system:warning-record:edit')")
    @Log(title = "预警信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysWarningRecord sysWarningRecord)
    {
        return toAjax(sysWarningRecordService.updateSysWarningRecord(sysWarningRecord));
    }

    /**
     * 删除预警信息
     */
    @PreAuthorize("@ss.hasPermi('system:warning-record:remove')")
    @Log(title = "预警信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{warningIds}")
    public AjaxResult remove(@PathVariable Long[] warningIds)
    {
        return toAjax(sysWarningRecordService.deleteSysWarningRecordByWarningIds(warningIds));
    }
}
