package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.system.domain.SysStudent;
import com.ruoyi.system.domain.SysStudentScore;
import com.ruoyi.system.domain.vo.SysStudentScoreClassStat;
import com.ruoyi.system.mapper.SysStudentMapper;
import com.ruoyi.system.mapper.SysStudentScoreMapper;
import com.ruoyi.system.service.ISysStudentScoreService;

/**
 * 学生成绩Service业务层处理
 *
 * 这一层是成绩模块最重要的业务层，Controller 只负责接收请求，
 * Mapper 只负责执行 SQL，真正的规则都放在这里：
 * 1. 学生必须存在；
 * 2. 同一个学生同一次考试只能有一条成绩；
 * 3. 三科成绩校验通过后，由后端统一计算总分和平均分；
 * 4. 导入时支持“新增”或“覆盖更新”两种模式。
 *
 * @author ruoyi
 */
@Service
public class SysStudentScoreServiceImpl implements ISysStudentScoreService
{
    private static final Logger log = LoggerFactory.getLogger(SysStudentScoreServiceImpl.class);

    /**
     * 平均分 = 总分 / 3。
     * 使用 BigDecimal 而不是 double，是为了避免小数精度问题。
     */
    private static final BigDecimal THREE = new BigDecimal("3");

    @Autowired
    private SysStudentScoreMapper studentScoreMapper;

    @Autowired
    private SysStudentMapper studentMapper;

    @Autowired
    protected Validator validator;

    @Override
    public SysStudentScore selectStudentScoreByScoreId(Long scoreId)
    {
        return studentScoreMapper.selectStudentScoreByScoreId(scoreId);
    }

    @Override
    public List<SysStudentScore> selectStudentScoreList(SysStudentScore studentScore)
    {
        return studentScoreMapper.selectStudentScoreList(studentScore);
    }

    @Override
    public List<SysStudentScore> selectStudentScoreListByIds(Long[] scoreIds)
    {
        return studentScoreMapper.selectStudentScoreListByIds(scoreIds);
    }

    @Override
    public List<SysStudentScoreClassStat> selectStudentScoreClassStatList(SysStudentScore studentScore)
    {
        // 统计逻辑直接由 SQL 聚合完成，Service 不再循环计算，避免把数据库擅长的事情搬到 Java 里。
        return studentScoreMapper.selectStudentScoreClassStatList(studentScore);
    }

    @Override
    public int insertStudentScore(SysStudentScore studentScore)
    {
        // 新增时先确认学生存在，否则可能插入一条挂不到学生档案上的“脏成绩”。
        checkStudentExists(studentScore.getStudentId());
        // 再校验唯一性，避免同一学生同一场考试录入多条成绩。
        checkScoreUniqueOrThrow(studentScore);
        // 总分、平均分永远由后端计算，不信任前端传来的 totalScore/averageScore。
        fillTotalAndAverageScore(studentScore);
        return studentScoreMapper.insertStudentScore(studentScore);
    }

    @Override
    public int updateStudentScore(SysStudentScore studentScore)
    {
        // 修改时也允许换学生或考试名称，所以仍然要重新校验学生存在和唯一性。
        checkStudentExists(studentScore.getStudentId());
        checkScoreUniqueOrThrow(studentScore);
        fillTotalAndAverageScore(studentScore);
        return studentScoreMapper.updateStudentScore(studentScore);
    }

    @Override
    public int deleteStudentScoreByScoreIds(Long[] scoreIds)
    {
        return studentScoreMapper.deleteStudentScoreByScoreIds(scoreIds);
    }

    @Override
    public int deleteStudentScoreByScoreId(Long scoreId)
    {
        return studentScoreMapper.deleteStudentScoreByScoreId(scoreId);
    }

    @Override
    public boolean checkStudentScoreUnique(SysStudentScore studentScore)
    {
        // 新增时 scoreId 为空，用 -1 表示“当前不存在自己的记录”；
        // 修改时 scoreId 不为空，如果查出来的重复记录就是自己，则仍然算唯一。
        Long scoreId = StringUtils.isNull(studentScore.getScoreId()) ? -1L : studentScore.getScoreId();
        SysStudentScore info = studentScoreMapper.selectStudentScoreByStudentAndExam(studentScore.getStudentId(),
                studentScore.getExamName());
        return StringUtils.isNull(info) || info.getScoreId().longValue() == scoreId.longValue();
    }

    /**
     * 导入学生成绩数据
     *
     * @param scoreList 成绩列表
     * @param isUpdateSupport 是否更新已经存在的数据
     * @param operName 操作人
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importStudentScore(List<SysStudentScore> scoreList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(scoreList) || scoreList.size() == 0)
        {
            throw new ServiceException("导入成绩数据不能为空");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysStudentScore score : scoreList)
        {
            try
            {
                // Excel 模板里让用户填写“学号”，而不是 studentId。
                // 这样用户更容易理解，后端再根据学号找到真正的 student_id。
                SysStudent student = resolveImportStudent(score);
                score.setStudentId(student.getStudentId());
                // 手动触发实体上的注解校验，例如考试名称不能为空、分数必须在 0-100。
                BeanValidators.validateWithException(validator, score);
                // 即使 Excel 里有总分/平均分列，也以这里重新计算的结果为准。
                fillTotalAndAverageScore(score);
                SysStudentScore existScore = studentScoreMapper.selectStudentScoreByStudentAndExam(score.getStudentId(),
                        score.getExamName());
                if (StringUtils.isNull(existScore))
                {
                    // 不存在：走新增逻辑。
                    score.setCreateBy(operName);
                    studentScoreMapper.insertStudentScore(score);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、学号 ")
                        .append(score.getStudentNo()).append(" 的 ").append(score.getExamName()).append(" 导入成功");
                }
                else if (isUpdateSupport)
                {
                    // 已存在并允许覆盖：把已存在记录的主键设置回对象，再执行 update。
                    score.setScoreId(existScore.getScoreId());
                    score.setUpdateBy(operName);
                    studentScoreMapper.updateStudentScore(score);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、学号 ")
                        .append(score.getStudentNo()).append(" 的 ").append(score.getExamName()).append(" 更新成功");
                }
                else
                {
                    // 已存在但不允许覆盖：记录失败原因，最后统一抛出给前端展示。
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、学号 ")
                        .append(score.getStudentNo()).append(" 的 ").append(score.getExamName()).append(" 已存在");
                }
            }
            catch (Exception e)
            {
                // 单行失败不立刻终止，继续处理后面的行，最后把所有失败信息一次性返回。
                failureNum++;
                String msg = "<br/>" + failureNum + "、学号 " + score.getStudentNo() + " 导入失败：";
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

    private void checkStudentExists(Long studentId)
    {
        if (StringUtils.isNull(studentId))
        {
            throw new ServiceException("请选择学生");
        }
        // 这里复用学生模块已有 Mapper，不重新写一条只查学生是否存在的 SQL。
        if (studentMapper.selectStudentByStudentId(studentId) == null)
        {
            throw new ServiceException("学生不存在");
        }
    }

    private void checkScoreUniqueOrThrow(SysStudentScore studentScore)
    {
        // 用一个小方法包住唯一性校验，是为了新增、修改时都能复用同一句错误提示。
        if (!checkStudentScoreUnique(studentScore))
        {
            throw new ServiceException("该学生的本次考试成绩已存在");
        }
    }

    private SysStudent resolveImportStudent(SysStudentScore score)
    {
        if (StringUtils.isEmpty(score.getStudentNo()))
        {
            throw new ServiceException("学号不能为空");
        }
        // 导入文件只填学号，后端根据学号找到学生，从而拿到 studentId。
        // 如果学号写错，说明这条成绩无法关联到学生，必须判为导入失败。
        SysStudent student = studentMapper.selectStudentByStudentNo(score.getStudentNo());
        if (student == null)
        {
            throw new ServiceException("学号不存在");
        }
        return student;
    }

    /**
     * 总分和平均分由后端统一计算，避免前端传错或导入文件里手填错。
     */
    private void fillTotalAndAverageScore(SysStudentScore studentScore)
    {
        // BigDecimal.add 返回新对象，不会修改原对象，所以这里要链式相加后赋给 totalScore。
        BigDecimal totalScore = studentScore.getChineseScore()
            .add(studentScore.getMathScore())
            .add(studentScore.getEnglishScore())
            .setScale(2, RoundingMode.HALF_UP);
        studentScore.setTotalScore(totalScore);
        // 平均分保留两位小数，HALF_UP 表示四舍五入。
        studentScore.setAverageScore(totalScore.divide(THREE, 2, RoundingMode.HALF_UP));
    }
}
