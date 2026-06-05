package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysTransferApply;

/**
 * 转班申请Mapper接口
 *
 * @author ruoyi
 */
public interface SysTransferApplyMapper
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
     * @param transferApply 查询参数（包含approveLevel和approveBy）
     * @return 转班申请集合
     */
    public List<SysTransferApply> selectMyApproveList(SysTransferApply transferApply);

    /**
     * 新增转班申请
     *
     * @param transferApply 转班申请
     * @return 结果
     */
    public int insertTransferApply(SysTransferApply transferApply);

    /**
     * 修改转班申请
     *
     * @param transferApply 转班申请
     * @return 结果
     */
    public int updateTransferApply(SysTransferApply transferApply);

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
