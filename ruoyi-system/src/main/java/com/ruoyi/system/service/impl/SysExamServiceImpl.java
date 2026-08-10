package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.SysExam;
import com.ruoyi.system.mapper.SysExamMapper;
import com.ruoyi.system.service.ISysExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 考试信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
@Service
public class SysExamServiceImpl implements ISysExamService 
{
    @Autowired
    private SysExamMapper sysExamMapper;

    /**
     * 查询考试信息
     * 
     * @param examId 考试信息主键
     * @return 考试信息
     */
    @Override
    public SysExam selectSysExamByExamId(Long examId)
    {
        return sysExamMapper.selectSysExamByExamId(examId);
    }

    /**
     * 查询考试信息列表
     * 
     * @param sysExam 考试信息
     * @return 考试信息
     */
    @Override
    public List<SysExam> selectSysExamList(SysExam sysExam)
    {
        return sysExamMapper.selectSysExamList(sysExam);
    }

    /**
     * 新增考试信息
     * 
     * @param sysExam 考试信息
     * @return 结果
     */
    @Override
    public int insertSysExam(SysExam sysExam)
    {
        if(!checkExamUnique(sysExam)) {
            throw new ServiceException("该考试批次已存在");
        }
        return sysExamMapper.insertSysExam(sysExam);
    }

    /**
     * 修改考试信息
     * 
     * @param sysExam 考试信息
     * @return 结果
     */
    @Override
    public int updateSysExam(SysExam sysExam)
    {
        if(!checkExamUnique(sysExam)) {
            throw new ServiceException("该考试批次已存在");
        }
        return sysExamMapper.updateSysExam(sysExam);
    }

    /**
     * 批量删除考试信息
     * 
     * @param examIds 需要删除的考试信息主键
     * @return 结果
     */
    @Override
    public int deleteSysExamByExamIds(Long[] examIds)
    {
        return sysExamMapper.deleteSysExamByExamIds(examIds);
    }

    /**
     * 删除考试信息信息
     * 
     * @param examId 考试信息主键
     * @return 结果
     */
    @Override
    public int deleteSysExamByExamId(Long examId)
    {
        return sysExamMapper.deleteSysExamByExamId(examId);
    }

    /**
     * 校验考试是否唯一
     *
     * @param sysExam 考试信息
     * @return 结果
     */
    private Boolean checkExamUnique(SysExam sysExam) {
        Long examId = sysExam.getExamId() == null ? -1L : sysExam.getExamId();
        SysExam info = sysExamMapper.selectSysExamByNameGradeSemester(sysExam);
        return info == null || examId.equals(info.getExamId());
    }
}
