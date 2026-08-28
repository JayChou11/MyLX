package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 成绩预警记录对象 sys_score_warning_record
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
public class SysWarningRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预警ID */
    private Long warningId;

    /** 成绩ID */
    private Long scoreId;

    /** 学生ID */
    private Long studentId;

    /** 考试ID */
    private Long examId;

    /** 预警类型 */
    @Excel(name = "预警类型", readConverterExp = "average_low=低于平均分,subject_low=单科不及格,rank_drop=排名下降")
    private String warningType;

    /** 预警等级：1低 2中 3高 */
    @Excel(name = "预警等级", readConverterExp = "1=低,2=中,3=高")
    private String warningLevel;

    /** 预警项目 */
    @Excel(name = "预警项目")
    private String warningItem;

    /** 预警原因 */
    @Excel(name = "预警原因")
    private String warningReason;

    /** 预警状态：0待处理 1已处理 2已忽略 */
    @Excel(name = "预警状态", readConverterExp = "0=待处理,1=已处理,2=已忽略")
    private String warningStatus;

    /** 处理结果 */
    @Excel(name = "处理结果")
    private String handleResult;

    /** 处理时间 */
    @Excel(name = "处理时间")
    private Date handleTime;

    /** 处理人ID */
    private Long handlerId;

    /** 处理人 */
    @Excel(name = "处理人")
    private String handlerName;

    public Long getWarningId() {
        return warningId;
    }

    public void setWarningId(Long warningId) {
        this.warningId = warningId;
    }

    public Long getScoreId() {
        return scoreId;
    }

    public void setScoreId(Long scoreId) {
        this.scoreId = scoreId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getWarningType() {
        return warningType;
    }

    public void setWarningType(String warningType) {
        this.warningType = warningType;
    }

    public String getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel) {
        this.warningLevel = warningLevel;
    }

    public String getWarningItem() {
        return warningItem;
    }

    public void setWarningItem(String warningItem) {
        this.warningItem = warningItem;
    }

    public String getWarningReason() {
        return warningReason;
    }

    public void setWarningReason(String warningReason) {
        this.warningReason = warningReason;
    }

    public String getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(String warningStatus) {
        this.warningStatus = warningStatus;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    @Override
    public String toString() {
        return "SysWarningRecord{" +
                "warningId=" + warningId +
                ", scoreId=" + scoreId +
                ", studentId=" + studentId +
                ", examId=" + examId +
                ", warningType='" + warningType + '\'' +
                ", warningLevel='" + warningLevel + '\'' +
                ", warningItem='" + warningItem + '\'' +
                ", warningReason='" + warningReason + '\'' +
                ", warningStatus='" + warningStatus + '\'' +
                ", handleResult='" + handleResult + '\'' +
                ", handleTime=" + handleTime +
                ", handlerId=" + handlerId +
                ", handlerName='" + handlerName + '\'' +
                '}';
    }
}
