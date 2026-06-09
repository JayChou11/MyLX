package com.ruoyi.system.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysClass;
import com.ruoyi.system.domain.SysStudent;
import com.ruoyi.system.domain.SysTransferApply;
import com.ruoyi.system.domain.SysTransferApprove;
import com.ruoyi.system.domain.vo.SysStudentTransferDto;
import com.ruoyi.system.domain.vo.SysTransferApplyDto;
import com.ruoyi.system.mapper.SysClassMapper;
import com.ruoyi.system.mapper.SysStudentMapper;
import com.ruoyi.system.mapper.SysTransferApplyMapper;
import com.ruoyi.system.mapper.SysTransferApproveMapper;
import com.ruoyi.system.service.ISysStudentService;
import com.ruoyi.system.service.ISysTransferApplyService;

/**
 * 转班申请Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysTransferApplyServiceImpl implements ISysTransferApplyService
{
    private static final String ROLE_CLASS_TEACHER = "class_teacher";

    private static final String ROLE_ACADEMIC_OFFICE = "academic_office";

    @Autowired
    private SysTransferApplyMapper transferApplyMapper;

    @Autowired
    private SysTransferApproveMapper transferApproveMapper;

    @Autowired
    private SysStudentMapper studentMapper;

    @Autowired
    private SysClassMapper classMapper;

    @Autowired
    private ISysStudentService studentService;

    /**
     * 查询转班申请
     *
     * @param applyId 申请ID
     * @return 转班申请
     */
    @Override
    public SysTransferApply selectTransferApplyByApplyId(Long applyId)
    {
        SysTransferApply apply = transferApplyMapper.selectTransferApplyByApplyId(applyId);
        validateViewPermission(apply);
        if (apply != null)
        {
            List<SysTransferApprove> approveList = transferApproveMapper.selectTransferApproveByApplyId(applyId);
            apply.setApproveList(approveList);
        }
        return apply;
    }

    /**
     * 查询转班申请列表
     *
     * @param transferApply 转班申请
     * @return 转班申请
     */
    @Override
    public List<SysTransferApply> selectTransferApplyList(SysTransferApply transferApply)
    {
        SysTransferApply query = transferApply == null ? new SysTransferApply() : transferApply;
        appendDataScope(query);
        return transferApplyMapper.selectTransferApplyList(query);
    }

    /**
     * 查询待我审批的申请列表
     *
     * @param approveLevel 审批层级
     * @param status 申请状态
     * @return 转班申请集合
     */
    @Override
    public List<SysTransferApply> selectMyApproveList(Integer approveLevel, String status)
    {
        if (approveLevel == null || (approveLevel != 1 && approveLevel != 2))
        {
            throw new ServiceException("审批层级不合法");
        }

        SysTransferApply query = new SysTransferApply();
        if (approveLevel == 1)
        {
            if (!SecurityUtils.isAdmin() && !hasClassTeacherRole())
            {
                return Collections.emptyList();
            }
            query.setStatus("0");
            if (!SecurityUtils.isAdmin())
            {
                query.getParams().put("approveBy", SecurityUtils.getUsername());
                query.getParams().put("approveNickName", getCurrentNickName());
            }
        }
        else
        {
            if (!SecurityUtils.isAdmin() && !hasAcademicOfficeRole())
            {
                return Collections.emptyList();
            }
            query.setStatus("1");
        }
        return transferApplyMapper.selectMyApproveList(query);
    }

    /**
     * 新增转班申请
     *
     * @param applyDto 申请信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTransferApply(SysTransferApplyDto applyDto)
    {
        SysStudent student = studentMapper.selectStudentByStudentId(applyDto.getStudentId());
        if (student == null)
        {
            throw new ServiceException("学生不存在");
        }

        SysClass targetClass = classMapper.selectClassByClassId(applyDto.getAfterClassId());
        if (targetClass == null)
        {
            throw new ServiceException("目标班级不存在");
        }

        SysClass sourceClass = classMapper.selectClassByClassId(student.getClassId());
        if (sourceClass == null)
        {
            throw new ServiceException("原班级不存在");
        }
        if (sourceClass.getClassId().equals(targetClass.getClassId()))
        {
            throw new ServiceException("目标班级不能与原班级相同");
        }

        SysTransferApply existQuery = new SysTransferApply();
        existQuery.setStudentId(applyDto.getStudentId());
        existQuery.setStatus("0");
        List<SysTransferApply> existList = transferApplyMapper.selectTransferApplyList(existQuery);
        if (StringUtils.isNotNull(existList) && !existList.isEmpty())
        {
            throw new ServiceException("该学生已有待审批的转班申请");
        }

        existQuery.setStatus("1");
        existList = transferApplyMapper.selectTransferApplyList(existQuery);
        if (StringUtils.isNotNull(existList) && !existList.isEmpty())
        {
            throw new ServiceException("该学生已有待审批的转班申请");
        }

        SysTransferApply transferApply = new SysTransferApply();
        transferApply.setStudentId(student.getStudentId());
        transferApply.setStudentName(student.getStudentName());
        transferApply.setBeforeClassId(sourceClass.getClassId());
        transferApply.setBeforeClassName(sourceClass.getClassName());
        transferApply.setBeforeGrade(sourceClass.getGrade());
        transferApply.setAfterClassId(targetClass.getClassId());
        transferApply.setAfterClassName(targetClass.getClassName());
        transferApply.setAfterGrade(targetClass.getGrade());
        transferApply.setApplyReason(applyDto.getApplyReason());
        transferApply.setApplyBy(applyDto.getApplyBy());
        transferApply.setApplyTime(new Date());
        transferApply.setStatus("0");
        transferApply.setCreateBy(applyDto.getApplyBy());

        return transferApplyMapper.insertTransferApply(transferApply);
    }

    /**
     * 审批转班申请
     *
     * @param applyId 申请ID
     * @param approveResult 审批结果（0通过 1拒绝）
     * @param approveRemark 审批意见
     * @param approveBy 审批人
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveTransferApply(Long applyId, String approveResult, String approveRemark, String approveBy)
    {
        SysTransferApply transferApply = transferApplyMapper.selectTransferApplyByApplyId(applyId);
        if (transferApply == null)
        {
            throw new ServiceException("申请不存在");
        }

        String currentStatus = transferApply.getStatus();
        if ("2".equals(currentStatus) || "3".equals(currentStatus) || "4".equals(currentStatus))
        {
            throw new ServiceException("该申请流程已结束，不能重复审批");
        }

        Integer approveLevel = null;
        if ("0".equals(currentStatus))
        {
            approveLevel = 1;
        }
        else if ("1".equals(currentStatus))
        {
            approveLevel = 2;
        }
        if (approveLevel == null)
        {
            throw new ServiceException("申请状态不合法，无法审批");
        }
        validateApprovePermission(transferApply, approveLevel, approveBy);

        SysTransferApprove approve = new SysTransferApprove();
        approve.setApplyId(applyId);
        approve.setApproveLevel(approveLevel);
        approve.setApproveBy(approveBy);
        approve.setApproveTime(new Date());
        approve.setApproveResult(approveResult);
        approve.setApproveRemark(approveRemark);
        approve.setCreateBy(approveBy);
        transferApproveMapper.insertTransferApprove(approve);

        SysTransferApply updateApply = new SysTransferApply();
        updateApply.setApplyId(applyId);
        updateApply.setUpdateBy(approveBy);

        if ("0".equals(approveResult))
        {
            if (approveLevel == 1)
            {
                updateApply.setStatus("1");
            }
            else
            {
                updateApply.setStatus("2");
                executeTransfer(transferApply, approveBy);
            }
        }
        else
        {
            updateApply.setStatus("3");
            updateApply.setRejectReason(approveRemark);
        }

        return transferApplyMapper.updateTransferApply(updateApply);
    }

    /**
     * 撤回转班申请
     *
     * @param applyId 申请ID
     * @param cancelBy 撤回人
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelTransferApply(Long applyId, String cancelBy)
    {
        SysTransferApply transferApply = transferApplyMapper.selectTransferApplyByApplyId(applyId);
        validateCancelPermission(transferApply);
        validateCancelStatus(transferApply);

        SysTransferApply updateApply = new SysTransferApply();
        updateApply.setApplyId(applyId);
        updateApply.setStatus("4");
        updateApply.setUpdateBy(cancelBy);
        return transferApplyMapper.updateTransferApply(updateApply);
    }

    /**
     * 执行转班操作
     *
     * @param transferApply 申请信息
     * @param operName 操作人
     */
    private void executeTransfer(SysTransferApply transferApply, String operName)
    {
        SysStudentTransferDto transferDto = new SysStudentTransferDto();
        transferDto.setStudentIds(new Long[]{transferApply.getStudentId()});
        transferDto.setClassId(transferApply.getAfterClassId());
        transferDto.setRemark("转班审批通过，执行转班操作");
        transferDto.setUpdateBy(operName);
        studentService.transferStudentClass(transferDto);
    }

    /**
     * 删除转班申请
     *
     * @param applyId 申请ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTransferApplyByApplyId(Long applyId)
    {
        SysTransferApply transferApply = transferApplyMapper.selectTransferApplyByApplyId(applyId);
        validateDeletePermission(transferApply);
        validateDeleteStatus(transferApply);
        transferApproveMapper.deleteTransferApproveByApplyId(applyId);
        return transferApplyMapper.deleteTransferApplyByApplyId(applyId);
    }

    /**
     * 批量删除转班申请
     *
     * @param applyIds 需要删除的申请ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTransferApplyByApplyIds(Long[] applyIds)
    {
        for (Long applyId : applyIds)
        {
            SysTransferApply transferApply = transferApplyMapper.selectTransferApplyByApplyId(applyId);
            validateDeletePermission(transferApply);
            validateDeleteStatus(transferApply);
            transferApproveMapper.deleteTransferApproveByApplyId(applyId);
        }
        return transferApplyMapper.deleteTransferApplyByApplyIds(applyIds);
    }

    /**
     * 统计待审批数量
     *
     * @param approveLevel 审批层级
     * @return 待审批数量
     */
    @Override
    public int countPendingByLevel(Integer approveLevel)
    {
        if (approveLevel == null || (approveLevel != 1 && approveLevel != 2))
        {
            throw new ServiceException("审批层级不合法");
        }

        if (SecurityUtils.isAdmin())
        {
            return transferApplyMapper.countPendingByLevel(approveLevel);
        }
        if (approveLevel == 2)
        {
            return hasAcademicOfficeRole() ? transferApplyMapper.countPendingByLevel(approveLevel) : 0;
        }
        return hasClassTeacherRole() ? selectMyApproveList(approveLevel, null).size() : 0;
    }

    private void appendDataScope(SysTransferApply transferApply)
    {
        if (canViewAllTransferApply())
        {
            return;
        }
        transferApply.getParams().put("currentUsername", SecurityUtils.getUsername());
        if (hasClassTeacherRole())
        {
            transferApply.getParams().put("approveBy", SecurityUtils.getUsername());
            transferApply.getParams().put("approveNickName", getCurrentNickName());
        }
    }

    private void validateViewPermission(SysTransferApply transferApply)
    {
        if (transferApply == null)
        {
            return;
        }
        if (canViewAllTransferApply())
        {
            return;
        }
        if (isApplyOwner(transferApply))
        {
            return;
        }
        if (hasClassTeacherRole() && isSourceClassTeacher(transferApply))
        {
            return;
        }
        throw new ServiceException("无权查看当前转班申请");
    }

    private void validateDeletePermission(SysTransferApply transferApply)
    {
        if (transferApply == null)
        {
            throw new ServiceException("转班申请不存在");
        }
        if (canViewAllTransferApply() || isApplyOwner(transferApply))
        {
            return;
        }
        throw new ServiceException("只有申请人或教务处可以删除当前申请");
    }

    /**
     * 校验当前用户是否有权限撤回申请。
     *
     * @param transferApply 转班申请
     */
    private void validateCancelPermission(SysTransferApply transferApply)
    {
        if (transferApply == null)
        {
            throw new ServiceException("转班申请不存在");
        }
        if (SecurityUtils.isAdmin() || isApplyOwner(transferApply))
        {
            return;
        }
        throw new ServiceException("只有申请人本人可以撤回当前申请");
    }

    private void validateApprovePermission(SysTransferApply transferApply, Integer approveLevel, String approveBy)
    {
        if (SecurityUtils.isAdmin())
        {
            return;
        }
        if (approveLevel == 2)
        {
            if (!hasAcademicOfficeRole())
            {
                throw new ServiceException("只有教务处角色可以执行二级审批");
            }
            return;
        }
        if (!hasClassTeacherRole())
        {
            throw new ServiceException("只有班主任角色可以执行一级审批");
        }

        SysClass sourceClass = classMapper.selectClassByClassId(transferApply.getBeforeClassId());
        if (sourceClass == null)
        {
            throw new ServiceException("原班级不存在，无法校验审批权限");
        }
        if (!matchesTeacher(sourceClass.getTeacherName(), approveBy, getCurrentNickName()))
        {
            throw new ServiceException("只有该班班主任才能审批当前申请");
        }
    }

    private void validateDeleteStatus(SysTransferApply transferApply)
    {
        if (transferApply == null)
        {
            throw new ServiceException("转班申请不存在");
        }
        if (!"0".equals(transferApply.getStatus()) && !"3".equals(transferApply.getStatus()) && !"4".equals(transferApply.getStatus()))
        {
            throw new ServiceException("只有待班主任审批、已拒绝或已撤回的申请才允许删除");
        }
    }

    /**
     * 校验当前申请状态是否允许撤回。
     *
     * @param transferApply 转班申请
     */
    private void validateCancelStatus(SysTransferApply transferApply)
    {
        if (transferApply == null)
        {
            throw new ServiceException("转班申请不存在");
        }
        // 只有流程尚未结束的申请可以撤回；已通过、已拒绝、已撤回都不能再次撤回。
        if (!"0".equals(transferApply.getStatus()) && !"1".equals(transferApply.getStatus()))
        {
            throw new ServiceException("只有待审批的申请才允许撤回");
        }
    }

    private boolean canViewAllTransferApply()
    {
        return SecurityUtils.isAdmin() || hasAcademicOfficeRole();
    }

    private boolean hasClassTeacherRole()
    {
        return SecurityUtils.hasRole(ROLE_CLASS_TEACHER);
    }

    private boolean hasAcademicOfficeRole()
    {
        return SecurityUtils.hasRole(ROLE_ACADEMIC_OFFICE);
    }

    private boolean isApplyOwner(SysTransferApply transferApply)
    {
        return StringUtils.isNotEmpty(transferApply.getApplyBy())
                && transferApply.getApplyBy().equals(SecurityUtils.getUsername());
    }

    private boolean isSourceClassTeacher(SysTransferApply transferApply)
    {
        SysClass sourceClass = classMapper.selectClassByClassId(transferApply.getBeforeClassId());
        if (sourceClass == null)
        {
            return false;
        }
        return matchesTeacher(sourceClass.getTeacherName(), SecurityUtils.getUsername(), getCurrentNickName());
    }

    private String getCurrentNickName()
    {
        return SecurityUtils.getLoginUser().getUser().getNickName();
    }

    private boolean matchesTeacher(String teacherName, String... candidates)
    {
        if (StringUtils.isEmpty(teacherName))
        {
            return false;
        }
        for (String candidate : candidates)
        {
            if (StringUtils.isNotEmpty(candidate) && teacherName.equals(candidate))
            {
                return true;
            }
        }
        return false;
    }
}
