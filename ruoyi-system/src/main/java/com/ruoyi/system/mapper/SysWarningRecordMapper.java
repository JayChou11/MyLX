package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.SysWarningRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成绩预警记录Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
public interface SysWarningRecordMapper 
{
    /**
     * 查询成绩预警记录
     *
     * @param warningId 成绩预警记录主键
     * @return 成绩预警记录
     */
    public SysWarningRecord selectSysWarningRecordByWarningId(Long warningId);

    /**
     * 查询成绩预警记录列表
     * 
     * @param sysWarningRecord 成绩预警记录
     * @return 成绩预警记录集合
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
     * 修改成绩预警记录
     * 
     * @param sysWarningRecord 成绩预警记录
     * @return 结果
     */
    public int updateSysWarningRecord(SysWarningRecord sysWarningRecord);

    /**
     * 删除成绩预警记录
     * 
     * @param warningId 成绩预警记录主键
     * @return 结果
     */
    public int deleteSysWarningRecordByWarningId(Long warningId);

    /**
     * 批量删除成绩预警记录
     * 
     * @param warningIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysWarningRecordByWarningIds(Long[] warningIds);

    /**
     * 按唯一键查询预警记录
     *
     * @param scoreId
     * @param warningType
     * @param warningItem
     * @return
     */
    public SysWarningRecord selectByScoreIdAndTypeAndItem(@Param("scoreId") Long scoreId, @Param("warningType") String warningType, @Param("warningItem") String warningItem);
}
