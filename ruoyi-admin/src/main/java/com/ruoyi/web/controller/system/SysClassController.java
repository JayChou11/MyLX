package com.ruoyi.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysClass;
import com.ruoyi.system.service.ISysClassService;

/**
 * 班级信息 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/class")
public class SysClassController extends BaseController
{
    @Autowired
    private ISysClassService classService;

    /**
     * 查询班级信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:class:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysClass sysClass)
    {
        startPage();
        List<SysClass> list = classService.selectClassList(sysClass);
        return getDataTable(list);
    }

    /**
     * 查询班级选项列表（不分页，用于下拉选择）
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect(String grade)
    {
        SysClass query = new SysClass();
        query.setGrade(grade);
        List<SysClass> list = classService.selectClassList(query);
        return success(list);
    }

    /**
     * 导出班级信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:class:export')")
    @Log(title = "班级信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysClass sysClass)
    {
        List<SysClass> list = classService.selectClassList(sysClass);
        ExcelUtil<SysClass> util = new ExcelUtil<SysClass>(SysClass.class);
        util.exportExcel(response, list, "班级数据");
    }

    /**
     * 获取班级信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:class:query')")
    @GetMapping(value = "/{classId}")
    public AjaxResult getInfo(@PathVariable("classId") Long classId)
    {
        return success(classService.selectClassByClassId(classId));
    }

    /**
     * 新增班级信息
     */
    @PreAuthorize("@ss.hasPermi('system:class:add')")
    @Log(title = "班级信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysClass sysClass)
    {
        if (!classService.checkGradeAndClassNameUnique(sysClass))
        {
            return error("新增班级'" + sysClass.getClassName() + "'失败，该年级下班级名称已存在");
        }
        sysClass.setCreateBy(getUsername());
        return toAjax(classService.insertClass(sysClass));
    }

    /**
     * 修改班级信息
     */
    @PreAuthorize("@ss.hasPermi('system:class:edit')")
    @Log(title = "班级信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysClass sysClass)
    {
        if (!classService.checkGradeAndClassNameUnique(sysClass))
        {
            return error("修改班级'" + sysClass.getClassName() + "'失败，该年级下班级名称已存在");
        }
        sysClass.setUpdateBy(getUsername());
        return toAjax(classService.updateClass(sysClass));
    }

    /**
     * 删除班级信息
     */
    @PreAuthorize("@ss.hasPermi('system:class:remove')")
    @Log(title = "班级信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{classIds}")
    public AjaxResult remove(@PathVariable Long[] classIds)
    {
        return toAjax(classService.deleteClassByClassIds(classIds));
    }
}
