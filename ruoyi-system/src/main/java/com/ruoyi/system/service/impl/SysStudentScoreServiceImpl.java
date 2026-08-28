package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.*;
import com.ruoyi.system.mapper.*;
import com.ruoyi.system.service.ISysClassService;
import com.ruoyi.system.service.ISysWarningRecordService;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.system.domain.vo.SysStudentScoreClassStat;
import com.ruoyi.system.domain.vo.SysStudentScoreTrendVo;
import com.ruoyi.system.domain.vo.SysStudentScoreWarningVo;
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

    /** 单科及格线 */
    private static final BigDecimal PASS_LINE = new BigDecimal("60");

    /** 班均预警线-中：低于班均 20 分时触发 */
    private static final BigDecimal WARNING_GAP = new BigDecimal("20");

    /** 班均预警线-高：低于班均 40 分时触发 */
    private static final BigDecimal WARNING_GAP_HIGH = new BigDecimal("40");

    @Autowired
    private SysStudentScoreMapper studentScoreMapper;

    @Autowired
    private SysStudentMapper studentMapper;

    @Autowired
    private SysExamMapper examMapper;

    @Autowired
    private SysClassMapper classMapper;

    @Autowired
    private SysWarningRecordMapper warningRecordMapper;

    @Autowired
    private ISysWarningRecordService warningRecordService;

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
    public List<SysStudentScoreTrendVo> selectStudentScoreTrendList(Long studentId)
    {
        // 趋势分析必须依附于具体学生，所以先校验学生存在，再去查该学生的多次考试成绩。
        checkStudentExists(studentId);
        return studentScoreMapper.selectStudentScoreTrendList(studentId);
    }

    @Override
    public List<SysStudentScoreWarningVo> selectStudentScoreWarningList(SysStudentScore studentScore)
    {
        // 预警列表复用现有成绩列表查询，这样前端传进来的筛选条件仍然有效。
        List<SysStudentScore> scoreList = studentScoreMapper.selectStudentScoreList(studentScore);
        if (scoreList == null || scoreList.isEmpty())
        {
            return new ArrayList<>();
        }

        Map<String, SysStudentScoreClassStat> classStatCache = new HashMap<>();
        Map<Long, List<SysStudentScoreTrendVo>> trendCache = new HashMap<>();
        List<SysStudentScoreWarningVo> warningList = new ArrayList<>();

        for (SysStudentScore score : scoreList)
        {
            SysStudentScoreWarningVo warningVo = new SysStudentScoreWarningVo();
            BeanUtils.copyProperties(score, warningVo);

            List<String> warningTypes = new ArrayList<>();
            List<String> warningReasons = new ArrayList<>();

            addSubjectWarning(score, warningTypes, warningReasons);
            addClassAverageWarning(score, classStatCache, warningVo, warningTypes, warningReasons);
            addRankDropWarning(score, trendCache, warningVo, warningTypes, warningReasons);

            if (!warningTypes.isEmpty())
            {
                warningVo.setWarningTypes(String.join("、", warningTypes));
                warningVo.setWarningReason(String.join("；", warningReasons));
                warningList.add(warningVo);
            }
        }

        return warningList;
    }

    @Override
    @Transactional
    public int insertStudentScore(SysStudentScore studentScore)
    {
        // 新增时先确认学生存在，否则可能插入一条挂不到学生档案上的“脏成绩”。
        checkStudentExists(studentScore.getStudentId());
        checkExamExists(studentScore.getExamId());
        // 再校验唯一性，避免同一学生同一场考试录入多条成绩。
        checkScoreUniqueOrThrow(studentScore);
        // 总分、平均分永远由后端计算，不信任前端传来的 totalScore/averageScore。
        fillTotalAndAverageScore(studentScore);
        int rows = studentScoreMapper.insertStudentScore(studentScore);
        // 插入成功后生成告警
        if (rows > 0)
        {
            generateWarningRecords(studentScore);
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateStudentScore(SysStudentScore studentScore)
    {
        // 修改时也允许换学生或考试名称，所以仍然要重新校验学生存在和唯一性。
        checkStudentExists(studentScore.getStudentId());
        checkExamExists(studentScore.getExamId());
        checkScoreUniqueOrThrow(studentScore);
        fillTotalAndAverageScore(studentScore);
        int rows = studentScoreMapper.updateStudentScore(studentScore);
        // 修改成功后生成告警
        if (rows > 0)
        {
            generateWarningRecords(studentScore);
        }
        return rows;
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
                studentScore.getExamId());
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
                // 考试批次的唯一性校验
                SysExam exam = resolveImportExam(student, score);
                score.setExamId(exam.getExamId());
                score.setExamName(exam.getExamName());
                // 手动触发实体上的注解校验，例如考试名称不能为空、分数必须在 0-100。
                BeanValidators.validateWithException(validator, score);
                // 即使 Excel 里有总分/平均分列，也以这里重新计算的结果为准。
                fillTotalAndAverageScore(score);
                SysStudentScore existScore = studentScoreMapper.selectStudentScoreByStudentAndExam(score.getStudentId(),
                        score.getExamId());
                if (StringUtils.isNull(existScore))
                {
                    // 不存在：走新增逻辑。
                    score.setCreateBy(operName);
                    int rows = studentScoreMapper.insertStudentScore(score);
                    // 拆入成功后生成告警
                    if(rows > 0) {
                        generateWarningRecords(score);
                    }
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
                    generateWarningRecords(score);
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

    //  根据 examId 校验考试是否存在
    private void checkExamExists(Long examId)
    {
        if (StringUtils.isNull(examId))
        {
            throw new ServiceException("请选择考试");
        }
        // 这里复用学生模块已有 Mapper，不重新写一条只查学生是否存在的 SQL。
        if (examMapper.selectSysExamByExamId(examId) == null)
        {
            throw new ServiceException("考试不存在");
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

    // 校验考试批次
    private SysExam resolveImportExam(SysStudent student, SysStudentScore score)
    {
        if (StringUtils.isEmpty(score.getExamName()))
        {
            throw new ServiceException("考试名称不能为空");
        }

        SysClass studentClass = classMapper.selectClassByClassId(student.getClassId());
        if (studentClass == null)
        {
            throw new ServiceException("学生班级不存在");
        }

        List<SysExam> examList = examMapper.selectSysExamByNameAndGrade(score.getExamName(), studentClass.getGrade());

        if (examList == null || examList.isEmpty())
        {
            throw new ServiceException("考试批次不存在：" + score.getExamName() + "，年级：" + studentClass.getGrade());
        }

        if (examList.size() > 1)
        {
            throw new ServiceException("考试批次不唯一：" + score.getExamName() + "，年级：" + studentClass.getGrade() + "，请在考试名称中区分学期或年份");
        }

        return examList.get(0);
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

    /**
     * 根据一条成绩生成预警记录。
     * 总分 < 180：total_low，总分，总分低于180分
     * 语文 < 60：subject_low，语文，语文低于60分
     * 数学 < 60：subject_low，数学，数学低于60分
     * 英语 < 60：subject_low，英语，英语低于60分
     * 预警等级：
     * 总分 < 120：3 高
     * 总分 < 180：2 中
     * 单科 < 40：3 高
     * 单科 < 60：2 中
     *
     * 排名下降
     * 班级排名下降：rank_drop  预警项目：班级排名
     * 预警等级：
     * 下降5名以上：中
     * 下降10名以上：高
     * 年级排名下降：rank_drop  预警项目：年级排名
     * 预警等级：
     * 下降5名以上：中
     * 下降10名以上：高
     *
     * 低于班级平均分
     * 低于班级平均分：average_low  预警项目：班级平均分
     * 预警等级：
     * 低于班级平均分20分以上：中
     * 低于班级平均分40分以上：高
     */
    private void generateWarningRecords(SysStudentScore score)
    {
        BigDecimal passScore = BigDecimal.valueOf(60);
        BigDecimal lowScore = BigDecimal.valueOf(40);
        BigDecimal totalPassScore = BigDecimal.valueOf(180);
        BigDecimal totalLowScore = BigDecimal.valueOf(120);

        String warningType = "";
        String warningLevel = "";
        String warningItem = "";
        String warningReason = "";

        BigDecimal chineseScore = score.getChineseScore();
        BigDecimal mathScore = score.getMathScore();
        BigDecimal englishScore = score.getEnglishScore();
        BigDecimal totalScore = score.getTotalScore();

        // 单科预警
        String[] names = { "语文", "数学", "英语" };
        BigDecimal[] singleScores = { chineseScore, mathScore, englishScore };
        for (int i = 0; i < singleScores.length; i++) {
            if(isLessThan(singleScores[i], passScore)) {
                warningType = "subject_low";
                warningLevel = "2";
                warningItem = names[i];
                warningReason = warningItem + "成绩低于" + passScore + "分";

                if(isLessThan(singleScores[i], lowScore)) {
                    warningLevel = "3";
                    warningReason = warningItem + "成绩低于" + lowScore + "分";
                }

                SysWarningRecord warningRecord = buildWarningRecord(score, warningType, warningLevel, warningItem, warningReason);
                saveWarningIfAbsent(warningRecord);
            }
        }

        // 总分预警
        if(isLessThan(totalScore, totalPassScore)) {
            warningType = "total_low";
            warningLevel = "2";
            warningItem = "总分";
            warningReason = warningItem + "低于" + totalPassScore + "分";

            if(totalScore.compareTo(totalLowScore) < 0) {
                warningLevel = "3";
                warningReason = warningItem + "低于" + totalLowScore + "分";
            }

            SysWarningRecord warningRecord = buildWarningRecord(score, warningType, warningLevel, warningItem, warningReason);
            saveWarningIfAbsent(warningRecord);
        }

        // 这里要先去拿到年级，因为导入时没有学生的年级信息
        if (score.getStudentId() != null && score.getClassId() == null) {
            SysStudent student = studentMapper.selectStudentByStudentId(score.getStudentId());
            if (student != null) {
                score.setClassId(student.getClassId());
            }
        }

        if (score.getClassId() != null && StringUtils.isEmpty(score.getGrade())) {
            SysClass currentClass = classMapper.selectClassByClassId(score.getClassId());
            if (currentClass != null) {
                score.setGrade(currentClass.getGrade());
            }
        }

        // 班均预警
        addClassAverageWarningRecord(score);

        // 排名预警
        addRankDropWarningRecord(score);
    }

    private boolean isLessThan(BigDecimal value, BigDecimal threshold)
    {
        return value != null && threshold != null && value.compareTo(threshold) < 0;
    }

    private void saveWarningIfAbsent(SysWarningRecord record)
    {
        SysWarningRecord exists = warningRecordMapper.selectByScoreIdAndTypeAndItem(
                record.getScoreId(),
                record.getWarningType(),
                record.getWarningItem()
        );

        if (exists == null)
        {
            warningRecordService.insertSysWarningRecord(record);
        }
    }

    /**
     * 构建预警对象。
     */
    private SysWarningRecord buildWarningRecord(SysStudentScore score,
                                                String warningType,
                                                String warningLevel,
                                                String warningItem,
                                                String warningReason)
    {
        SysWarningRecord record = new SysWarningRecord();
        record.setScoreId(score.getScoreId());
        record.setStudentId(score.getStudentId());
        record.setExamId(score.getExamId());
        record.setWarningType(warningType);
        record.setWarningLevel(warningLevel);
        record.setWarningItem(warningItem);
        record.setWarningReason(warningReason);
        record.setWarningStatus("0");
        record.setCreateTime(DateUtils.getNowDate());
        return record;
    }

    /**
     * 单科预警：任意一科低于 60 分就触发。
     */
    private void addSubjectWarning(SysStudentScore score, List<String> warningTypes, List<String> warningReasons)
    {
        List<String> lowSubjects = new ArrayList<>();
        addIfBelowLine(lowSubjects, "语文", score.getChineseScore());
        addIfBelowLine(lowSubjects, "数学", score.getMathScore());
        addIfBelowLine(lowSubjects, "英语", score.getEnglishScore());
        if (!lowSubjects.isEmpty())
        {
            warningTypes.add("单科不及格");
            warningReasons.add(String.join("、", lowSubjects) + "低于60分");
        }
    }

    /**
     * 班均预警：当前学生总分比本班同场考试均分低 20 分以上就触发。
     */
    private void addClassAverageWarning(SysStudentScore score, Map<String, SysStudentScoreClassStat> classStatCache,
            SysStudentScoreWarningVo warningVo, List<String> warningTypes, List<String> warningReasons)
    {
        if (score.getClassId() == null || StringUtils.isEmpty(score.getExamName()) || score.getTotalScore() == null)
        {
            return;
        }

        String cacheKey = score.getClassId() + "_" + score.getExamName();
        SysStudentScoreClassStat classStat = classStatCache.get(cacheKey);
        if (classStat == null)
        {
            SysStudentScore query = new SysStudentScore();
            query.setClassId(score.getClassId());
            query.setExamId(score.getExamId());
            query.setGrade(score.getGrade());
            List<SysStudentScoreClassStat> classStats = studentScoreMapper.selectStudentScoreClassStatList(query);
            if (classStats != null && !classStats.isEmpty())
            {
                classStat = classStats.get(0);
                classStatCache.put(cacheKey, classStat);
            }
        }
        if (classStat == null || classStat.getAvgScore() == null)
        {
            return;
        }

        BigDecimal classAvgTotalScore = classStat.getAvgScore().multiply(THREE).setScale(2, RoundingMode.HALF_UP);
        warningVo.setClassAvgTotalScore(classAvgTotalScore);

        BigDecimal gap = classAvgTotalScore.subtract(score.getTotalScore()).setScale(2, RoundingMode.HALF_UP);
        if (gap.compareTo(WARNING_GAP) >= 0)
        {
            warningTypes.add("低于班均20分");
            warningReasons.add("总分比班级平均分低" + gap + "分");
        }
    }

    /**
     * 班均预警记录
     */
    private void addClassAverageWarningRecord(SysStudentScore score)
    {
        SysStudentScore query = new SysStudentScore();
        query.setExamId(score.getExamId());
        query.setExamName(score.getExamName());
        query.setGrade(score.getGrade());
        List<SysStudentScoreClassStat> classStats = studentScoreMapper.selectStudentScoreClassStatList(query);
        if (classStats != null && !classStats.isEmpty())
        {
            SysStudentScoreClassStat classStat = classStats.get(0);
            if (classStat == null)
            {
                return;
            }

            if (classStat.getAvgScore() == null)
            {
                return;
            }

            BigDecimal classAvgTotalScore = classStat.getAvgScore().multiply(THREE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal gap = classAvgTotalScore.subtract(score.getTotalScore()).setScale(2, RoundingMode.HALF_UP);
            if (gap.compareTo(WARNING_GAP) >= 0)
            {
                String warningType = "";
                String warningLevel = "";
                String warningItem = "";
                String warningReason = "";

                warningType = "average_low";
                warningLevel = "2";
                warningItem = "班级平均分";
                warningReason = "总分比班级平均分低" + WARNING_GAP + "分以上";

                if(gap.compareTo(WARNING_GAP_HIGH) >= 0) {
                    warningLevel = "3";
                    warningReason = "总分比班级平均分低" + WARNING_GAP_HIGH + "分以上";
                }

                SysWarningRecord warningRecord = buildWarningRecord(score, warningType, warningLevel, warningItem, warningReason);
                saveWarningIfAbsent(warningRecord);
            }
        }
    }

    /**
     * 排名下降预警：当前考试排名比上一次考试更靠后就触发。
     */
    private void addRankDropWarning(SysStudentScore score, Map<Long, List<SysStudentScoreTrendVo>> trendCache,
            SysStudentScoreWarningVo warningVo, List<String> warningTypes, List<String> warningReasons)
    {
        if (score.getStudentId() == null)
        {
            return;
        }

        List<SysStudentScoreTrendVo> trendList = trendCache.get(score.getStudentId());
        if (trendList == null)
        {
            trendList = studentScoreMapper.selectStudentScoreTrendList(score.getStudentId());
            trendCache.put(score.getStudentId(), trendList);
        }
        if (trendList == null || trendList.size() < 2)
        {
            return;
        }

        int index = findTrendIndex(trendList, score.getScoreId());
        if (index <= 0)
        {
            return;
        }

        SysStudentScoreTrendVo current = trendList.get(index);
        SysStudentScoreTrendVo previous = trendList.get(index - 1);
        StringBuilder reason = new StringBuilder();
        boolean dropped = false;

        if (isRankDropped(current.getClassRank(), previous.getClassRank()))
        {
            warningVo.setPreviousClassRank(previous.getClassRank());
            reason.append("班级排名从第").append(previous.getClassRank()).append("名下降到第")
                .append(current.getClassRank()).append("名");
            dropped = true;
        }

        if (isRankDropped(current.getGradeRank(), previous.getGradeRank()))
        {
            warningVo.setPreviousGradeRank(previous.getGradeRank());
            if (dropped)
            {
                reason.append("，");
            }
            reason.append("年级排名从第").append(previous.getGradeRank()).append("名下降到第")
                .append(current.getGradeRank()).append("名");
            dropped = true;
        }

        if (dropped)
        {
            warningTypes.add("排名下降");
            warningReasons.add(reason.toString());
        }
    }

    /**
     * 排名下降预警记录。
     */
    private void addRankDropWarningRecord(SysStudentScore score)
    {
        List<SysStudentScoreTrendVo> trendList = studentScoreMapper.selectStudentScoreTrendList(score.getStudentId());

        if (trendList == null || trendList.size() < 2)
        {
            return;
        }

        int index = findTrendIndex(trendList, score.getScoreId());
        if (index <= 0)
        {
            return;
        }

        SysStudentScoreTrendVo current = trendList.get(index);
        SysStudentScoreTrendVo previous = trendList.get(index - 1);
        StringBuilder reason = new StringBuilder();

        String warningType = "";
        String warningLevel = "";
        String warningItem = "";
        String warningReason = "";

        if (isRankDropped(current.getClassRank(), previous.getClassRank()) && current.getClassRank() - previous.getClassRank() >= 5)
        {
            warningType = "rank_drop";
            warningLevel = "2";
            warningItem = "班级排名";
            warningReason = String.valueOf(reason.append("班级排名从第").append(previous.getClassRank()).append("名下降到第")
                .append(current.getClassRank()).append("名"));

            if(current.getClassRank() - previous.getClassRank() >= 10) {
                warningLevel = "3";
            }

            SysWarningRecord warningRecord = buildWarningRecord(score, warningType, warningLevel, warningItem, warningReason);
            saveWarningIfAbsent(warningRecord);
        }


        if (isRankDropped(current.getGradeRank(), previous.getGradeRank()) && current.getGradeRank() - previous.getGradeRank() >= 5)
        {
            warningType = "rank_drop";
            warningLevel = "2";
            warningItem = "年级排名";
            reason.setLength(0);
            warningReason = String.valueOf(reason.append("年级排名从第").append(previous.getGradeRank()).append("名下降到第")
                .append(current.getGradeRank()).append("名"));

            if(current.getGradeRank() - previous.getGradeRank() >= 10) {
                warningLevel = "3";
            }

            SysWarningRecord warningRecord = buildWarningRecord(score, warningType, warningLevel, warningItem, warningReason);
            saveWarningIfAbsent(warningRecord);
        }
    }

    /** 如果某一科低于及格线，就把科目名称记下来。 */
    private void addIfBelowLine(List<String> lowSubjects, String subjectName, BigDecimal score)
    {
        if (score != null && score.compareTo(PASS_LINE) < 0)
        {
            lowSubjects.add(subjectName);
        }
    }

    /** 在趋势列表里找到当前成绩对应的位置。 */
    private int findTrendIndex(List<SysStudentScoreTrendVo> trendList, Long scoreId)
    {
        if (scoreId == null)
        {
            return -1;
        }
        for (int i = 0; i < trendList.size(); i++)
        {
            if (scoreId.equals(trendList.get(i).getScoreId()))
            {
                return i;
            }
        }
        return -1;
    }

    /** 排名数字越大表示越靠后，所以 current > previous 就表示下降。 */
    private boolean isRankDropped(Long currentRank, Long previousRank)
    {
        return currentRank != null && previousRank != null && currentRank.longValue() > previousRank.longValue();
    }
}
