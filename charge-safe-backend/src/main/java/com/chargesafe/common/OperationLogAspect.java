package com.chargesafe.common;

import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志 AOP 切面
 * 自动记录标注了 @OperationLog 注解的方法操作
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final com.chargesafe.common.OperationLogMapper operationLogMapper;

    @Pointcut("@annotation(com.chargesafe.common.OperationLog)")
    public void logPointcut() {}

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        OperationLogEntity logEntity = new OperationLogEntity();
        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog annotation = method.getAnnotation(OperationLog.class);

            logEntity.setModule(annotation.module());
            logEntity.setOperation(annotation.description());
            logEntity.setMethod(signature.getDeclaringTypeName() + "." + method.getName());

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setUrl(request.getRequestURI());
                logEntity.setIp(getClientIp(request));
            }

            // 获取参数
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                StringBuilder paramsStr = new StringBuilder();
                for (Object arg : args) {
                    if (arg != null && !(arg instanceof jakarta.servlet.http.HttpServletRequest)
                            && !(arg instanceof jakarta.servlet.http.HttpServletResponse)) {
                        paramsStr.append(arg.toString()).append("; ");
                    }
                }
                logEntity.setParams(paramsStr.length() > 1000
                        ? paramsStr.substring(0, 1000) : paramsStr.toString());
            }

            // 获取当前用户
            Long userId = UserContext.getUserId();
            if (userId != null) {
                logEntity.setUserId(userId);
                logEntity.setUsername(UserContext.getUsername());
            }

            // 执行方法
            Object result = joinPoint.proceed();

            logEntity.setStatus(1);
            logEntity.setDuration(System.currentTimeMillis() - startTime);

            // 异步保存日志
            saveLog(logEntity);

            return result;
        } catch (Throwable e) {
            logEntity.setStatus(0);
            logEntity.setErrorMsg(e.getMessage());
            logEntity.setDuration(System.currentTimeMillis() - startTime);
            saveLog(logEntity);
            throw e;
        }
    }

    private void saveLog(OperationLogEntity logEntity) {
        try {
            operationLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
