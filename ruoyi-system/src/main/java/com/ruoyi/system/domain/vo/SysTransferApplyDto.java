package com.ruoyi.system.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.xss.Xss;

/**
 * 转班申请请求对象
 *
 * @author ruoyi
 */
public class SysTransferApplyDto
{
    /** 学生ID */
    @NotNull(message = "学生不能为空")
    private Long studentId;

    /** 目标班级ID */
    @NotNull(message = "目标班级不能为空")
    private Long afterClassId;

    /** 申请原因 */
    @NotBlank(message = "申请原因不能为空")
    @Xss(message = "申请原因不能包含脚本字符")
    @Size(max = 500, message = "申请原因长度不能超过500个字符")
    private String applyReason;

    /** 申请人 */
    private String applyBy;

    public Long getStudentId()
    {
        return studentId;
    }

    public void setStudentId(Long studentId)
    {
        this.studentId = studentId;
    }

    public Long getAfterClassId()
    {
        return afterClassId;
    }

    public void setAfterClassId(Long afterClassId)
    {
        this.afterClassId = afterClassId;
    }

    public String getApplyReason()
    {
        return applyReason;
    }

    public void setApplyReason(String applyReason)
    {
        this.applyReason = applyReason;
    }

    public String getApplyBy()
    {
        return applyBy;
    }

    public void setApplyBy(String applyBy)
    {
        this.applyBy = applyBy;
    }
}
