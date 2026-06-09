package com.ruoyi.web.controller.system;

import java.util.List;
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
import com.ruoyi.system.domain.SysTransferApply;
import com.ruoyi.system.domain.vo.SysTransferApplyDto;
import com.ruoyi.system.domain.vo.SysTransferApproveDto;
import com.ruoyi.system.service.ISysTransferApplyService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 转班申请Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/student/transferApply")
public class SysTransferApplyController extends BaseController
{
    @Autowired
    private ISysTransferApplyService transferApplyService;

    /**
     * 查询转班申请列表
     */
    @PreAuthorize("@ss.hasPermi('system:student:transferApprove:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysTransferApply transferApply)
    {
        startPage();
        List<SysTransferApply> list = transferApplyService.selectTransferApplyList(transferApply);
        return getDataTable(list);
    }

    /**
     * 查询待我审批的申请列表
     */
    @PreAuthorize("@ss.hasPermi('system:student:transferApprove:list')")
    @GetMapping("/myApprove/{approveLevel}")
    public TableDataInfo myApprove(@PathVariable Integer approveLevel, String status)
    {
        startPage();
        List<SysTransferApply> list = transferApplyService.selectMyApproveList(approveLevel, status);
        return getDataTable(list);
    }

    /**
     * 获取转班申请详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:student:transferApprove:query')")
    @GetMapping("/{applyId}")
    public AjaxResult getInfo(@PathVariable Long applyId)
    {
        return success(transferApplyService.selectTransferApplyByApplyId(applyId));
    }

    /**
     * 新增转班申请
     */
    @PreAuthorize("@ss.hasPermi('system:student:transferApprove:add')")
    @Log(title = "转班申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysTransferApplyDto applyDto)
    {
        applyDto.setApplyBy(getUsername());
        return toAjax(transferApplyService.insertTransferApply(applyDto));
    }

    /**
     * 审批转班申请
     */
    @PreAuthorize("@ss.hasPermi('system:student:transferApprove:approve')")
    @Log(title = "转班审批", businessType = BusinessType.UPDATE)
    @PutMapping("/approve")
    public AjaxResult approve(@Validated @RequestBody SysTransferApproveDto approveDto)
    {
        return toAjax(transferApplyService.approveTransferApply(
                approveDto.getApplyId(),
                approveDto.getApproveResult(),
                approveDto.getApproveRemark(),
                getUsername()));
    }

    /**
     * 撤回转班申请
     */
    @PreAuthorize("@ss.hasPermi('system:student:transferApprove:cancel')")
    @Log(title = "转班申请", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{applyId}")
    public AjaxResult cancel(@PathVariable Long applyId)
    {
        return toAjax(transferApplyService.cancelTransferApply(applyId, getUsername()));
    }

    /**
     * 删除转班申请
     */
    @PreAuthorize("@ss.hasPermi('system:student:transferApprove:remove')")
    @Log(title = "转班申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{applyIds}")
    public AjaxResult remove(@PathVariable Long[] applyIds)
    {
        return toAjax(transferApplyService.deleteTransferApplyByApplyIds(applyIds));
    }

    /**
     * 导出转班申请列表
     */
    @PreAuthorize("@ss.hasPermi('system:student:transferApprove:export')")
    @Log(title = "转班申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysTransferApply transferApply)
    {
        List<SysTransferApply> list = transferApplyService.selectTransferApplyList(transferApply);
        ExcelUtil<SysTransferApply> util = new ExcelUtil<SysTransferApply>(SysTransferApply.class);
        util.exportExcel(response, list, "转班申请数据");
    }

    /**
     * 统计待审批数量
     *
     * @param approveLevel 审批层级
     * @return 待审批数量
     */
    @PreAuthorize("@ss.hasPermi('system:student:transferApprove:list')")
    @GetMapping("/pendingCount/{approveLevel}")
    public AjaxResult pendingCount(@PathVariable Integer approveLevel)
    {
        return success(transferApplyService.countPendingByLevel(approveLevel));
    }
}
