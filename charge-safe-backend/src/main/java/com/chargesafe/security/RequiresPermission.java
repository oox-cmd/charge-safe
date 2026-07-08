package com.chargesafe.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口方法级权限校验注解。
 *
 * <p>标注在 Controller 方法上，声明该方法所需权限码（对应 sys_menu.permission_code）。
 * 由 {@link PermissionAspect} 在方法执行前校验当前登录用户是否拥有该权限码；
 * 平台管理员（permissions 为 null）默认放行。
 *
 * <p>示例：
 * <pre>
 *   &#64;RequiresPermission("system:user:create")
 *   &#64;PostMapping
 *   public Result&lt;?&gt; create(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String value();
}
