package com.chargesafe.modules.auth.controller;

import com.chargesafe.common.GlobalExceptionHandler;
import com.chargesafe.modules.auth.service.AuthService;
import com.chargesafe.modules.user.service.MenuService;
import com.chargesafe.modules.user.vo.MenuVO;
import com.chargesafe.security.LoginUser;
import com.chargesafe.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerPermissionsHttpTest {

    private final AtomicInteger permissionLoaderCalls = new AtomicInteger();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserContext.setUser(new LoginUser(42L, "admin", "PLATFORM_ADMIN"));

        AuthService authService = proxy(AuthService.class, (methodName, args) -> null);
        MenuService menuService = proxy(MenuService.class, (methodName, args) -> {
            if ("listPermissionCodesByUserId".equals(methodName)) {
                permissionLoaderCalls.incrementAndGet();
                return Set.of(
                        "system:user:view",
                        "system:user:create",
                        "system:user:update",
                        "system:user:delete"
                );
            }
            if ("listCurrentUserMenus".equals(methodName)) {
                MenuVO pageMenu = new MenuVO();
                pageMenu.setId(1110L);
                pageMenu.setParentId(1100L);
                pageMenu.setPermissionCode("system:user:view");
                pageMenu.setChildren(List.of());
                return List.of(pageMenu);
            }
            return null;
        });

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, menuService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void permissionsEndpointReturnsButtonCodesFromTheSameLoaderUsedByAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("system:user:create"))
                .andExpect(jsonPath("$.data[1]").value("system:user:delete"))
                .andExpect(jsonPath("$.data[2]").value("system:user:update"))
                .andExpect(jsonPath("$.data[3]").value("system:user:view"));

        assertEquals(1, permissionLoaderCalls.get());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> type.getSimpleName() + "TestProxy";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> null;
                        };
                    }
                    Object result = handler.invoke(method.getName(), args);
                    return result != null ? result : defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        return 0;
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object invoke(String methodName, Object[] args);
    }
}
