package com.chargesafe.modules.assistant.controller;

import com.chargesafe.common.GlobalExceptionHandler;
import com.chargesafe.modules.assistant.service.AssistantService;
import com.chargesafe.modules.assistant.service.impl.AssistantServiceImpl;
import com.chargesafe.modules.assistant.vo.AssistantChatResponse;
import com.chargesafe.modules.user.entity.SysUser;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.modules.user.service.MenuService;
import com.chargesafe.security.JwtAuthenticationFilter;
import com.chargesafe.security.JwtUtils;
import com.chargesafe.security.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssistantAccessHttpTest {

    private static final String SECRET = "ChargeSafeAssistantHttpTests-123456789";

    private AssistantService assistantService;
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
        user.setNickname("居民赵六");
        user.setStatus(1);
        user.setDeleted(0);

        SysUserMapper userMapper = mock(SysUserMapper.class);
        when(userMapper.selectById(5L)).thenReturn(user);
        MenuService menuService = mock(MenuService.class);
        when(menuService.listPermissionCodesByUserId(5L)).thenReturn(Set.of());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtUtils, userMapper, menuService, new ObjectMapper());

        assistantService = mock(AssistantService.class);
        when(assistantService.chat(eq(5L), any())).thenReturn(AssistantChatResponse.builder()
                .answer("模型回答")
                .sourceType("LLM")
                .build());
        AssistantServiceImpl assistantServiceImpl = new AssistantServiceImpl(
                mock(com.chargesafe.modules.assistant.mapper.AiQaRecordMapper.class), new RestTemplate());

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AssistantController(assistantService, assistantServiceImpl))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(filter)
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void authenticatedResidentChatUsesUserContextInsteadOfMissingRequestAttribute() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"如何预约充电？\"}")
                        .header("Authorization", "Bearer " + jwtUtils.generateToken(5L, "resident01", "RESIDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").value("模型回答"));
    }

    @Test
    void nonResidentCannotUseResidentAssistantEndpoint() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"如何预约充电？\"}")
                        .header("Authorization", "Bearer " + jwtUtils.generateToken(5L, "resident01", "PROPERTY_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
