package com.ruoyi.system.service;

import com.ruoyi.system.domain.SysWarningRecord;
import com.ruoyi.system.domain.vo.SysWarningHandleRequestDto;

import java.util.List;

/**
 * 预警信息Service接口
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
public interface ISysWarningRecordService 
{
    /**
     * 查询预警信息
     * 
     * @param warningId 预警信息主键
     * @return 预警信息
     */
    public SysWarningRecord selectSysWarningRecordByWarningId(Long warningId);

    /**
     * 查询预警信息列表
     *
     * @param sysWarningRecord 预警信息
     * @return 预警信息集合
     */
    public List<SysWarningRecord> selectSysWarningRecordList(SysWarningRecord sysWarningRecord);

    /**
     * 新增预警信息
     *
     * @param sysWarningRecord 预警信息
     * @return 结果
     */
    public int insertSysWarningRecord(SysWarningRecord sysWarningRecord);

    /**
     * 修改预警信息
     * 
     * @param sysWarningHandleRequestDto 预警信息
     * @return 结果
     */
    public int updateSysWarningRecord(SysWarningHandleRequestDto sysWarningHandleRequestDto);

    /**
     * 批量删除预警信息
     * 
     * @param warningIds 需要删除的预警信息主键集合
     * @return 结果
     */
    public int deleteSysWarningRecordByWarningIds(Long[] warningIds);

    /**
     * 删除预警信息信息
     * 
     * @param warningId 预警信息主键
     * @return 结果
     */
    public int deleteSysWarningRecordByWarningId(Long warningId);
}
