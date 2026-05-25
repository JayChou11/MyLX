package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysClass;

/**
 * 班级信息Mapper接口
 *
 * @author ruoyi
 */
public interface SysClassMapper
{
    /**
     * 查询班级信息
     *
     * @param classId 班级ID
     * @return 班级信息
     */
    public SysClass selectClassByClassId(Long classId);

    /**
     * 查询班级信息列表
     *
     * @param sysClass 班级信息
     * @return 班级信息集合
     */
    public List<SysClass> selectClassList(SysClass sysClass);

    /**
     * 根据年级和班级名称查询班级
     *
     * @param sysClass 班级信息
     * @return 班级信息
     */
    public SysClass selectClassByGradeAndName(SysClass sysClass);

    /**
     * 新增班级信息
     *
     * @param sysClass 班级信息
     * @return 结果
     */
    public int insertClass(SysClass sysClass);

    /**
     * 修改班级信息
     *
     * @param sysClass 班级信息
     * @return 结果
     */
    public int updateClass(SysClass sysClass);

    /**
     * 删除班级信息
     *
     * @param classId 班级ID
     * @return 结果
     */
    public int deleteClassByClassId(Long classId);

    /**
     * 批量删除班级信息
     *
     * @param classIds 需要删除的班级ID集合
     * @return 结果
     */
    public int deleteClassByClassIds(Long[] classIds);

    /**
     * 查询班级下的学生人数
     *
     * @param classId 班级ID
     * @return 学生人数
     */
    public int countStudentByClassId(Long classId);
}
