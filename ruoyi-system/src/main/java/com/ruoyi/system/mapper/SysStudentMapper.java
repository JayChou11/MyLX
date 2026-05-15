package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysStudent;

/**
 * 学生信息Mapper接口
 * 
 * @author ruoyi
 */
public interface SysStudentMapper
{
    /**
     * 查询学生信息
     * 
     * @param studentId 学生信息主键
     * @return 学生信息
     */
    public SysStudent selectStudentByStudentId(Long studentId);

    /**
     * 查询学生信息列表
     * 
     * @param student 学生信息
     * @return 学生信息集合
     */
    public List<SysStudent> selectStudentList(SysStudent student);

    /**
     * 根据ids查询学生信息列表
     *
     * @param ids 学生ids
     * @return 学生信息
     */
    public List<SysStudent> selectStudentListByIds(Long[] ids);

    /**
     * 根据学号查询学生信息
     * 
     * @param studentNo 学号
     * @return 学生信息
     */
    public SysStudent selectStudentByStudentNo(String studentNo);

    /**
     * 根据身份证号查询学生信息
     * 
     * @param idCard 身份证号
     * @return 学生信息
     */
    public SysStudent selectStudentByIdCard(String idCard);

    /**
     * 新增学生信息
     * 
     * @param student 学生信息
     * @return 结果
     */
    public int insertStudent(SysStudent student);

    /**
     * 修改学生信息
     * 
     * @param student 学生信息
     * @return 结果
     */
    public int updateStudent(SysStudent student);

    /**
     * 删除学生信息
     * 
     * @param studentId 学生信息主键
     * @return 结果
     */
    public int deleteStudentByStudentId(Long studentId);

    /**
     * 批量删除学生信息
     * 
     * @param studentIds 需要删除的学生信息主键集合
     * @return 结果
     */
    public int deleteStudentByStudentIds(Long[] studentIds);
}
