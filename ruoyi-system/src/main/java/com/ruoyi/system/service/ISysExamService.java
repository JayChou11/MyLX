package com.ruoyi.system.service;

import com.ruoyi.system.domain.SysExam;

import java.util.List;

/**
 * 考试信息Service接口
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
public interface ISysExamService 
{
    /**
     * 查询考试信息
     * 
     * @param examId 考试信息主键
     * @return 考试信息
     */
    public SysExam selectSysExamByExamId(Long examId);

    /**
     * 查询考试信息列表
     * 
     * @param sysExam 考试信息
     * @return 考试信息集合
     */
    public List<SysExam> selectSysExamList(SysExam sysExam);

    /**
     * 新增考试信息
     * 
     * @param sysExam 考试信息
     * @return 结果
     */
    public int insertSysExam(SysExam sysExam);

    /**
     * 修改考试信息
     * 
     * @param sysExam 考试信息
     * @return 结果
     */
    public int updateSysExam(SysExam sysExam);

    /**
     * 批量删除考试信息
     * 
     * @param examIds 需要删除的考试信息主键集合
     * @return 结果
     */
    public int deleteSysExamByExamIds(Long[] examIds);

    /**
     * 删除考试信息信息
     * 
     * @param examId 考试信息主键
     * @return 结果
     */
    public int deleteSysExamByExamId(Long examId);
}
