package com.ruoyi.system.domain.vo;

import java.util.List;

/**
 * 学生完整档案
 *
 * @author ruoyi
 */
public class SysStudentProfileVo
{
    private Long studentId;

    private String studentNo;

    private String studentName;

    private String idCard;

    private Integer age;

    private String gender;

    private String photo;

    private String remark;

    private Integer transferCount;

    private SysStudentProfileClassVo currentClassInfo;

    private List<SysStudentProfileTransferApplyVo> recentTransferApplies;

    private List<SysStudentProfileTransferLogVo> recentTransferLogs;

    private List<SysStudentProfileTimelineVo> timelines;

    public Long getStudentId()
    {
        return studentId;
    }

    public void setStudentId(Long studentId)
    {
        this.studentId = studentId;
    }

    public String getStudentNo()
    {
        return studentNo;
    }

    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
    }

    public String getStudentName()
    {
        return studentName;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public Integer getAge()
    {
        return age;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getPhoto()
    {
        return photo;
    }

    public void setPhoto(String photo)
    {
        this.photo = photo;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Integer getTransferCount()
    {
        return transferCount;
    }

    public void setTransferCount(Integer transferCount)
    {
        this.transferCount = transferCount;
    }

    public SysStudentProfileClassVo getCurrentClassInfo()
    {
        return currentClassInfo;
    }

    public void setCurrentClassInfo(SysStudentProfileClassVo currentClassInfo)
    {
        this.currentClassInfo = currentClassInfo;
    }

    public List<SysStudentProfileTransferApplyVo> getRecentTransferApplies()
    {
        return recentTransferApplies;
    }

    public void setRecentTransferApplies(List<SysStudentProfileTransferApplyVo> recentTransferApplies)
    {
        this.recentTransferApplies = recentTransferApplies;
    }

    public List<SysStudentProfileTransferLogVo> getRecentTransferLogs()
    {
        return recentTransferLogs;
    }

    public void setRecentTransferLogs(List<SysStudentProfileTransferLogVo> recentTransferLogs)
    {
        this.recentTransferLogs = recentTransferLogs;
    }

    public List<SysStudentProfileTimelineVo> getTimelines()
    {
        return timelines;
    }

    public void setTimelines(List<SysStudentProfileTimelineVo> timelines)
    {
        this.timelines = timelines;
    }
}
