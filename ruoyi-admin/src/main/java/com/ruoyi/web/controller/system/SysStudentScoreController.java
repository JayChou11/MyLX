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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysStudentScore;
import com.ruoyi.system.service.ISysStudentScoreService;

/**
 * 学生成绩Controller
 *
 * Controller 是前端请求进入后端的第一站，主要负责：
 * 1. 定义请求地址和请求方式；
 * 2. 接收前端传过来的参数；
 * 3. 做权限控制、操作日志记录这类通用处理；
 * 4. 把真正的业务处理交给 Service。
 *
 * 注意：这里尽量不写复杂业务逻辑。比如“成绩是否重复”“总分怎么计算”，
 * 都放在 Service 层做，这样导入、新增、修改都能复用同一套规则。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/studentScore")
public class SysStudentScoreController extends BaseController
{
    @Autowired
    private ISysStudentScoreService studentScoreService;

    /**
     * 查询学生成绩列表
     *
     * 前端以 GET /system/studentScore/list 发起请求。
     * SysStudentScore studentScore 没有写 @RequestParam，是因为 Spring MVC
     * 会把 query string 中同名参数自动封装到对象里，例如：
     * ?studentNo=2026001&examName=期中考试
     * 会自动调用 studentScore.setStudentNo(...)、studentScore.setExamName(...)。
     *
     * startPage() 会读取 pageNum/pageSize，并让后面的 MyBatis 查询自动分页。
     */
    @PreAuthorize("@ss.hasPermi('system:studentScore:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysStudentScore studentScore)
    {
        startPage();
        List<SysStudentScore> list = studentScoreService.selectStudentScoreList(studentScore);
        return getDataTable(list);
    }

    /**
     * 查询班级成绩统计
     *
     * 这个接口返回的不是 sys_student_score 表的一行行原始成绩，
     * 而是按“年级 + 班级 + 考试名称”聚合后的统计 VO。
     * 所以返回 AjaxResult，而不是 TableDataInfo 分页表格。
     */
    @PreAuthorize("@ss.hasPermi('system:studentScore:stat')")
    @GetMapping("/classStats")
    public AjaxResult classStats(SysStudentScore studentScore)
    {
        return success(studentScoreService.selectStudentScoreClassStatList(studentScore));
    }

    /**
     * 查询某个学生的成绩趋势
     *
     * 前端点击“成绩分析”时，会把当前行的 studentId 放到路径里：
     * GET /system/studentScore/trend/100
     *
     * 返回数据是一组按考试时间排序的成绩记录，前端可以用它画折线图：
     * x 轴是考试名称，y 轴可以是总分、平均分、班级排名、年级排名。
     */
    @PreAuthorize("@ss.hasPermi('system:studentScore:query')")
    @GetMapping("/trend/{studentId}")
    public AjaxResult trend(@PathVariable("studentId") Long studentId)
    {
        return success(studentScoreService.selectStudentScoreTrendList(studentId));
    }

    /**
     * 导出学生成绩列表
     *
     * 如果 scoreIds 有值，表示用户点的是“选择导出”，只导出勾选的数据；
     * 如果 scoreIds 没有值，表示用户点的是普通“导出”，按当前查询条件导出全部匹配数据。
     */
    @PreAuthorize("@ss.hasPermi('system:studentScore:export')")
    @Log(title = "学生成绩", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysStudentScore studentScore, String scoreIds)
    {
        List<SysStudentScore> list;
        if (StringUtils.isNotEmpty(scoreIds))
        {
            // 前端传过来的是逗号分隔字符串，例如 "1,2,3"，这里转换成 Long[] 给 Mapper 的 foreach 使用。
            Long[] ids = Convert.toLongArray(scoreIds);
            list = studentScoreService.selectStudentScoreListByIds(ids);
        }
        else
        {
            list = studentScoreService.selectStudentScoreList(studentScore);
        }
        ExcelUtil<SysStudentScore> util = new ExcelUtil<SysStudentScore>(SysStudentScore.class);
        // exportExcel 会根据 SysStudentScore 字段上的 @Excel 注解生成表头并写入 response 输出流。
        util.exportExcel(response, list, "学生成绩数据");
    }

    /**
     * 导入学生成绩
     *
     * MultipartFile file 对应前端上传的 Excel 文件。
     * updateSupport 表示如果“同一学生 + 同一考试名称”的成绩已存在，是否允许覆盖更新。
     */
    @Log(title = "学生成绩", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:studentScore:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SysStudentScore> util = new ExcelUtil<SysStudentScore>(SysStudentScore.class);
        // importExcel 会把 Excel 每一行解析成一个 SysStudentScore 对象，字段匹配依赖 @Excel(name = "...")。
        List<SysStudentScore> scoreList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        // 导入时的学生是否存在、成绩是否重复、是否允许覆盖，都交给 Service 层统一处理。
        String message = studentScoreService.importStudentScore(scoreList, updateSupport, operName);
        return success(message);
    }

    /**
     * 下载导入模板
     *
     * 模板列来自 SysStudentScore 中带 @Excel 注解的字段。
     * 用户按模板填写后，再通过 importData 上传。
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SysStudentScore> util = new ExcelUtil<SysStudentScore>(SysStudentScore.class);
        util.importTemplateExcel(response, "学生成绩数据");
    }

    /**
     * 获取学生成绩详细信息
     *
     * @PathVariable("scoreId") 表示从 URL 路径里取 scoreId。
     * 例如 GET /system/studentScore/10，会把 10 赋值给 Long scoreId。
     */
    @PreAuthorize("@ss.hasPermi('system:studentScore:query')")
    @GetMapping(value = "/{scoreId}")
    public AjaxResult getInfo(@PathVariable("scoreId") Long scoreId)
    {
        return success(studentScoreService.selectStudentScoreByScoreId(scoreId));
    }

    /**
     * 新增学生成绩
     *
     * @RequestBody 表示前端传的是 JSON 请求体。
     * @Validated 会触发 SysStudentScore 中的校验注解，例如成绩不能为空、不能超过100。
     */
    @PreAuthorize("@ss.hasPermi('system:studentScore:add')")
    @Log(title = "学生成绩", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysStudentScore studentScore)
    {
        // createBy 不信任前端传值，而是以后端当前登录人为准，避免伪造操作人。
        studentScore.setCreateBy(getUsername());
        return toAjax(studentScoreService.insertStudentScore(studentScore));
    }

    /**
     * 修改学生成绩
     *
     * 修改和新增走同一个实体，但区别是修改时必须带 scoreId，
     * Service 会根据 scoreId 判断唯一性校验时是否排除自己。
     */
    @PreAuthorize("@ss.hasPermi('system:studentScore:edit')")
    @Log(title = "学生成绩", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysStudentScore studentScore)
    {
        // updateBy 同样由后端当前登录人生成，和 createBy 分开记录。
        studentScore.setUpdateBy(getUsername());
        return toAjax(studentScoreService.updateStudentScore(studentScore));
    }

    /**
     * 删除学生成绩
     *
     * @PathVariable Long[] scoreIds 支持单删和批量删除。
     * 例如 /system/studentScore/1 或 /system/studentScore/1,2,3 都可以被转换。
     */
    @PreAuthorize("@ss.hasPermi('system:studentScore:remove')")
    @Log(title = "学生成绩", businessType = BusinessType.DELETE)
    @DeleteMapping("/{scoreIds}")
    public AjaxResult remove(@PathVariable Long[] scoreIds)
    {
        return toAjax(studentScoreService.deleteStudentScoreByScoreIds(scoreIds));
    }
}
