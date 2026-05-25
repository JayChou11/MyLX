package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysStudentTransferLog;

/**
 * 学生调班记录Service接口
 *
 * @author ruoyi
 */
public interface ISysStudentTransferLogService
{
    public SysStudentTransferLog selectStudentTransferLogByTransferId(Long transferId);

    public List<SysStudentTransferLog> selectStudentTransferLogList(SysStudentTransferLog studentTransferLog);

    public int insertStudentTransferLog(SysStudentTransferLog studentTransferLog);
}
