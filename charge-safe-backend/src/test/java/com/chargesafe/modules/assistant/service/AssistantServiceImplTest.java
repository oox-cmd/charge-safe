package com.chargesafe.modules.assistant.service;

import com.chargesafe.modules.assistant.mapper.AiQaRecordMapper;
import com.chargesafe.modules.assistant.service.impl.AssistantServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

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
