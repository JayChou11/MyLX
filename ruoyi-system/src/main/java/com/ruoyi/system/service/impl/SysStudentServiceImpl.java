package com.ruoyi.system.service.impl;

import java.util.List;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.system.domain.SysStudent;
import com.ruoyi.system.mapper.SysStudentMapper;
import com.ruoyi.system.service.ISysStudentService;

/**
 * 学生信息Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysStudentServiceImpl implements ISysStudentService
{
    private static final Logger log = LoggerFactory.getLogger(SysStudentServiceImpl.class);

    @Autowired
    private SysStudentMapper studentMapper;

    @Autowired
    protected Validator validator;

    /**
     * 查询学生信息
     * 
     * @param studentId 学生信息主键
     * @return 学生信息
     */
    @Override
    public SysStudent selectStudentByStudentId(Long studentId)
    {
        return studentMapper.selectStudentByStudentId(studentId);
    }

    /**
     * 查询学生信息列表
     * 
     * @param student 学生信息
     * @return 学生信息
     */
    @Override
    public List<SysStudent> selectStudentList(SysStudent student)
    {
        return studentMapper.selectStudentList(student);
    }

    /**
     * 根据ids查询学生信息列表
     *
     * @param ids 学生ids
     * @return 学生信息
     */
    @Override
    public List<SysStudent> selectStudentListByIds(Long[] ids)
    {
        return studentMapper.selectStudentListByIds(ids);
    }

    /**
     * 新增学生信息
     * 
     * @param student 学生信息
     * @return 结果
     */
    @Override
    public int insertStudent(SysStudent student)
    {
        return studentMapper.insertStudent(student);
    }

    /**
     * 修改学生信息
     * 
     * @param student 学生信息
     * @return 结果
     */
    @Override
    public int updateStudent(SysStudent student)
    {
        return studentMapper.updateStudent(student);
    }

    /**
     * 批量删除学生信息
     * 
     * @param studentIds 需要删除的学生信息主键
     * @return 结果
     */
    @Override
    public int deleteStudentByStudentIds(Long[] studentIds)
    {
        return studentMapper.deleteStudentByStudentIds(studentIds);
    }

    /**
     * 删除学生信息信息
     * 
     * @param studentId 学生信息主键
     * @return 结果
     */
    @Override
    public int deleteStudentByStudentId(Long studentId)
    {
        return studentMapper.deleteStudentByStudentId(studentId);
    }

    /**
     * 校验学号是否唯一
     * 
     * @param student 学生信息
     * @return 结果
     */
    @Override
    public boolean checkStudentNoUnique(SysStudent student)
    {
        Long studentId = StringUtils.isNull(student.getStudentId()) ? -1L : student.getStudentId();
        SysStudent info = studentMapper.selectStudentByStudentNo(student.getStudentNo());
        return StringUtils.isNull(info) || info.getStudentId().longValue() == studentId.longValue();
    }

    /**
     * 校验身份证号是否唯一
     * 
     * @param student 学生信息
     * @return 结果
     */
    @Override
    public boolean checkIdCardUnique(SysStudent student)
    {
        Long studentId = StringUtils.isNull(student.getStudentId()) ? -1L : student.getStudentId();
        SysStudent info = studentMapper.selectStudentByIdCard(student.getIdCard());
        return StringUtils.isNull(info) || info.getStudentId().longValue() == studentId.longValue();
    }

    /**
     * 导入学生数据
     *
     * @param studentList 学生数据列表
     * @param isUpdateSupport 是否更新已存在数据
     * @param operName 操作人
     * @return 导入结果
     */
    @Override
    public String importStudent(List<SysStudent> studentList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(studentList) || studentList.size() == 0)
        {
            throw new ServiceException("导入学生数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysStudent student : studentList)
        {
            try
            {
                BeanValidators.validateWithException(validator, student);
                SysStudent existStudent = studentMapper.selectStudentByStudentNo(student.getStudentNo());
                if (StringUtils.isNull(existStudent))
                {
                    if (!checkIdCardUnique(student))
                    {
                        throw new ServiceException("身份证号已存在");
                    }
                    student.setCreateBy(operName);
                    studentMapper.insertStudent(student);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、学号 ")
                        .append(student.getStudentNo()).append(" 导入成功");
                }
                else if (isUpdateSupport)
                {
                    student.setStudentId(existStudent.getStudentId());
                    if (!checkIdCardUnique(student))
                    {
                        throw new ServiceException("身份证号已存在");
                    }
                    student.setUpdateBy(operName);
                    studentMapper.updateStudent(student);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、学号 ")
                        .append(student.getStudentNo()).append(" 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、学号 ")
                        .append(student.getStudentNo()).append(" 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、学号 " + student.getStudentNo() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        return successMsg.toString();
    }
}
