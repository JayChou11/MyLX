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

    /**
     * 查询某个学生最近的转班记录
     *
     * @param studentId 学生ID
     * @return 转班记录集合
     */
    public List<SysStudentTransferLog> selectRecentStudentTransferLogListByStudentId(Long studentId);

    /**
     * 统计某个学生累计出现过的转班记录条数
     *
     * @param studentId 学生ID
     * @return 转班次数
     */
    public int countStudentTransferLogByStudentId(Long studentId);

    public int insertStudentTransferLog(SysStudentTransferLog studentTransferLog);
}
