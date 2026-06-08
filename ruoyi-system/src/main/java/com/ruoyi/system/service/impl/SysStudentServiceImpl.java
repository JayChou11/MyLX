package com.ruoyi.system.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.system.domain.SysClass;
import com.ruoyi.system.domain.SysStudent;
import com.ruoyi.system.domain.SysStudentTransferLog;
import com.ruoyi.system.domain.SysTransferApply;
import com.ruoyi.system.domain.vo.SysStudentClassStat;
import com.ruoyi.system.domain.vo.SysStudentProfileClassVo;
import com.ruoyi.system.domain.vo.SysStudentProfileTransferApplyVo;
import com.ruoyi.system.domain.vo.SysStudentProfileTransferLogVo;
import com.ruoyi.system.domain.vo.SysStudentProfileVo;
import com.ruoyi.system.domain.vo.SysStudentTransferDto;
import com.ruoyi.system.mapper.SysClassMapper;
import com.ruoyi.system.mapper.SysStudentMapper;
import com.ruoyi.system.mapper.SysStudentTransferLogMapper;
import com.ruoyi.system.mapper.SysTransferApplyMapper;
import com.ruoyi.system.service.ISysStudentService;
import com.ruoyi.system.service.ISysStudentTransferLogService;

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
    private SysClassMapper classMapper;

    @Autowired
    private ISysStudentTransferLogService transferLogService;

    @Autowired
    private SysStudentTransferLogMapper studentTransferLogMapper;

    @Autowired
    private SysTransferApplyMapper transferApplyMapper;

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
     * 查询学生完整档案
     *
     * @param studentId 学生ID
     * @return 学生完整档案
     */
    @Override
    public SysStudentProfileVo selectStudentProfileByStudentId(Long studentId)
    {
        // 第一步先查学生主表，确认学生存在，同时拿到后续查询班级所需的 classId。
        SysStudent student = studentMapper.selectStudentByStudentId(studentId);
        if (student == null)
        {
            throw new ServiceException("学生不存在");
        }

        // 使用 VO 返回页面需要的聚合数据，避免把多个数据库实体直接暴露给前端。
        SysStudentProfileVo profile = new SysStudentProfileVo();
        profile.setStudentId(student.getStudentId());
        profile.setStudentNo(student.getStudentNo());
        profile.setStudentName(student.getStudentName());
        profile.setIdCard(student.getIdCard());
        profile.setAge(student.getAge());
        profile.setGender(student.getGender());
        profile.setPhoto(student.getPhoto());
        profile.setRemark(student.getRemark());
        // 当前班级信息来自班级表，最近申请和最近日志来自转班相关表。
        profile.setCurrentClassInfo(buildClassInfo(student.getClassId()));
        profile.setTransferCount(studentTransferLogMapper.countStudentTransferLogByStudentId(studentId));
        profile.setRecentTransferApplies(buildTransferApplyVoList(
                transferApplyMapper.selectRecentTransferApplyListByStudentId(studentId)));
        profile.setRecentTransferLogs(buildTransferLogVoList(
                studentTransferLogMapper.selectRecentStudentTransferLogListByStudentId(studentId)));
        return profile;
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
     * 查询学生班级统计（带缓存）
     *
     * @return 学生班级统计集合
     */
    @Override
    @Cacheable(value = "studentClassStat", key = "'all'")
    public List<SysStudentClassStat> selectStudentClassStatList()
    {
        return studentMapper.selectStudentClassStatList();
    }

    /**
     * 批量调班
     *
     * @param transferDto 调班参数
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "studentClassStat", key = "'all'")
    public int transferStudentClass(SysStudentTransferDto transferDto)
    {
        if (transferDto.getStudentIds() == null || transferDto.getStudentIds().length == 0)
        {
            throw new ServiceException("请选择需要调班的学生");
        }
        // 校验目标班级是否存在
        SysClass targetClass = classMapper.selectClassByClassId(transferDto.getClassId());
        if (targetClass == null)
        {
            throw new ServiceException("目标班级不存在");
        }
        List<SysStudent> beforeList = studentMapper.selectStudentListByIds(transferDto.getStudentIds());
        if (beforeList == null || beforeList.isEmpty())
        {
            throw new ServiceException("未查询到需要调班的学生");
        }
        int rows = studentMapper.transferStudentClass(transferDto);
        if (rows <= 0)
        {
            throw new ServiceException("批量调班失败");
        }
        transferLogService.insertStudentTransferLog(buildTransferLog(transferDto, beforeList, targetClass));
        return rows;
    }

    /**
     * 新增学生信息
     *
     * @param student 学生信息
     * @return 结果
     */
    @Override
    @CacheEvict(value = "studentClassStat", key = "'all'")
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
    @CacheEvict(value = "studentClassStat", key = "'all'")
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
    @CacheEvict(value = "studentClassStat", key = "'all'")
    public int deleteStudentByStudentIds(Long[] studentIds)
    {
        return studentMapper.deleteStudentByStudentIds(studentIds);
    }

    /**
     * 删除学生信息
     *
     * @param studentId 学生信息主键
     * @return 结果
     */
    @Override
    @CacheEvict(value = "studentClassStat", key = "'all'")
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
     * @param isUpdateSupport 是否更新已经存在的数据
     * @param operName 操作人
     * @return 导入结果
     */
    @Override
    @CacheEvict(value = "studentClassStat", key = "'all'")
    public String importStudent(List<SysStudent> studentList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(studentList) || studentList.size() == 0)
        {
            throw new ServiceException("导入学生数据不能为空");
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

    private SysStudentTransferLog buildTransferLog(SysStudentTransferDto transferDto, List<SysStudent> beforeList, SysClass targetClass)
    {
        SysStudentTransferLog transferLog = new SysStudentTransferLog();
        transferLog.setStudentIds(Arrays.stream(transferDto.getStudentIds()).map(String::valueOf).collect(Collectors.joining(",")));
        transferLog.setStudentCount(beforeList.size());
        transferLog.setBeforeGrade(buildSummary(beforeList.stream().map(SysStudent::getGrade).collect(Collectors.toList())));
        transferLog.setBeforeClassName(buildSummary(beforeList.stream().map(SysStudent::getClassName).collect(Collectors.toList())));
        transferLog.setAfterGrade(targetClass.getGrade());
        transferLog.setAfterClassName(targetClass.getClassName());
        transferLog.setRemark(transferDto.getRemark());
        transferLog.setTransferBy(transferDto.getUpdateBy());
        transferLog.setTransferTime(DateUtils.getNowDate());
        return transferLog;
    }

    private String buildSummary(List<String> values)
    {
        return values.stream()
            .filter(StringUtils::isNotEmpty)
            .distinct()
            .collect(Collectors.joining(", "));
    }

    private SysStudentProfileClassVo buildClassInfo(Long classId)
    {
        if (classId == null)
        {
            return null;
        }
        // 学生表只保存 classId，这里再查班级表补全班级名称、班主任、教室等展示信息。
        SysClass sysClass = classMapper.selectClassByClassId(classId);
        if (sysClass == null)
        {
            return null;
        }
        SysStudentProfileClassVo classInfo = new SysStudentProfileClassVo();
        classInfo.setClassId(sysClass.getClassId());
        classInfo.setGrade(sysClass.getGrade());
        classInfo.setClassName(sysClass.getClassName());
        classInfo.setTeacherName(sysClass.getTeacherName());
        classInfo.setClassroom(sysClass.getClassroom());
        classInfo.setMaxCount(sysClass.getMaxCount());
        return classInfo;
    }

    private List<SysStudentProfileTransferApplyVo> buildTransferApplyVoList(List<SysTransferApply> applyList)
    {
        return applyList.stream().map(this::buildTransferApplyVo).collect(Collectors.toList());
    }

    private SysStudentProfileTransferApplyVo buildTransferApplyVo(SysTransferApply apply)
    {
        // 申请表字段较多，这里只挑完整档案弹窗需要展示的字段。
        SysStudentProfileTransferApplyVo vo = new SysStudentProfileTransferApplyVo();
        vo.setApplyId(apply.getApplyId());
        vo.setBeforeGrade(apply.getBeforeGrade());
        vo.setBeforeClassName(apply.getBeforeClassName());
        vo.setAfterGrade(apply.getAfterGrade());
        vo.setAfterClassName(apply.getAfterClassName());
        vo.setApplyReason(apply.getApplyReason());
        vo.setApplyBy(apply.getApplyBy());
        vo.setApplyTime(apply.getApplyTime());
        vo.setStatus(apply.getStatus());
        vo.setStatusLabel(buildTransferApplyStatusLabel(apply.getStatus()));
        vo.setRejectReason(apply.getRejectReason());
        return vo;
    }

    private List<SysStudentProfileTransferLogVo> buildTransferLogVoList(List<SysStudentTransferLog> transferLogs)
    {
        return transferLogs.stream().map(this::buildTransferLogVo).collect(Collectors.toList());
    }

    private SysStudentProfileTransferLogVo buildTransferLogVo(SysStudentTransferLog transferLog)
    {
        // 转班日志用于展示学生历史流转，不需要把 student_ids 等批量字段返回给页面。
        SysStudentProfileTransferLogVo vo = new SysStudentProfileTransferLogVo();
        vo.setTransferId(transferLog.getTransferId());
        vo.setBeforeGrade(transferLog.getBeforeGrade());
        vo.setBeforeClassName(transferLog.getBeforeClassName());
        vo.setAfterGrade(transferLog.getAfterGrade());
        vo.setAfterClassName(transferLog.getAfterClassName());
        vo.setRemark(transferLog.getRemark());
        vo.setTransferBy(transferLog.getTransferBy());
        vo.setTransferTime(transferLog.getTransferTime());
        return vo;
    }

    private String buildTransferApplyStatusLabel(String status)
    {
        if ("0".equals(status))
        {
            return "待班主任审批";
        }
        if ("1".equals(status))
        {
            return "待教务处审批";
        }
        if ("2".equals(status))
        {
            return "已通过";
        }
        if ("3".equals(status))
        {
            return "已拒绝";
        }
        return "未知状态";
    }

    /**
     * 刷新班级统计缓存（先清除再查询，触发缓存重建）
     */
    @Override
    @CacheEvict(value = "studentClassStat", key = "'all'")
    public void refreshClassStatCache()
    {
        // 清除缓存后，下次查询会自动重新加载
    }
}
