package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.DesensitizedType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysStudent;
import com.ruoyi.system.domain.vo.SysStudentListVo;
import com.ruoyi.system.domain.vo.SysStudentTransferDto;
import com.ruoyi.system.service.ISysStudentService;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * 学生信息 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/student")
public class SysStudentController extends BaseController
{
    @Autowired
    private ISysStudentService studentService;

    /**
     * 查询学生信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:student:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysStudent student)
    {
        startPage();
        List<SysStudent> list = studentService.selectStudentList(student);
        TableDataInfo rspData = getDataTable(list);
        rspData.setRows(buildStudentListVo(list));
        return rspData;
    }

    /**
     * 查询学生班级统计
     */
    @PreAuthorize("@ss.hasPermi('system:student:list')")
    @GetMapping("/classStats")
    public AjaxResult classStats()
    {
        return success(studentService.selectStudentClassStatList());
    }

    /**
     * 导出学生信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:student:export')")
    @Log(title = "学生信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysStudent student, String studentIds)
    {
        List<SysStudent> list;
        if (StringUtils.isNotEmpty(studentIds))
        {
            Long[] ids = Convert.toLongArray(studentIds);
            list = studentService.selectStudentListByIds(ids);
        }
        else
        {
            list = studentService.selectStudentList(student);
        }
        ExcelUtil<SysStudent> util = new ExcelUtil<SysStudent>(SysStudent.class);
        util.exportExcel(response, list, "学生数据");
    }

    /**
     * 导入学生信息
     */
    @Log(title = "学生信息", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:student:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SysStudent> util = new ExcelUtil<SysStudent>(SysStudent.class);
        List<SysStudent> studentList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = studentService.importStudent(studentList, updateSupport, operName);
        return success(message);
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SysStudent> util = new ExcelUtil<SysStudent>(SysStudent.class);
        util.importTemplateExcel(response, "学生数据");
    }

    /**
     * 获取学生信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:student:query')")
    @GetMapping(value = "/{studentId}")
    public AjaxResult getInfo(@PathVariable("studentId") Long studentId)
    {
        return success(studentService.selectStudentByStudentId(studentId));
    }

    /**
     * 新增学生信息
     */
    @PreAuthorize("@ss.hasPermi('system:student:add')")
    @Log(title = "学生信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysStudent student)
    {
        if (!studentService.checkStudentNoUnique(student))
        {
            return error("新增学生'" + student.getStudentName() + "'失败，学号已存在");
        }
        if (!studentService.checkIdCardUnique(student))
        {
            return error("新增学生'" + student.getStudentName() + "'失败，身份证号已存在");
        }
        student.setCreateBy(getUsername());
        return toAjax(studentService.insertStudent(student));
    }

    /**
     * 修改学生信息
     */
    @PreAuthorize("@ss.hasPermi('system:student:edit')")
    @Log(title = "学生信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysStudent student)
    {
        if (!studentService.checkStudentNoUnique(student))
        {
            return error("修改学生'" + student.getStudentName() + "'失败，学号已存在");
        }
        if (!studentService.checkIdCardUnique(student))
        {
            return error("修改学生'" + student.getStudentName() + "'失败，身份证号已存在");
        }
        student.setUpdateBy(getUsername());
        return toAjax(studentService.updateStudent(student));
    }

    /**
     * 批量调班
     */
    @PreAuthorize("@ss.hasPermi('system:student:edit')")
    @Log(title = "学生信息", businessType = BusinessType.UPDATE)
    @PutMapping("/transferClass")
    public AjaxResult transferClass(@Validated @RequestBody SysStudentTransferDto transferDto)
    {
        transferDto.setUpdateBy(getUsername());
        return toAjax(studentService.transferStudentClass(transferDto));
    }
    /**
     * 删除学生信息
     */
    @PreAuthorize("@ss.hasPermi('system:student:remove')")
    @Log(title = "学生信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{studentIds}")
    public AjaxResult remove(@PathVariable Long[] studentIds)
    {
        return toAjax(studentService.deleteStudentByStudentIds(studentIds));
    }

    private List<SysStudentListVo> buildStudentListVo(List<SysStudent> list)
    {
        List<SysStudentListVo> voList = new ArrayList<SysStudentListVo>(list.size());
        for (SysStudent student : list)
        {
            SysStudentListVo vo = new SysStudentListVo();
            BeanUtils.copyProperties(student, vo);
            vo.setIdCard(maskIdCard(student.getIdCard()));
            voList.add(vo);
        }
        return voList;
    }

    private String maskIdCard(String idCard)
    {
        if (StringUtils.isEmpty(idCard))
        {
            return idCard;
        }
        return DesensitizedType.ID_CARD.desensitizer().apply(idCard);
    }
}
