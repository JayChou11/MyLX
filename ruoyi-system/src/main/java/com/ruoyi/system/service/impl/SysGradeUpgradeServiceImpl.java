package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysClass;
import com.ruoyi.system.mapper.SysClassMapper;
import com.ruoyi.system.mapper.SysStudentMapper;
import com.ruoyi.system.service.ISysDictTypeService;
import com.ruoyi.system.service.ISysGradeUpgradeService;

/**
 * 学生升年级Service业务层处理
 *
 * 核心逻辑：
 * 1. 从字典 student_grade_order 读取年级顺序，构建"当前年级→下一年级"映射
 * 2. 从最高年级往低年级依次处理（避免升级后又被重复升级）
 * 3. 最高年级的学生执行毕业删除
 * 4. 其他年级按班级名称匹配，批量更新学生classId
 *
 * @author ruoyi
 */
@Service
public class SysGradeUpgradeServiceImpl implements ISysGradeUpgradeService
{
    private static final Logger log = LoggerFactory.getLogger(SysGradeUpgradeServiceImpl.class);

    /** 年级顺序字典类型 */
    private static final String GRADE_ORDER_DICT_TYPE = "student_grade_order";

    @Autowired
    private ISysDictTypeService dictTypeService;

    @Autowired
    private SysClassMapper classMapper;

    @Autowired
    private SysStudentMapper studentMapper;

    /**
     * 执行升年级操作
     *
     * @return 升年级结果描述
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "studentClassStat", key = "'all'")
    public String upgradeGrade()
    {
        // 1. 从字典读取年级顺序，构建年级映射
        Map<String, String> gradeNextMap = buildGradeNextMap();
        if (gradeNextMap.isEmpty())
        {
            return "未配置年级顺序字典（student_grade_order），无法执行升年级";
        }

        // 2. 获取所有班级，按年级分组
        List<SysClass> allClasses = classMapper.selectClassList(new SysClass());
        Map<String, List<SysClass>> gradeClassMap = allClasses.stream()
                .collect(Collectors.groupingBy(SysClass::getGrade));

        // 3. 按从高年级到低年级的顺序处理（避免重复升级）
        List<String> sortedGrades = gradeNextMap.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int upgradeCount = 0;
        int graduateCount = 0;
        List<String> detailList = new ArrayList<>();

        for (String currentGrade : sortedGrades)
        {
            String nextGrade = gradeNextMap.get(currentGrade);
            List<SysClass> currentGradeClasses = gradeClassMap.get(currentGrade);
            if (StringUtils.isEmpty(currentGradeClasses))
            {
                continue;
            }

            if (nextGrade == null)
            {
                // 最高年级 → 毕业：删除该年级所有学生
                Long[] classIds = currentGradeClasses.stream()
                        .map(SysClass::getClassId)
                        .toArray(Long[]::new);
                int deleted = studentMapper.deleteStudentByClassIds(classIds);
                graduateCount += deleted;
                detailList.add(currentGrade + " 毕业：删除 " + deleted + " 名学生");
                log.info("升年级 - {} 毕业：删除 {} 名学生", currentGrade, deleted);
            }
            else
            {
                // 非最高年级 → 升级：按班级名称匹配下一年级班级
                List<SysClass> nextGradeClasses = gradeClassMap.get(nextGrade);
                if (StringUtils.isEmpty(nextGradeClasses))
                {
                    detailList.add(currentGrade + " → " + nextGrade + "：下一年级无班级，跳过");
                    log.warn("升年级 - {} → {}：下一年级无班级配置，跳过", currentGrade, nextGrade);
                    continue;
                }

                // 构建下一年级的班级名称→班级ID映射
                Map<String, Long> nextClassNameIdMap = nextGradeClasses.stream()
                        .collect(Collectors.toMap(SysClass::getClassName, SysClass::getClassId, (a, b) -> a));

                int gradeUpgradeCount = 0;
                int skipCount = 0;
                for (SysClass currentClass : currentGradeClasses)
                {
                    Long nextClassId = nextClassNameIdMap.get(currentClass.getClassName());
                    if (nextClassId == null)
                    {
                        skipCount++;
                        log.warn("升年级 - {} {} 在 {} 中无对应班级，跳过",
                                currentGrade, currentClass.getClassName(), nextGrade);
                        continue;
                    }
                    int rows = studentMapper.upgradeStudentClassByClassId(
                            currentClass.getClassId(), nextClassId, "system");
                    gradeUpgradeCount += rows;
                }
                upgradeCount += gradeUpgradeCount;
                detailList.add(currentGrade + " → " + nextGrade
                        + "：升级 " + gradeUpgradeCount + " 名，跳过 " + skipCount + " 个班级");
                log.info("升年级 - {} → {}：升级 {} 名，跳过 {} 个班级",
                        currentGrade, nextGrade, gradeUpgradeCount, skipCount);
            }
        }

        StringBuilder result = new StringBuilder();
        result.append("升年级执行完成！共升级 ").append(upgradeCount).append(" 名学生，毕业 ").append(graduateCount).append(" 名学生。");
        for (String detail : detailList)
        {
            result.append("<br/>").append(detail);
        }
        return result.toString();
    }

    /**
     * 从字典构建年级映射：当前年级 → 下一年级
     * 例如：{初一→初二, 初二→初三, 初三→高一, 高一→高二, 高二→高三, 高三→null}
     *
     * @return 年级映射（LinkedHashMap 保证顺序）
     */
    private Map<String, String> buildGradeNextMap()
    {
        List<SysDictData> dictDataList = dictTypeService.selectDictDataByType(GRADE_ORDER_DICT_TYPE);
        if (dictDataList == null || dictDataList.isEmpty())
        {
            return new LinkedHashMap<>();
        }

        // 按 dictSort 排序，建立有序的年级列表
        List<SysDictData> sortedList = dictDataList.stream()
                .filter(d -> "0".equals(d.getStatus()))
                .sorted(Comparator.comparingLong(SysDictData::getDictSort))
                .collect(Collectors.toList());

        Map<String, String> gradeNextMap = new LinkedHashMap<>();
        for (int i = 0; i < sortedList.size(); i++)
        {
            String currentGrade = sortedList.get(i).getDictValue();
            String nextGrade = (i < sortedList.size() - 1) ? sortedList.get(i + 1).getDictValue() : null;
            gradeNextMap.put(currentGrade, nextGrade);
        }
        return gradeNextMap;
    }
}