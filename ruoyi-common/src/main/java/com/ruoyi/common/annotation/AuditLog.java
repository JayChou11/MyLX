package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务审计日志注解
 *
 * 与若依自带的 @Log 注解不同，@AuditLog 关注的是业务层面的变更详情，
 * 记录"谁在什么时间做了什么变更，变更前后的数据是什么"。
 *
 * 使用示例：
 * @AuditLog(businessType = "TRANSFER", detail = "批量调班")
 * public AjaxResult transferClass(...)
 *
 * @author ruoyi
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog
{
    /**
     * 业务类型标识
     * 如：TRANSFER（调班）、UPGRADE（升年级）、DELETE（删除学生）
     */
    String businessType() default "";

    /**
     * 业务描述（用于前端展示）
     * 如："批量调班"、"执行升年级"、"删除学生"
     */
    String detail() default "";
}