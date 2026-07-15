package com.chargesafe.modules.resident.controller;

import com.chargesafe.common.GlobalExceptionHandler;
import com.chargesafe.modules.resident.service.ResidentDashboardService;
import com.chargesafe.modules.resident.vo.ResidentDashboardVO;
import com.chargesafe.modules.statistics.controller.StatisticsController;
import com.chargesafe.modules.statistics.service.StatisticsService;
import com.chargesafe.modules.statistics.vo.ManagementBigscreenVO;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardAccessHttpTest {

    private static final String SECRET = "ChargeSafeResidentDashboardHttpTests-123456789";
    private static final long USER_ID = 5L;
    private static final long COMMUNITY_ID = 10L;

    private final AtomicReference<Set<String>> permissions = new AtomicReference<>(Set.of());
    private final AtomicReference<SysUser> user = new AtomicReference<>();

    private ResidentDashboardService residentDashboardService;
    private StatisticsService statisticsService;
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
        user.set(currentUser);

        SysUserMapper userMapper = proxy(SysUserMapper.class, (methodName, args) ->
                "selectById".equals(methodName) ? user.get() : null);
        MenuService menuService = proxy(MenuService.class, (methodName, args) ->
                "listPermissionCodesByUserId".equals(methodName) ? permissions.get() : null);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtUtils, userMapper, menuService, new ObjectMapper());

        residentDashboardService = mock(ResidentDashboardService.class);
        statisticsService = mock(StatisticsService.class);
        when(residentDashboardService.getCurrentDashboard()).thenReturn(new ResidentDashboardVO());
        when(statisticsService.getManagementBigscreen(any())).thenReturn(new ManagementBigscreenVO());

        Object residentController = withPermissionAspect(
                new ResidentDashboardController(residentDashboardService));
        Object statisticsController = withPermissionAspect(new StatisticsController(statisticsService));
        mockMvc = MockMvcBuilders.standaloneSetup(residentController, statisticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(filter)
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void rejectsAnonymousResidentDashboardRequestWithHttp401() throws Exception {
        mockMvc.perform(get("/api/resident/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
        verify(residentDashboardService, never()).getCurrentDashboard();
    }

    @Test
    void rejectsInvalidJwtWithHttp401() throws Exception {
        mockMvc.perform(get("/api/resident/dashboard")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void deniesAuthenticatedUserWithoutResidentPermissionUsingExisting403BodySemantics() throws Exception {
        permissions.set(Set.of("dashboard:view"));

        mockMvc.perform(get("/api/resident/dashboard")
                        .header("Authorization", bearer("MAINTAINER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
        verify(residentDashboardService, never()).getCurrentDashboard();
    }

    @Test
    void allowsResidentPermissionAndIgnoresAnyRequestCommunityId() throws Exception {
        permissions.set(Set.of("resident:dashboard:view"));

        mockMvc.perform(get("/api/resident/dashboard")
                        .param("communityId", "999")
                        .header("Authorization", bearer("RESIDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(residentDashboardService).getCurrentDashboard();
    }

    @Test
    void platformAdminDoesNotGainResidentEndpointFromItsAllEnabledPermissionsBehavior() throws Exception {
        permissions.set(Set.of("resident:dashboard:view", "statistics:bigscreen:view"));

        mockMvc.perform(get("/api/resident/dashboard")
                        .header("Authorization", bearer("PLATFORM_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
        verify(residentDashboardService, never()).getCurrentDashboard();
    }

    @Test
    void residentCannotAccessManagementBigscreenData() throws Exception {
        permissions.set(Set.of("resident:dashboard:view"));

        mockMvc.perform(get("/api/statistics/bigscreen")
                        .header("Authorization", bearer("RESIDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
        verify(statisticsService, never()).getManagementBigscreen(any());
    }

    @Test
    void platformAdminCanAccessManagementBigscreenData() throws Exception {
        permissions.set(Set.of("statistics:bigscreen:view"));

        mockMvc.perform(get("/api/statistics/bigscreen")
                        .header("Authorization", bearer("PLATFORM_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(statisticsService).getManagementBigscreen(null);
    }

    @Test
    void propertyAdminIsRestrictedToItsServerSideCommunity() throws Exception {
        permissions.set(Set.of("statistics:bigscreen:view"));

        mockMvc.perform(get("/api/statistics/bigscreen")
                        .param("communityId", "999")
                        .header("Authorization", bearer("PROPERTY_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(statisticsService).getManagementBigscreen(COMMUNITY_ID);
    }

    private Object withPermissionAspect(Object controller) {
        AspectJProxyFactory factory = new AspectJProxyFactory(controller);
        factory.setProxyTargetClass(true);
        factory.addAspect(new PermissionAspect());
        return factory.getProxy();
    }

    private String bearer(String roleCode) {
        return "Bearer " + jwtUtils.generateToken(USER_ID, "resident01", roleCode);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(), new Class<?>[]{type},
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
