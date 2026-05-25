package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysStudentTransferLog;

/**
 * 学生调班记录Mapper接口
 *
 * @author ruoyi
 */
public interface SysStudentTransferLogMapper
{
    public SysStudentTransferLog selectStudentTransferLogByTransferId(Long transferId);

    public List<SysStudentTransferLog> selectStudentTransferLogList(SysStudentTransferLog studentTransferLog);

    public int insertStudentTransferLog(SysStudentTransferLog studentTransferLog);
}
