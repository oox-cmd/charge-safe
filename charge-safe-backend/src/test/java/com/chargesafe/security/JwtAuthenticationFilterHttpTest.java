package com.chargesafe.security;

import com.chargesafe.common.BusinessException;
import com.chargesafe.common.GlobalExceptionHandler;
import com.chargesafe.common.Result;
import com.chargesafe.common.ResultCode;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.service.MenuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JwtAuthenticationFilterHttpTest {

    private static final String SECRET = "ChargeSafeTestSecretKeyForJwtHttpTests-123456789";
    private static final String OTHER_SECRET = "DifferentTestSecretKeyForJwtHttpTests-987654321";

    private final AtomicReference<SysUser> currentUser = new AtomicReference<>();
    private final AtomicReference<Set<String>> currentPermissions = new AtomicReference<>(Set.of("test:read"));
    private final AtomicInteger protectedInvocations = new AtomicInteger();

    private JwtUtils jwtUtils;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserContext.remove();
        jwtUtils = jwtUtils(SECRET, 60_000L);
        currentUser.set(enabledUser(1L, "alice"));

        SysUserMapper userMapper = proxy(SysUserMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return currentUser.get();
            }
            return null;
        });
        MenuService menuService = proxy(MenuService.class, (methodName, args) -> {
            if ("listPermissionCodesByUserId".equals(methodName)) {
                return currentPermissions.get();
            }
            return null;
        });

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtUtils, userMapper, menuService, new ObjectMapper());
        SecurityTestController controller = new SecurityTestController(protectedInvocations);
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(controller);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAspect(new PermissionAspect());

        Object proxiedController = proxyFactory.getProxy();
        mockMvc = MockMvcBuilders.standaloneSetup(proxiedController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(filter)
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void rejectsProtectedRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        assertNoProtectedInvocation();
    }

    @Test
    void rejectsMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Token not-a-bearer-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        assertNoProtectedInvocation();
    }

    @Test
    void rejectsUnparseableToken() throws Exception {
        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        assertNoProtectedInvocation();
    }

    @Test
    void rejectsTokenWithInvalidSignature() throws Exception {
        String token = jwtUtils(OTHER_SECRET, 60_000L)
                .generateToken(1L, "alice", "PROPERTY_ADMIN");

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        assertNoProtectedInvocation();
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        String token = jwtUtils(SECRET, -1_000L)
                .generateToken(1L, "alice", "PROPERTY_ADMIN");

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        assertNoProtectedInvocation();
    }

    @Test
    void rejectsValidTokenWhenUserNoLongerExists() throws Exception {
        currentUser.set(null);

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        assertNoProtectedInvocation();
    }

    @Test
    void rejectsValidTokenWhenUserIsDisabled() throws Exception {
        SysUser disabled = enabledUser(1L, "alice");
        disabled.setStatus(0);
        currentUser.set(disabled);

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        assertNoProtectedInvocation();
    }

    @Test
    void allowsValidTokenToAccessProtectedReadEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("alice"));

        assertTrue(protectedInvocations.get() == 1);
        assertNull(UserContext.getUser());
    }

    @Test
    void allowsPublicLoginEndpointWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertNull(UserContext.getUser());
    }

    @Test
    void keepsPermissionDenialDistinctFromAuthenticationFailure() throws Exception {
        currentPermissions.set(Collections.emptySet());

        mockMvc.perform(get("/api/test/permission")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        assertNull(UserContext.getUser());
    }

    @Test
    void clearsStaleUserContextAfterAuthenticationFailure() throws Exception {
        UserContext.setUser(new LoginUser(999L, "stale-user", "PLATFORM_ADMIN"));

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());

        assertNull(UserContext.getUser());
        assertNoProtectedInvocation();
    }

    @Test
    void authenticationErrorResponseDoesNotExposeSecretsOrToken() throws Exception {
        String invalidToken = "sensitive-token-value";

        MvcResult result = mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains(invalidToken));
        assertFalse(body.contains(SECRET));
        assertFalse(body.toLowerCase().contains("password"));
        assertFalse(body.contains("Exception"));
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(1L, "alice", "PROPERTY_ADMIN");
    }

    private void assertNoProtectedInvocation() {
        assertTrue(protectedInvocations.get() == 0);
        assertNull(UserContext.getUser());
    }

    private static SysUser enabledUser(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname("Alice");
        user.setStatus(1);
        user.setDeleted(0);
        return user;
    }

    private static JwtUtils jwtUtils(String secret, long expiration) {
        JwtUtils utils = new JwtUtils();
        ReflectionTestUtils.setField(utils, "secret", secret);
        ReflectionTestUtils.setField(utils, "expiration", expiration);
        return utils;
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

    @RestController
    @RequestMapping("/api")
    static class SecurityTestController {

        private final AtomicInteger protectedInvocations;

        SecurityTestController(AtomicInteger protectedInvocations) {
            this.protectedInvocations = protectedInvocations;
        }

        @PostMapping("/auth/login")
        Result<Void> publicLogin() {
            return Result.success();
        }

        @GetMapping("/test/protected")
        Result<String> protectedRead() {
            protectedInvocations.incrementAndGet();
            return Result.success(UserContext.getUsername());
        }

        @RequiresPermission("test:write")
        @GetMapping("/test/permission")
        Result<Void> permissionProtected() {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
