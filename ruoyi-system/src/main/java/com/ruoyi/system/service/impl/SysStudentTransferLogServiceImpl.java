package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysStudentTransferLog;
import com.ruoyi.system.mapper.SysStudentTransferLogMapper;
import com.ruoyi.system.service.ISysStudentTransferLogService;

/**
 * 学生调班记录Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysStudentTransferLogServiceImpl implements ISysStudentTransferLogService
{
    @Autowired
    private SysStudentTransferLogMapper transferLogMapper;

    @Override
    public SysStudentTransferLog selectStudentTransferLogByTransferId(Long transferId)
    {
        return transferLogMapper.selectStudentTransferLogByTransferId(transferId);
    }

    @Override
    public List<SysStudentTransferLog> selectStudentTransferLogList(SysStudentTransferLog studentTransferLog)
    {
        return transferLogMapper.selectStudentTransferLogList(studentTransferLog);
    }

    @Override
    public int insertStudentTransferLog(SysStudentTransferLog studentTransferLog)
    {
        return transferLogMapper.insertStudentTransferLog(studentTransferLog);
    }
}
