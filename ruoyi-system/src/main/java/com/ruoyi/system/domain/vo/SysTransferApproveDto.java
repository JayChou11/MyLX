package com.ruoyi.system.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.xss.Xss;

/**
 * 审批请求对象
 *
 * @author ruoyi
 */
public class SysTransferApproveDto
{
    /** 申请ID */
    @NotNull(message = "申请ID不能为空")
    private Long applyId;

    /** 审批结果（0通过 1拒绝） */
    @NotBlank(message = "审批结果不能为空")
    @Pattern(regexp = "^[01]$", message = "审批结果只能是0或1")
    private String approveResult;

    /** 审批意见 */
    @Xss(message = "审批意见不能包含脚本字符")
    @Size(max = 500, message = "审批意见长度不能超过500个字符")
    private String approveRemark;

    /** 审批人 */
    private String approveBy;

    public Long getApplyId()
    {
        return applyId;
    }

    public void setApplyId(Long applyId)
    {
        this.applyId = applyId;
    }

    public String getApproveResult()
    {
        return approveResult;
    }

    public void setApproveResult(String approveResult)
    {
        this.approveResult = approveResult;
    }

    public String getApproveRemark()
    {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark)
    {
        this.approveRemark = approveRemark;
    }

    public String getApproveBy()
    {
        return approveBy;
    }

    public void setApproveBy(String approveBy)
    {
        this.approveBy = approveBy;
    }
}
