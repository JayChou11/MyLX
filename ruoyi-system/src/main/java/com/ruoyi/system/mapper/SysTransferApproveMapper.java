package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysTransferApprove;

/**
 * 审批记录Mapper接口
 *
 * @author ruoyi
 */
public interface SysTransferApproveMapper
{
    /**
     * 查询审批记录
     *
     * @param approveId 审批ID
     * @return 审批记录
     */
    public SysTransferApprove selectTransferApproveByApproveId(Long approveId);

    /**
     * 根据申请ID查询审批记录列表
     *
     * @param applyId 申请ID
     * @return 审批记录集合
     */
    public List<SysTransferApprove> selectTransferApproveByApplyId(Long applyId);

    /**
     * 新增审批记录
     *
     * @param transferApprove 审批记录
     * @return 结果
     */
    public int insertTransferApprove(SysTransferApprove transferApprove);

    /**
     * 删除审批记录
     *
     * @param approveId 审批ID
     * @return 结果
     */
    public int deleteTransferApproveByApproveId(Long approveId);

    /**
     * 批量删除审批记录
     *
     * @param approveIds 需要删除的审批ID
     * @return 结果
     */
    public int deleteTransferApproveByApproveIds(Long[] approveIds);

    /**
     * 根据申请ID删除审批记录
     *
     * @param applyId 申请ID
     * @return 结果
     */
    public int deleteTransferApproveByApplyId(Long applyId);
}
