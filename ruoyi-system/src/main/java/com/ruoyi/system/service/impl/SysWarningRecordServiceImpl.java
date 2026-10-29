package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysWarningRecord;
import com.ruoyi.system.domain.vo.SysWarningHandleRequestDto;
import com.ruoyi.system.mapper.SysWarningRecordMapper;
import com.ruoyi.system.service.ISysWarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 预警信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
@Service
public class SysWarningRecordServiceImpl implements ISysWarningRecordService 
{
    @Autowired
    private SysWarningRecordMapper sysWarningRecordMapper;

    /**
     * 查询预警信息
     * 
     * @param warningId 预警信息主键
     * @return 预警信息
     */
    @Override
    public SysWarningRecord selectSysWarningRecordByWarningId(Long warningId)
    {
        return sysWarningRecordMapper.selectSysWarningRecordByWarningId(warningId);
    }

    /**
     * 查询预警信息列表
     * 
     * @param sysWarningRecord 预警信息
     * @return 预警信息
     */
    @Override
    public List<SysWarningRecord> selectSysWarningRecordList(SysWarningRecord sysWarningRecord)
    {
        return sysWarningRecordMapper.selectSysWarningRecordList(sysWarningRecord);
    }

    @Override
    public int insertSysWarningRecord(SysWarningRecord sysWarningRecord)
    {
        return sysWarningRecordMapper.insertSysWarningRecord(sysWarningRecord);
    };

    /**
     * 修改预警信息
     * 
     * @param sysWarningHandleRequestDto 预警信息
     * @return 结果
     */
    @Override
    public int updateSysWarningRecord(SysWarningHandleRequestDto sysWarningHandleRequestDto)
    {
        Long userId = SecurityUtils.getUserId();
        String nickName = SecurityUtils.getLoginUser().getUser().getNickName();
        Date now = DateUtils.getNowDate();

        SysWarningRecord sysWarningRecord = new SysWarningRecord();
        sysWarningRecord.setWarningId(sysWarningHandleRequestDto.getWarningId());
        sysWarningRecord.setWarningStatus(sysWarningHandleRequestDto.getWarningStatus());
        sysWarningRecord.setHandleResult(sysWarningHandleRequestDto.getHandleResult().trim());
        sysWarningRecord.setHandleTime(now);
        sysWarningRecord.setHandlerId(userId);
        sysWarningRecord.setHandlerName(nickName);
        sysWarningRecord.setUpdateTime(now);

        int rows = sysWarningRecordMapper.updateSysWarningRecord(sysWarningRecord);
        if (rows == 0) {
            throw new ServiceException("预警不存在或已被处理，请刷新列表");
        }
        return rows;
    }

    /**
     * 批量删除预警信息
     * 
     * @param warningIds 需要删除的预警信息主键
     * @return 结果
     */
    @Override
    public int deleteSysWarningRecordByWarningIds(Long[] warningIds)
    {
        return sysWarningRecordMapper.deleteSysWarningRecordByWarningIds(warningIds);
    }

    /**
     * 删除预警信息信息
     * 
     * @param warningId 预警信息主键
     * @return 结果
     */
    @Override
    public int deleteSysWarningRecordByWarningId(Long warningId)
    {
        return sysWarningRecordMapper.deleteSysWarningRecordByWarningId(warningId);
    }
}
