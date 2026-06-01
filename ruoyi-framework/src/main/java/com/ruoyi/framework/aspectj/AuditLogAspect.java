package com.ruoyi.framework.aspectj;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Date;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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
     * 处理请求前执行 —— 构建业务视角的"变更前数据"
     *
     * 改进：不再直接序列化原始 JSON 参数，
     * 而是：注解的 beforeDesc（业务意图） + 反射提取参数的可读字段
     *
     * @param joinPoint 切点
     * @param auditLog  注解实例（Spring 自动注入匹配到的注解对象）
     */
    @Before(value = "@annotation(auditLog)")
    public void doBefore(JoinPoint joinPoint, AuditLog auditLog)
    {
        try
        {
            // 1. 取注解的业务意图描述
            StringBuilder beforeData = new StringBuilder();
            if (StringUtils.isNotEmpty(auditLog.beforeDesc()))
            {
                beforeData.append(auditLog.beforeDesc());
            }

            // 2. 用反射提取参数对象的可读字段（字段名=值），而非原始 JSON
            String readableParams = argsArrayToReadableString(joinPoint.getArgs());
            if (StringUtils.isNotEmpty(readableParams))
            {
                if (beforeData.length() > 0)
                {
                    beforeData.append("\n");
                }
                beforeData.append("操作参数：").append(readableParams);
            }

            BEFORE_DATA_THREADLOCAL.set(StringUtils.substring(beforeData.toString(), 0, 2000));
        }
        catch (Exception e)
        {
            log.error("审计日志@Before构建变更前数据异常: {}", e.getMessage());
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

            // 5. 变更后数据：只记录业务结果描述（AjaxResult 的 msg），不再存整个请求响应
            String afterData = "";
            if (jsonResult != null)
            {
                // 反射读取返回对象的 msg 属性作为"变更后数据"
                // 因为 msg 才是业务层面的描述（如"升年级执行完成！共升级X名学生"）
                String msgFromResult = extractMsgByReflection(jsonResult);
                if (StringUtils.isNotEmpty(msgFromResult))
                {
                    afterData = msgFromResult;
                }
                else
                {
                    // 如果没有 msg，则尝试提取 data 字段
                    String dataFromResult = extractDataByReflection(jsonResult);
                    if (StringUtils.isNotEmpty(dataFromResult))
                    {
                        afterData = dataFromResult;
                    }
                }
            }
            auditLogEntity.setAfterData(StringUtils.substring(afterData, 0, 2000));

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
     * 将方法参数数组转为业务视角的可读字符串
     *
     * 改进：不再直接序列化原始 JSON，而是用反射提取参数对象的字段名和值，
     * 格式化为 "字段名=值" 的可读形式。
     *
     * 例如：原来输出 {"studentIds":[1,2],"classId":5}
     *       现在输出 studentIds=[1, 2], classId=5
     *
     * 这是反射的第二个核心演示：
     * - 对简单类型（Long[], String等），直接展示值
     * - 对复杂对象（DTO、Entity），用反射提取字段名=值
     */
    private String argsArrayToReadableString(Object[] paramsArray)
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
                        String readableParam = objectToReadableString(o);
                        if (StringUtils.isNotEmpty(readableParam))
                        {
                            if (params.length() > 0)
                            {
                                params.append("; ");
                            }
                            params.append(readableParam);
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("参数转可读字符串异常: {}", e.getMessage());
                    }
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 将单个对象转为可读字符串
     *
     * 反射演示：
     * - 数组/集合类型 → 直接展示值列表
     * - DTO/Entity对象 → 反射提取每个字段的名称和值，格式化为"字段名=值"
     * - 简单类型 → 直接展示值
     */
    private String objectToReadableString(Object obj)
    {
        Class<?> clazz = obj.getClass();

        // 数组类型：直接展示元素列表
        if (clazz.isArray())
        {
            // 用 java.lang.reflect.Array 统一处理基本类型数组（如 long[]）和对象数组（如 Long[]）
            int length = Array.getLength(obj);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(length, 20); i++)
            {
                if (i > 0) sb.append(", ");
                sb.append(Array.get(obj, i));
            }
            if (length > 20)
            {
                sb.append("...(共").append(length).append("项)");
            }
            return sb.toString();
        }

        // 基本类型和String：直接展示值
        if (clazz.isPrimitive() || obj instanceof String || obj instanceof Number)
        {
            return obj.toString();
        }

        // DTO/Entity对象：反射提取字段名=值
        // 只取业务相关的字段（跳过 updateBy 等内部字段），最多取10个字段
        StringBuilder sb = new StringBuilder();
        Field[] fields = clazz.getDeclaredFields();
        int count = 0;
        for (Field field : fields)
        {
            String fieldName = field.getName();
            // 跳过内部字段和序列化字段
            if ("serialVersionUID".equals(fieldName) || "updateBy".equals(fieldName)
                    || "createBy".equals(fieldName) || "createTime".equals(fieldName)
                    || "updateTime".equals(fieldName) || "remark".equals(fieldName))
            {
                continue;
            }
            try
            {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null)
                {
                    if (count > 0) sb.append(", ");
                    // 字段值如果是数组，简短展示（用 java.lang.reflect.Array 统一处理）
                    if (value.getClass().isArray())
                    {
                        int arrLen = Array.getLength(value);
                        StringBuilder arrSb = new StringBuilder();
                        for (int i = 0; i < Math.min(arrLen, 10); i++)
                        {
                            if (i > 0) arrSb.append(", ");
                            arrSb.append(Array.get(value, i));
                        }
                        if (arrLen > 10) arrSb.append("...(共").append(arrLen).append("项)");
                        sb.append(fieldName).append("=[").append(arrSb).append("]");
                    }
                    else
                    {
                        sb.append(fieldName).append("=").append(value);
                    }
                    count++;
                    if (count >= 10) break;  // 最多展示10个字段
                }
            }
            catch (Exception e)
            {
                // 反射读取失败，跳过此字段
            }
        }
        return sb.toString();
    }

    /**
     * 通过反射提取返回对象中的 data 属性
     *
     * @param obj 目标对象
     * @return data 字段的字符串表示，如果没有则返回 null
     */
    private String extractDataByReflection(Object obj)
    {
        try
        {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields)
            {
                if ("data".equals(field.getName()))
                {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value != null)
                    {
                        return StringUtils.substring(JSON.toJSONString(value), 0, 500);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.warn("反射读取data字段异常: {}", e.getMessage());
        }
        return null;
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
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        }
        else if (java.util.Collection.class.isAssignableFrom(clazz))
        {
            java.util.Collection collection = (java.util.Collection) o;
            for (Object value : collection)
            {
                return value instanceof MultipartFile;
            }
        }
        else if (java.util.Map.class.isAssignableFrom(clazz))
        {
            java.util.Map map = (java.util.Map) o;
            for (Object value : map.entrySet())
            {
                java.util.Map.Entry entry = (java.util.Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile
                || o instanceof jakarta.servlet.http.HttpServletRequest
                || o instanceof jakarta.servlet.http.HttpServletResponse;
    }
}