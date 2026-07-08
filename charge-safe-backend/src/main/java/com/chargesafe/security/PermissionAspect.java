package com.chargesafe.security;

import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 接口方法级权限校验切面。
 *
 * <p>拦截带 {@link RequiresPermission} 注解的 Controller 方法，在方法执行前校验
 * 当前登录用户是否拥有注解声明的权限码。平台管理员（permissions 为 null）放行。
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(com.chargesafe.security.RequiresPermission)")
    public void checkPermission(JoinPoint joinPoint) {
        LoginUser user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
        if (annotation == null) {
            return;
        }

        if (!user.hasPermission(annotation.value())) {
            log.warn("权限不足：用户 {} 尝试访问需要权限 {} 的接口 {}",
                    user.getUsername(), annotation.value(), method.getName());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
