package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysClass;
import com.ruoyi.system.mapper.SysClassMapper;
import com.ruoyi.system.service.ISysClassService;

/**
 * 班级信息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysClassServiceImpl implements ISysClassService
{
    @Autowired
    private SysClassMapper classMapper;

    /**
     * 查询班级信息
     *
     * @param classId 班级ID
     * @return 班级信息
     */
    @Override
    public SysClass selectClassByClassId(Long classId)
    {
        SysClass sysClass = classMapper.selectClassByClassId(classId);
        if (sysClass != null)
        {
            sysClass.setCurrentCount(classMapper.countStudentByClassId(classId));
        }
        return sysClass;
    }

    /**
     * 查询班级信息列表
     *
     * @param sysClass 班级信息
     * @return 班级信息
     */
    @Override
    public List<SysClass> selectClassList(SysClass sysClass)
    {
        List<SysClass> list = classMapper.selectClassList(sysClass);
        for (SysClass item : list)
        {
            item.setCurrentCount(classMapper.countStudentByClassId(item.getClassId()));
        }
        return list;
    }

    /**
     * 新增班级信息
     *
     * @param sysClass 班级信息
     * @return 结果
     */
    @Override
    public int insertClass(SysClass sysClass)
    {
        return classMapper.insertClass(sysClass);
    }

    /**
     * 修改班级信息
     *
     * @param sysClass 班级信息
     * @return 结果
     */
    @Override
    public int updateClass(SysClass sysClass)
    {
        return classMapper.updateClass(sysClass);
    }

    /**
     * 批量删除班级信息
     *
     * @param classIds 需要删除的班级ID
     * @return 结果
     */
    @Override
    public int deleteClassByClassIds(Long[] classIds)
    {
        for (Long classId : classIds)
        {
            checkBeforeDelete(classId);
        }
        return classMapper.deleteClassByClassIds(classIds);
    }

    /**
     * 删除班级信息
     *
     * @param classId 班级ID
     * @return 结果
     */
    @Override
    public int deleteClassByClassId(Long classId)
    {
        checkBeforeDelete(classId);
        return classMapper.deleteClassByClassId(classId);
    }

    /**
     * 校验年级+班级名称是否唯一
     *
     * @param sysClass 班级信息
     * @return 结果
     */
    @Override
    public boolean checkGradeAndClassNameUnique(SysClass sysClass)
    {
        Long classId = StringUtils.isNull(sysClass.getClassId()) ? -1L : sysClass.getClassId();
        SysClass info = classMapper.selectClassByGradeAndName(sysClass);
        return StringUtils.isNull(info) || info.getClassId().longValue() == classId.longValue();
    }

    /**
     * 删除前检查该班级下是否还有学生
     */
    private void checkBeforeDelete(Long classId)
    {
        int count = classMapper.countStudentByClassId(classId);
        if (count > 0)
        {
            throw new ServiceException("该班级下还有 " + count + " 名学生，无法删除");
        }
    }
}
