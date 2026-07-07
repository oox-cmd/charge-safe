package com.chargesafe.common;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在 Controller 方法上，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 操作模块 */
    String module() default "";

    /** 操作描述 */
    String description() default "";

    /** 操作类型: CREATE, UPDATE, DELETE, QUERY, LOGIN, OTHER */
    String type() default "OTHER";
}
