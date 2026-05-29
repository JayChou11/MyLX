package com.ruoyi.quartz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.system.service.ISysGradeUpgradeService;

/**
 * 自动升年级定时任务
 *
 * 在 sys_job 表中注册，invoke_target 为 gradeTask.upgradeGrade
 * Cron 表达式：0 0 0 1 7 ?（每年7月1日0点执行）
 * 默认暂停状态，需在定时任务管理页面手动启用
 *
 * @author ruoyi
 */
@Component("gradeTask")
public class GradeTask
{
    private static final Logger log = LoggerFactory.getLogger(GradeTask.class);

    @Autowired
    private ISysGradeUpgradeService gradeUpgradeService;

    /**
     * 执行升年级操作
     * 无参方法，由 Quartz 调度器通过反射调用
     */
    public void upgradeGrade()
    {
        log.info("========== 开始执行自动升年级任务 ==========");
        try
        {
            String result = gradeUpgradeService.upgradeGrade();
            log.info("升年级任务执行结果：{}", result);
        }
        catch (Exception e)
        {
            log.error("升年级任务执行异常", e);
        }
        log.info("========== 自动升年级任务执行结束 ==========");
    }
}