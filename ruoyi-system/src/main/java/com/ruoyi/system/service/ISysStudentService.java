package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysStudent;
import com.ruoyi.system.domain.vo.SysStudentClassStat;
import com.ruoyi.system.domain.vo.SysStudentTransferDto;

/**
 * 学生信息Service接口
 * 
 * @author ruoyi
 */
public interface ISysStudentService
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
     * @return 学生信息集合cg
     */
    public List<SysStudent> selectStudentList(SysStudent student);

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
     * 批量删除学生信息
     * 
     * @param studentIds 需要删除的学生信息主键集合
     * @return 结果
     */
    public int deleteStudentByStudentIds(Long[] studentIds);

    /**
     * 删除学生信息
     * 
     * @param studentId 学生信息主键
     * @return 结果
     */
    public int deleteStudentByStudentId(Long studentId);

    /**
     * 校验学号是否唯一
     * 
     * @param student 学生信息
     * @return 结果
     */
    public boolean checkStudentNoUnique(SysStudent student);

    /**
     * 校验身份证号是否唯一
     * 
     * @param student 学生信息
     * @return 结果
     */
    public boolean checkIdCardUnique(SysStudent student);

    public List<SysStudent> selectStudentListByIds(Long[] ids);

    /**
     * 查询学生班级统计
     *
     * @return 学生班级统计集合
     */
    public List<SysStudentClassStat> selectStudentClassStatList();

    /**
     * 导入学生数据
     *
     * @param studentList 学生数据列表
     * @param isUpdateSupport 是否更新已存在数据
     * @param operName 操作人
     * @return 导入结果
     */
    public String importStudent(List<SysStudent> studentList, Boolean isUpdateSupport, String operName);

    /**
     * 批量调班
     *
     * @param transferDto 调班参数
     * @return 结果
     */
    public int transferStudentClass(SysStudentTransferDto transferDto);
}
