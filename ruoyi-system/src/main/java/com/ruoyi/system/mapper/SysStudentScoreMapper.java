package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysStudentScore;
import com.ruoyi.system.domain.vo.SysStudentScoreClassStat;

/**
 * 学生成绩Mapper接口
 *
 * Mapper 接口只声明“能做哪些数据库操作”，真正 SQL 写在
 * resources/mapper/system/SysStudentScoreMapper.xml 中。
 *
 * 方法名要和 XML 中的 id 保持一致，例如：
 * selectStudentScoreList(...) 对应 <select id="selectStudentScoreList">。
 *
 * @author ruoyi
 */
public interface SysStudentScoreMapper
{
    /**
     * 查询学生成绩
     *
     * @param scoreId 成绩ID
     * @return 学生成绩
     */
    public SysStudentScore selectStudentScoreByScoreId(Long scoreId);

    /**
     * 查询学生成绩列表
     *
     * 这个查询会关联 sys_student 和 sys_class，所以返回对象中不仅有成绩字段，
     * 还有学号、姓名、年级、班级等展示字段。
     *
     * @param studentScore 学生成绩
     * @return 学生成绩集合
     */
    public List<SysStudentScore> selectStudentScoreList(SysStudentScore studentScore);

    /**
     * 根据ID集合查询成绩列表
     *
     * 用于“选择导出”：前端传入多个 scoreId，XML 里用 foreach 拼成 in (...)。
     *
     * @param scoreIds 成绩ID集合
     * @return 学生成绩集合
     */
    public List<SysStudentScore> selectStudentScoreListByIds(Long[] scoreIds);

    /**
     * 查询班级成绩统计
     *
     * 返回的是 VO，不是实体表对象。
     * 因为统计结果里有 avgScore、maxTotalScore、passCount 这类聚合字段，
     * 它们不是 sys_student_score 表中的单行数据。
     *
     * @param studentScore 查询条件
     * @return 班级成绩统计集合
     */
    public List<SysStudentScoreClassStat> selectStudentScoreClassStatList(SysStudentScore studentScore);

    /**
     * 根据学生和考试名称查询成绩
     *
     * 这里有两个普通参数，所以要用 @Param 起名字。
     * 如果不写 @Param，XML 中的 #{studentId}、#{examName} 可能找不到对应参数名。
     *
     * @param studentId 学生ID
     * @param examName 考试名称
     * @return 学生成绩
     */
    public SysStudentScore selectStudentScoreByStudentAndExam(@Param("studentId") Long studentId,
            @Param("examName") String examName);

    /**
     * 新增学生成绩
     *
     * @param studentScore 学生成绩
     * @return 结果
     */
    public int insertStudentScore(SysStudentScore studentScore);

    /**
     * 修改学生成绩
     *
     * @param studentScore 学生成绩
     * @return 结果
     */
    public int updateStudentScore(SysStudentScore studentScore);

    /**
     * 删除学生成绩
     *
     * @param scoreId 成绩ID
     * @return 结果
     */
    public int deleteStudentScoreByScoreId(Long scoreId);

    /**
     * 批量删除学生成绩
     *
     * @param scoreIds 成绩ID集合
     * @return 结果
     */
    public int deleteStudentScoreByScoreIds(Long[] scoreIds);
}
