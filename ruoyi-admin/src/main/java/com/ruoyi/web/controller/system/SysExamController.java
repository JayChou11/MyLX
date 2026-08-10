package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysExam;
import com.ruoyi.system.service.ISysExamService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试信息Controller
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
@RestController
@RequestMapping("/system/exam")
public class SysExamController extends BaseController
{
    @Autowired
    private ISysExamService sysExamService;

    /**
     * 查询考试信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:exam:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysExam sysExam)
    {
        startPage();
        List<SysExam> list = sysExamService.selectSysExamList(sysExam);
        return getDataTable(list);
    }

    /**
     * 导出考试信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:exam:export')")
    @Log(title = "考试信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysExam sysExam)
    {
        List<SysExam> list = sysExamService.selectSysExamList(sysExam);
        ExcelUtil<SysExam> util = new ExcelUtil<SysExam>(SysExam.class);
        util.exportExcel(response, list, "考试信息数据");
    }

    /**
     * 获取考试信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:exam:query')")
    @GetMapping(value = "/{examId}")
    public AjaxResult getInfo(@PathVariable("examId") Long examId)
    {
        return success(sysExamService.selectSysExamByExamId(examId));
    }

    /**
     * 新增考试信息
     */
    @PreAuthorize("@ss.hasPermi('system:exam:add')")
    @Log(title = "考试信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysExam sysExam)
    {
        return toAjax(sysExamService.insertSysExam(sysExam));
    }

    /**
     * 修改考试信息
     */
    @PreAuthorize("@ss.hasPermi('system:exam:edit')")
    @Log(title = "考试信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysExam sysExam)
    {
        return toAjax(sysExamService.updateSysExam(sysExam));
    }

    /**
     * 删除考试信息
     */
    @PreAuthorize("@ss.hasPermi('system:exam:remove')")
    @Log(title = "考试信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{examIds}")
    public AjaxResult remove(@PathVariable Long[] examIds)
    {
        return toAjax(sysExamService.deleteSysExamByExamIds(examIds));
    }
}
