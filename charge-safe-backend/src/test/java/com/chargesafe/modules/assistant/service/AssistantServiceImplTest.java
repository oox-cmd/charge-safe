package com.chargesafe.modules.assistant.service;

import com.chargesafe.modules.assistant.mapper.AiQaRecordMapper;
import com.chargesafe.modules.assistant.service.impl.AssistantServiceImpl;
import com.chargesafe.modules.assistant.entity.AiQaRecord;
import com.chargesafe.modules.assistant.vo.AssistantChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssistantServiceImplTest {

    @Test
    void sendsConfiguredBearerTokenWhenCallingTheLlm() {
        CapturingRestTemplate restTemplate = new CapturingRestTemplate();
        AiQaRecordMapper recordMapper = mock(AiQaRecordMapper.class);
        AssistantServiceImpl service = new AssistantServiceImpl(recordMapper, restTemplate);
        ReflectionTestUtils.setField(service, "assistantEnabled", true);
        ReflectionTestUtils.setField(service, "apiKey", "test-api-key");

        service.chat(1L, newRequest("如何预约充电？"));

        assertEquals("Bearer test-api-key",
                restTemplate.request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void recordsSuccessfulLlmAnswerWithLlmSourceType() {
        CapturingRestTemplate restTemplate = new CapturingRestTemplate();
        AiQaRecordMapper recordMapper = mock(AiQaRecordMapper.class);
        AssistantServiceImpl service = new AssistantServiceImpl(recordMapper, restTemplate);
        ReflectionTestUtils.setField(service, "assistantEnabled", true);
        ReflectionTestUtils.setField(service, "apiKey", "test-api-key");

        AssistantChatResponse response = service.chat(1L, newRequest("如何预约充电？"));

        assertEquals("LLM", response.getSourceType());
        org.mockito.ArgumentCaptor<AiQaRecord> recordCaptor =
                org.mockito.ArgumentCaptor.forClass(AiQaRecord.class);
        verify(recordMapper).insert(recordCaptor.capture());
        assertEquals("LLM", recordCaptor.getValue().getSourceType());
        assertEquals("模型回答", recordCaptor.getValue().getAnswer());
    }

    @Test
    void recordsLlmFailureFallbackWithFallbackSourceType() {
        AiQaRecordMapper recordMapper = mock(AiQaRecordMapper.class);
        RestTemplate failingRestTemplate = new RestTemplate() {
            @Override
            public <T> ResponseEntity<T> postForEntity(String url, Object request,
                                                       Class<T> responseType, Object... uriVariables) {
                throw new IllegalStateException("模型服务不可用");
            }
        };
        AssistantServiceImpl service = new AssistantServiceImpl(recordMapper, failingRestTemplate);
        ReflectionTestUtils.setField(service, "assistantEnabled", true);
        ReflectionTestUtils.setField(service, "apiKey", "test-api-key");

        AssistantChatResponse response = service.chat(1L, newRequest("如何预约充电？"));

        assertEquals("FALLBACK", response.getSourceType());
        org.mockito.ArgumentCaptor<AiQaRecord> recordCaptor =
                org.mockito.ArgumentCaptor.forClass(AiQaRecord.class);
        verify(recordMapper).insert(recordCaptor.capture());
        assertEquals("FALLBACK", recordCaptor.getValue().getSourceType());
    }

    @Test
    void assistantStatsUsesActualRecordTotalInsteadOfOverlappingTimeWindows() {
        AiQaRecordMapper recordMapper = mock(AiQaRecordMapper.class);
        when(recordMapper.countToday()).thenReturn(1L);
        when(recordMapper.countWeek()).thenReturn(1L);
        when(recordMapper.countMonth()).thenReturn(1L);
        when(recordMapper.selectCount(any())).thenReturn(1L);
        when(recordMapper.countBySourceType()).thenReturn(List.of());
        when(recordMapper.getHotQuestions()).thenReturn(List.of());
        when(recordMapper.getDailyTrend()).thenReturn(List.of());
        AssistantServiceImpl service = new AssistantServiceImpl(recordMapper, new RestTemplate());

        assertEquals(1L, service.getStats().getTotalCount());
    }

    private static com.chargesafe.modules.assistant.dto.AssistantChatRequest newRequest(String question) {
        com.chargesafe.modules.assistant.dto.AssistantChatRequest request =
                new com.chargesafe.modules.assistant.dto.AssistantChatRequest();
        request.setQuestion(question);
        return request;
    }

    private static class CapturingRestTemplate extends RestTemplate {
        private HttpEntity<?> request;

        @Override
        public <T> ResponseEntity<T> postForEntity(String url, Object request,
                                                   Class<T> responseType, Object... uriVariables) {
            this.request = (HttpEntity<?>) request;
            @SuppressWarnings("unchecked")
            ResponseEntity<T> response = (ResponseEntity<T>) ResponseEntity.ok(Map.of(
                    "choices", List.of(Map.of("message", Map.of("content", "模型回答")))
            ));
            return response;
        }

        @Override
        public <T> T postForObject(String url, Object request, Class<T> responseType,
                                   Object... uriVariables) {
            @SuppressWarnings("unchecked")
            T response = (T) Map.of(
                    "choices", List.of(Map.of("message", Map.of("content", "模型回答")))
            );
            return response;
        }
    }
}
