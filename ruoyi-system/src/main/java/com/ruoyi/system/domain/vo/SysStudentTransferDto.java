package com.ruoyi.system.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.xss.Xss;

/**
 * 学生批量调班请求对象
 *
 * @author ruoyi
 */
public class SysStudentTransferDto
{
    /** 学生ID集合 */
    @NotEmpty(message = "请选择需要调班的学生")
    private Long[] studentIds;

    /** 调整后的班级ID */
    @NotNull(message = "目标班级不能为空")
    private Long classId;

    /** 调班备注 */
    private String remark;

    /** 更新人 */
    private String updateBy;

    public Long[] getStudentIds()
    {
        return studentIds;
    }

    public void setStudentIds(Long[] studentIds)
    {
        this.studentIds = studentIds;
    }

    public Long getClassId()
    {
        return classId;
    }

    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    @Xss(message = "备注不能包含脚本字符")
    @Size(max = 200, message = "备注长度不能超过200个字符")
    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getUpdateBy()
    {
        return updateBy;
    }

    public void setUpdateBy(String updateBy)
    {
        this.updateBy = updateBy;
    }
}
