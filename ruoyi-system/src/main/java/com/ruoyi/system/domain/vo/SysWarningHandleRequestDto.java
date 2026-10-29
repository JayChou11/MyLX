package com.ruoyi.system.domain.vo;

import com.ruoyi.common.xss.Xss;
import jakarta.validation.constraints.*;

/**
 * 预警记录请求对象
 *
 * @author ruoyi
 */
public class SysWarningHandleRequestDto
{
    /** 预警记录ID */
    @NotNull(message = "预警记录id不能为空")
    @Positive(message = "预警记录ID必须为正数")
    private Long warningId;

    /** 预警状态 */
    @NotNull(message = "预警状态不能为空")
    @Pattern(regexp = "^[12]$", message = "预警状态只能为已处理或已忽略")
    private String warningStatus;

    /** 处理结果 */
    @NotBlank(message = "处理结果不能为空")
    @Xss(message = "处理结果不能包含脚本字符")
    @Size(max = 500, message = "处理结果长度不能超过500个字符")
    private String handleResult;


    public Long getWarningId() {
        return warningId;
    }

    public void setWarningId(Long warningId) {
        this.warningId = warningId;
    }

    public String getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(String warningStatus) {
        this.warningStatus = warningStatus;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }
}
