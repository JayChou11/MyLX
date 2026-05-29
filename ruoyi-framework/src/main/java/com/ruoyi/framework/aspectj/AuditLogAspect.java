package com.ruoyi.framework.aspectj;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.AuditLog;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.domain.SysAuditLog;

/**
 * 业务审计日志切面
 *
 * 核心知识点：
 * 1. AOP（面向切面编程）：通过 @Aspect + @Component 定义切面，
 *    用 @Before/@AfterReturning/@AfterThrowing 定义通知时机，
 *    不修改业务代码即可自动织入审计逻辑。
 *
 * 2. 自定义注解：@AuditLog 标注在方法上，切面通过
 *    "@annotation(auditLog)" 匹配所有带此注解的方法。
 *
 * 3. 反射：通过 JoinPoint 获取方法签名和参数，通过 Method 对象
 *    读取注解属性，通过 Field 对象对比两个实体的差异字段。
 *
 * @author ruoyi
 */
@Aspect
@Component
public class AuditLogAspect
{
    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    /** 变更前数据的 ThreadLocal（@Before 中暂存，@AfterReturning 中使用） */
    private static final ThreadLocal<String> BEFORE_DATA_THREADLOCAL = new ThreadLocal<>();

    /**
     * 处理请求前执行 —— 暂存方法参数（变更前数据）
     *
     * @param joinPoint 切点
     * @param auditLog  注解实例（Spring 自动注入匹配到的注解对象）
     */
    @Before(value = "@annotation(auditLog)")
    public void doBefore(JoinPoint joinPoint, AuditLog auditLog)
    {
        // 序列化方法参数作为"变更前数据"
        try
        {
            Object[] args = joinPoint.getArgs();
            String beforeData = argsArrayToString(args);
            // 截取防止过长
            BEFORE_DATA_THREADLOCAL.set(StringUtils.substring(beforeData, 0, 2000));
        }
        catch (Exception e)
        {
            log.error("审计日志@Before序列化参数异常: {}", e.getMessage());
            BEFORE_DATA_THREADLOCAL.set("");
        }
    }

    /**
     * 处理完请求后执行 —— 成功时记录审计日志
     *
     * @param joinPoint   切点
     * @param auditLog    注解实例
     * @param jsonResult  方法返回值（Spring 自动注入）
     */
    @AfterReturning(pointcut = "@annotation(auditLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, AuditLog auditLog, Object jsonResult)
    {
        handleAuditLog(joinPoint, auditLog, null, jsonResult);
    }

    /**
     * 拦截异常操作 —— 失败时也记录审计日志
     *
     * @param joinPoint 切点
     * @param auditLog  注解实例
     * @param e         异常对象
     */
    @AfterThrowing(value = "@annotation(auditLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, AuditLog auditLog, Exception e)
    {
        handleAuditLog(joinPoint, auditLog, e, null);
    }

    /**
     * 构建并保存审计日志记录
     */
    protected void handleAuditLog(final JoinPoint joinPoint, AuditLog auditLog, final Exception e, Object jsonResult)
    {
        try
        {
            SysAuditLog auditLogEntity = new SysAuditLog();

            // 1. 从注解中读取业务类型和描述（这就是自定义注解的作用）
            auditLogEntity.setBusinessType(auditLog.businessType());
            auditLogEntity.setDetail(auditLog.detail());

            // 2. 通过反射获取类名和方法名（这就是反射的作用）
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            auditLogEntity.setMethod(className + "." + methodName + "()");

            // 3. 获取操作人信息
            auditLogEntity.setOperName(SecurityUtils.getUsername());
            auditLogEntity.setOperIp(IpUtils.getIpAddr());
            auditLogEntity.setOperTime(new Date());

            // 4. 变更前数据（从 ThreadLocal 取出 @Before 中暂存的参数）
            auditLogEntity.setBeforeData(BEFORE_DATA_THREADLOCAL.get());

            // 5. 变更后数据（序列化方法返回值）
            String afterData = "";
            if (jsonResult != null)
            {
                afterData = StringUtils.substring(JSON.toJSONString(jsonResult), 0, 2000);
            }
            auditLogEntity.setAfterData(afterData);

            // 6. 生成变更摘要：用方法返回值的 msg 字段，或用注解的 detail
            String changeSummary = auditLog.detail();
            if (jsonResult != null)
            {
                // 反射读取返回对象的 msg 属性作为摘要
                String msgFromResult = extractMsgByReflection(jsonResult);
                if (StringUtils.isNotEmpty(msgFromResult))
                {
                    changeSummary = msgFromResult;
                }
            }
            auditLogEntity.setChangeSummary(StringUtils.substring(changeSummary, 0, 500));

            // 7. 状态：正常 or 异常
            if (e != null)
            {
                auditLogEntity.setStatus(1);
                auditLogEntity.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            else
            {
                auditLogEntity.setStatus(0);
            }

            // 8. 异步保存到数据库（与若依 @Log 注解一样，用 AsyncManager 异步写入）
            AsyncManager.me().execute(AsyncFactory.recordAudit(auditLogEntity));
        }
        catch (Exception exp)
        {
            log.error("审计日志记录异常: {}", exp.getMessage());
            exp.printStackTrace();
        }
        finally
        {
            // 清除 ThreadLocal，防止内存泄漏
            BEFORE_DATA_THREADLOCAL.remove();
        }
    }

    /**
     * 通过反射提取返回对象中的 msg 属性
     *
     * 这是反射的核心演示：
     * - getDeclaredFields() 获取对象的所有字段
     * - setAccessible(true) 允许访问私有字段
     * - field.get(obj) 通过反射读取字段值
     *
     * @param obj 目标对象
     * @return msg 字段的值，如果没有 msg 字段则返回 null
     */
    private String extractMsgByReflection(Object obj)
    {
        try
        {
            // 反射：遍历对象的声明字段，找到名为 "msg" 的字段
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields)
            {
                if ("msg".equals(field.getName()))
                {
                    field.setAccessible(true);  // 允许访问私有字段
                    Object value = field.get(obj);  // 反射读取字段值
                    return value != null ? value.toString() : null;
                }
            }
        }
        catch (Exception e)
        {
            log.warn("反射读取msg字段异常: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 将方法参数数组序列化为 JSON 字符串
     */
    private String argsArrayToString(Object[] paramsArray)
    {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0)
        {
            for (Object o : paramsArray)
            {
                if (StringUtils.isNotNull(o) && !isFilterObject(o))
                {
                    try
                    {
                        params.append(JSON.toJSONString(o)).append(" ");
                    }
                    catch (Exception e)
                    {
                        log.error("参数序列化异常: {}", e.getMessage());
                    }
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象（与若依 LogAspect 一致）
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o)
    {
        Class<?> clazz = o.getClass();
        if (clazz.isArray())
        {
            return clazz.getComponentType().isAssignableFrom(jakarta.servlet.http.MultipartFile.class);
        }
        else if (java.util.Collection.class.isAssignableFrom(clazz))
        {
            java.util.Collection collection = (java.util.Collection) o;
            for (Object value : collection)
            {
                return value instanceof jakarta.servlet.http.MultipartFile;
            }
        }
        else if (java.util.Map.class.isAssignableFrom(clazz))
        {
            java.util.Map map = (java.util.Map) o;
            for (Object value : map.entrySet())
            {
                java.util.Map.Entry entry = (java.util.Map.Entry) value;
                return entry.getValue() instanceof jakarta.servlet.http.MultipartFile;
            }
        }
        return o instanceof jakarta.servlet.http.MultipartFile
                || o instanceof jakarta.servlet.http.HttpServletRequest
                || o instanceof jakarta.servlet.http.HttpServletResponse;
    }
}