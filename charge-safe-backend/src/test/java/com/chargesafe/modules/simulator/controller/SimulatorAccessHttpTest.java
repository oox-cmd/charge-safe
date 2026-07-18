package com.chargesafe.modules.simulator.controller;

import com.chargesafe.common.GlobalExceptionHandler;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.simulator.service.SimulatorService;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SimulatorAccessHttpTest {

    private static final String SECRET = "ChargeSafeSimulatorAccessHttpTests-123456789";

    private final AtomicReference<Set<String>> permissions = new AtomicReference<>(Set.of());
    private SimulatorService simulatorService;
    private JwtUtils jwtUtils;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserContext.remove();
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expiration", 60_000L);

        SysUser user = new SysUser();
        user.setId(5L);
        user.setUsername("resident01");
        user.setStatus(1);
        user.setDeleted(0);

        SysUserMapper userMapper = proxy(SysUserMapper.class, (methodName, args) ->
                "selectById".equals(methodName) ? user : null);
        MenuService menuService = proxy(MenuService.class, (methodName, args) ->
                "listPermissionCodesByUserId".equals(methodName) ? permissions.get() : null);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtUtils, userMapper, menuService, new ObjectMapper());

        simulatorService = mock(SimulatorService.class);
        ChargingPort port = new ChargingPort();
        port.setId(10L);
        when(simulatorService.simulatePortFault(10L)).thenReturn(port);

        AspectJProxyFactory factory = new AspectJProxyFactory(new SimulatorController(simulatorService));
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
    void rejectsSimulatorMutationWithoutSimulatorPermission() throws Exception {
        mockMvc.perform(post("/api/simulator/ports/10/fault")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        verifyNoInteractions(simulatorService);
    }

    @Test
    void permitsSimulatorMutationWithSimulatorPermission() throws Exception {
        permissions.set(Set.of("simulator:manage"));

        mockMvc.perform(post("/api/simulator/ports/10/fault")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(simulatorService).simulatePortFault(10L);
    }

    private String bearer() {
        return "Bearer " + jwtUtils.generateToken(5L, "resident01", "RESIDENT");
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
