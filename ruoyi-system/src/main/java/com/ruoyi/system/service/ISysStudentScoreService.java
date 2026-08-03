package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysStudentScore;
import com.ruoyi.system.domain.vo.SysStudentScoreClassStat;
import com.ruoyi.system.domain.vo.SysStudentScoreTrendVo;
import com.ruoyi.system.domain.vo.SysStudentScoreWarningVo;

/**
 * 学生成绩Service接口
 *
 * 接口只定义“对外能提供哪些能力”，不关心具体怎么实现。
 * Controller 依赖这个接口，而不是直接依赖实现类，这样符合 Spring 的面向接口编程习惯。
 *
 * @author ruoyi
 */
public interface ISysStudentScoreService
{
    /** 根据成绩ID查询单条详情 */
    public SysStudentScore selectStudentScoreByScoreId(Long scoreId);

    /** 按条件查询成绩列表，列表页和普通导出都会用到 */
    public List<SysStudentScore> selectStudentScoreList(SysStudentScore studentScore);

    /** 根据多个ID查询成绩，选择导出时使用 */
    public List<SysStudentScore> selectStudentScoreListByIds(Long[] scoreIds);

    /** 按班级和考试名称统计成绩，返回统计 VO */
    public List<SysStudentScoreClassStat> selectStudentScoreClassStatList(SysStudentScore studentScore);

    /** 查询某个学生的成绩趋势和每次考试排名 */
    public List<SysStudentScoreTrendVo> selectStudentScoreTrendList(Long studentId);

    /** 鏌ヨ瀛︾敓鎴愮哗棰勮鍒楄〃 */
    public List<SysStudentScoreWarningVo> selectStudentScoreWarningList(SysStudentScore studentScore);

    /** 新增成绩，内部会校验学生是否存在、成绩是否重复，并自动计算总分/平均分 */
    public int insertStudentScore(SysStudentScore studentScore);

    /** 修改成绩，内部同样会重新校验并重新计算总分/平均分 */
    public int updateStudentScore(SysStudentScore studentScore);

    /** 批量删除成绩 */
    public int deleteStudentScoreByScoreIds(Long[] scoreIds);

    /** 删除单条成绩 */
    public int deleteStudentScoreByScoreId(Long scoreId);

    /** 校验同一学生同一次考试是否唯一 */
    public boolean checkStudentScoreUnique(SysStudentScore studentScore);

    /** Excel 导入成绩，支持新增和覆盖更新两种模式 */
    public String importStudentScore(List<SysStudentScore> scoreList, Boolean isUpdateSupport, String operName);
}
