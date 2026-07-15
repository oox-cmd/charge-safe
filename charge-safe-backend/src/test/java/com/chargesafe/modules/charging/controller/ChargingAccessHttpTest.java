package com.chargesafe.modules.charging.controller;

import com.chargesafe.common.GlobalExceptionHandler;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.modules.charging.service.ChargingService;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.service.MenuService;
import com.chargesafe.security.JwtAuthenticationFilter;
import com.chargesafe.security.JwtUtils;
import com.chargesafe.security.PermissionAspect;
import com.chargesafe.security.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChargingAccessHttpTest {

    private static final String SECRET = "ChargeSafeChargingAccessHttpTests-123456789";
    private static final long USER_ID = 5L;
    private static final long COMMUNITY_ID = 10L;

    private final AtomicReference<Set<String>> permissions = new AtomicReference<>(Set.of());
    private ChargingService chargingService;
    private JwtUtils jwtUtils;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserContext.remove();
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expiration", 60_000L);

        SysUser currentUser = new SysUser();
        currentUser.setId(USER_ID);
        currentUser.setUsername("resident01");
        currentUser.setNickname("居民赵六");
        currentUser.setCommunityId(COMMUNITY_ID);
        currentUser.setStatus(1);
        currentUser.setDeleted(0);

        SysUserMapper userMapper = proxy(SysUserMapper.class, (methodName, args) ->
                "selectById".equals(methodName) ? currentUser : null);
        MenuService menuService = proxy(MenuService.class, (methodName, args) ->
                "listPermissionCodesByUserId".equals(methodName) ? permissions.get() : null);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtUtils, userMapper, menuService, new ObjectMapper());

        chargingService = mock(ChargingService.class);
        when(chargingService.pageOrders(any(), any(), any()))
                .thenReturn(new PageResult<>(0, 1, 10, List.of()));
        when(chargingService.pageMyOrders(any(), any()))
                .thenReturn(new PageResult<>(0, 1, 10, List.of()));

        AspectJProxyFactory factory = new AspectJProxyFactory(new ChargingController(chargingService));
        factory.setProxyTargetClass(true);
        factory.addAspect(new PermissionAspect());
        Object controller = factory.getProxy();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(filter)
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void rejectsAnonymousResidentOrderRequestWithHttp401() throws Exception {
        mockMvc.perform(get("/api/orders/my"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
        verifyNoInteractions(chargingService);
    }

    @Test
    void residentCanOnlyUseDedicatedMyOrdersEndpoint() throws Exception {
        mockMvc.perform(get("/api/orders/my")
                        .header("Authorization", bearer("RESIDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(chargingService).pageMyOrders(isNull(), any(PageQuery.class));
    }

    @Test
    void managementOrderListKeepsExisting403BodySemanticsWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", bearer("PROPERTY_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
        verifyNoInteractions(chargingService);
    }

    @Test
    void managementOrderListRequiresAndAcceptsExistingOrderViewPermission() throws Exception {
        permissions.set(Set.of("order:view"));

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", bearer("PROPERTY_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(chargingService).pageOrders(isNull(), isNull(), any(PageQuery.class));
    }

    private String bearer(String roleCode) {
        return "Bearer " + jwtUtils.generateToken(USER_ID, "resident01", roleCode);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> type.getSimpleName() + "TestProxy";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> null;
                        };
                    }
                    return handler.invoke(method.getName(), args);
                });
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object invoke(String methodName, Object[] args);
    }
}
