package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysTransferApply;
import com.ruoyi.system.domain.vo.SysTransferApplyDto;

/**
 * 转班申请Service接口
 *
 * @author ruoyi
 */
public interface ISysTransferApplyService
{
    /**
     * 查询转班申请
     *
     * @param applyId 申请ID
     * @return 转班申请
     */
    public SysTransferApply selectTransferApplyByApplyId(Long applyId);

    /**
     * 查询转班申请列表
     *
     * @param transferApply 转班申请
     * @return 转班申请集合
     */
    public List<SysTransferApply> selectTransferApplyList(SysTransferApply transferApply);

    /**
     * 查询待我审批的申请列表
     *
     * @param approveLevel 审批层级
     * @param status 申请状态
     * @return 转班申请集合
     */
    public List<SysTransferApply> selectMyApproveList(Integer approveLevel, String status);

    /**
     * 新增转班申请
     *
     * @param applyDto 申请信息
     * @return 结果
     */
    public int insertTransferApply(SysTransferApplyDto applyDto);

    /**
     * 审批转班申请
     *
     * @param applyId 申请ID
     * @param approveResult 审批结果（0通过 1拒绝）
     * @param approveRemark 审批意见
     * @param approveBy 审批人
     * @return 结果
     */
    public int approveTransferApply(Long applyId, String approveResult, String approveRemark, String approveBy);

    /**
     * 删除转班申请
     *
     * @param applyId 申请ID
     * @return 结果
     */
    public int deleteTransferApplyByApplyId(Long applyId);

    /**
     * 批量删除转班申请
     *
     * @param applyIds 需要删除的申请ID
     * @return 结果
     */
    public int deleteTransferApplyByApplyIds(Long[] applyIds);

    /**
     * 统计待审批数量
     *
     * @param approveLevel 审批层级
     * @return 待审批数量
     */
    public int countPendingByLevel(Integer approveLevel);
}
