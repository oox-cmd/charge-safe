package com.chargesafe.modules.assistant.service.impl;

import com.chargesafe.modules.assistant.dto.AssistantChatRequest;
import com.chargesafe.modules.assistant.entity.AiQaRecord;
import com.chargesafe.modules.assistant.mapper.AiQaRecordMapper;
import com.chargesafe.modules.assistant.service.AssistantService;
import com.chargesafe.modules.assistant.vo.AssistantChatResponse;
import com.chargesafe.modules.assistant.vo.AssistantStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 助手服务实现
 * <p>
 * 支持：mimo API 调用 + FAQ 兜底 + 流式输出 + 对话历史 + 知识库增强
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private final AiQaRecordMapper aiQaRecordMapper;
    private final RestTemplate restTemplate;

    @Value("${assistant.enabled:false}")
    private boolean assistantEnabled;

    @Value("${assistant.api-key:}")
    private String apiKey;

    @Value("${assistant.base-url:https://token-plan-cn.xiaomimimo.com/v1}")
    private String baseUrl;

    @Value("${assistant.model:mimo-v2.5}")
    private String model;

    @Value("${assistant.max-history:10}")
    private int maxHistory;

    /**
     * 用户对话历史（内存存储，轻量版本）
     */
    private final ConcurrentHashMap<Long, List<Map<String, String>>> userHistories = new ConcurrentHashMap<>();

    /**
     * 系统提示词 - ChargeSafe 充电助手专属
     */
    private static final String SYSTEM_PROMPT = """
            你是 ChargeSafe 社区充电安全助手，专门回答与电动自行车充电、预约、订单、端口状态、故障报修、充电安全、系统使用相关的问题。

            【你的能力边界】
            1. 你不能执行任何业务操作
            2. 你不能声称已经替用户预约、报修、派单或修改设备状态
            3. 你只能提供操作指引和安全建议
            4. 如果用户要求执行操作，请引导他去对应页面手动操作

            【端口状态说明】
            - IDLE：空闲，可以预约
            - RESERVED：已预约
            - CHARGING：充电中
            - FAULT：故障，不可预约，应报修
            - MAINTENANCE：维修中
            - DISABLED：停用
            - OFFLINE：离线

            【预约规则】
            1. 只有空闲（IDLE）端口可以预约
            2. 同一用户不能同时存在多个有效预约
            3. 预约后需在规定时间内使用，否则会过期
            4. 取消预约后端口恢复空闲

            【充电流程】
            1. 预约成功后，在规定时间内到达车棚
            2. 扫描端口二维码或手动开始充电
            3. 充电完成后手动结束充电
            4. 系统自动计算费用并生成订单

            【报修规则】
            1. 如果发现端口故障、设备异常、无法充电，应在小程序提交报修
            2. 报修后可查看处理进度
            3. 故障端口状态会显示为 FAULT

            【回答要求】
            1. 简洁明了，不要超过200字
            2. 口语化，面向居民用户
            3. 不要讲后台管理术语
            4. 不要编造系统没有的功能
            5. 如果不知道，提示用户联系物业或提交报修
            """;

    /**
     * FAQ 兜底答案 - 更完整的知识库
     */
    private static final Map<String, String> FAQ_MAP = new LinkedHashMap<>();

    static {
        FAQ_MAP.put("预约", "你可以在首页点击车棚，进入详情页后选择空闲（绿色）端口，点击预约按钮。预约成功后请在规定时间内前往充电。");
        FAQ_MAP.put("取消预约", "你可以在「我的预约」页面找到对应预约，点击取消按钮。取消后端口会恢复空闲状态。");
        FAQ_MAP.put("失效", "预约后需要在规定时间内使用，超时会自动失效。你可以重新预约其他空闲端口。");
        FAQ_MAP.put("过期", "预约后需要在规定时间内使用，超时会自动失效。你可以重新预约其他空闲端口。");
        FAQ_MAP.put("故障", "如果端口显示红色或提示故障，请不要使用该端口。你可以在车棚详情页点击「提交报修」，我们会尽快处理。");
        FAQ_MAP.put("端口状态", "端口状态说明：\n🟢 绿色 - 空闲可预约\n🔵 蓝色 - 已预约\n🟡 黄色 - 充电中\n🔴 红色 - 故障\n⚪ 灰色 - 停用/离线");
        FAQ_MAP.put("状态", "端口状态说明：\n🟢 绿色 - 空闲可预约\n🔵 蓝色 - 已预约\n🟡 黄色 - 充电中\n🔴 红色 - 故障\n⚪ 灰色 - 停用/离线");
        FAQ_MAP.put("订单", "你可以在小程序底部导航栏点击「我的」，然后点击「我的订单」查看充电记录和费用明细。");
        FAQ_MAP.put("费用", "充电费用根据实际充电时长和电价计算。你可以在订单详情中查看费用明细。");
        FAQ_MAP.put("报修", "你可以在小程序首页点击「故障报修」，或在车棚详情页点击「提交报修」，填写故障描述后提交。");
        FAQ_MAP.put("报修进度", "你可以在「我的报修」页面查看报修记录和处理进度。");
        FAQ_MAP.put("安全", "充电安全注意事项：\n1. 不要使用破损的充电线\n2. 不要在室内充电\n3. 不要长时间无人看管充电\n4. 发现异常立即停止充电并报修\n5. 不要在高温环境下充电");
        FAQ_MAP.put("开始充电", "预约成功后，在规定时间内到达车棚，扫描端口二维码或在页面点击「开始充电」按钮。");
        FAQ_MAP.put("结束充电", "充电完成后，在充电页面点击「结束充电」按钮，系统会自动计算费用并生成订单。");
        FAQ_MAP.put("扫码", "你可以在车棚详情页扫描端口二维码，快速开始充电。");
        FAQ_MAP.put("车棚", "你可以在首页点击「充电车棚」查看附近的车棚和可用端口。");
        FAQ_MAP.put("密码", "如需修改密码，请联系管理员。");
        FAQ_MAP.put("登录", "如遇登录问题，请检查账号密码是否正确，或联系管理员重置密码。");
        FAQ_MAP.put("物业", "如需联系物业，请拨打物业电话或在小程序中提交报修。");
    }

    @Override
    public AssistantChatResponse chat(Long userId, AssistantChatRequest request) {
        String question = request.getQuestion();
        String pageContext = request.getPageContext();

        // 1. 优先尝试调用大模型
        if (assistantEnabled && apiKey != null && !apiKey.isEmpty()) {
            try {
                return callLlm(userId, question, pageContext);
            } catch (Exception e) {
                log.warn("大模型调用失败，使用 FAQ 兜底: {}", e.getMessage());
            }
        }

        // 2. FAQ 兜底
        AssistantChatResponse response = getFaqAnswer(question);
        response.setSourceType("FAQ");

        // 3. 保存问答记录
        saveQaRecord(userId, question, response.getAnswer(), pageContext, response.getSourceType());

        return response;
    }

    /**
     * 流式输出接口
     */
    public SseEmitter chatStream(Long userId, AssistantChatRequest request) {
        SseEmitter emitter = new SseEmitter(60000L);

        String question = request.getQuestion();
        String pageContext = request.getPageContext();

        // 如果大模型未启用，直接返回 FAQ
        if (!assistantEnabled || apiKey == null || apiKey.isEmpty()) {
            try {
                AssistantChatResponse response = getFaqAnswer(question);
                response.setSourceType("FAQ");
                emitter.send(SseEmitter.event().name("message").data(response));
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
                saveQaRecord(userId, question, response.getAnswer(), pageContext, "FAQ");
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 异步调用大模型流式输出
        new Thread(() -> {
            try {
                String answer = callLlmStream(userId, question, pageContext, emitter);
                saveQaRecord(userId, question, answer, pageContext, "LLM");
            } catch (Exception e) {
                log.error("流式输出失败", e);
                try {
                    AssistantChatResponse fallback = getFaqAnswer(question);
                    emitter.send(SseEmitter.event().name("message").data(fallback));
                    emitter.send(SseEmitter.event().name("done").data(""));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        }).start();

        return emitter;
    }

    /**
     * 调用大模型（非流式）
     */
    private AssistantChatResponse callLlm(Long userId, String question, String pageContext) {
        // 获取或初始化用户对话历史
        List<Map<String, String>> history = userHistories.computeIfAbsent(userId, k -> new ArrayList<>());

        // 构建请求消息
        List<Map<String, String>> messages = buildMessages(history, question, pageContext);

        // 调用 API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                baseUrl + "/chat/completions",
                new HttpEntity<>(requestBody, headers),
                Map.class
        );
        Map<String, Object> response = responseEntity.getBody();

        // 解析响应
        String answer = extractAnswer(response);

        // 更新对话历史
        updateHistory(history, question, answer);

        // 构建响应
        AssistantChatResponse chatResponse = AssistantChatResponse.builder()
                .answer(answer)
                .suggestions(getSuggestions(pageContext))
                .sourceType("LLM")
                .build();

        return chatResponse;
    }

    /**
     * 调用大模型（流式输出）
     */
    private String callLlmStream(Long userId, String question, String pageContext, SseEmitter emitter) throws Exception {
        // 获取或初始化用户对话历史
        List<Map<String, String>> history = userHistories.computeIfAbsent(userId, k -> new ArrayList<>());

        // 构建请求消息
        List<Map<String, String>> messages = buildMessages(history, question, pageContext);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);
        requestBody.put("stream", true);

        // 发送请求并读取流式响应
        StringBuilder fullAnswer = new StringBuilder();
        String requestBodyJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);

        URL url = new URL(baseUrl + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(60000);

        try {
            // 发送请求
            connection.getOutputStream().write(requestBodyJson.getBytes(StandardCharsets.UTF_8));

            // 读取流式响应
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if (data.equals("[DONE]")) {
                            break;
                        }
                        try {
                            Map<String, Object> chunk = new com.fasterxml.jackson.databind.ObjectMapper().readValue(data, Map.class);
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                if (delta != null && delta.containsKey("content")) {
                                    String content = (String) delta.get("content");
                                    fullAnswer.append(content);
                                    // 发送增量内容
                                    emitter.send(SseEmitter.event().name("message").data(
                                            AssistantChatResponse.builder()
                                                    .answer(content)
                                                    .sourceType("LLM")
                                                    .build()
                                    ));
                                }
                            }
                        } catch (Exception e) {
                            // 忽略解析错误，继续读取
                        }
                    }
                }
            }

            // 发送完成信号
            emitter.send(SseEmitter.event().name("done").data(""));
            emitter.complete();

        } finally {
            connection.disconnect();
        }

        // 更新对话历史
        updateHistory(history, question, fullAnswer.toString());

        return fullAnswer.toString();
    }

    /**
     * 构建消息列表
     */
    private List<Map<String, String>> buildMessages(List<Map<String, String>> history, String question, String pageContext) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示词
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        // 历史对话（滑动窗口，保留最近 N 轮）
        int start = Math.max(0, history.size() - maxHistory);
        messages.addAll(history.subList(start, history.size()));

        // 当前用户问题
        String userMessage = question;
        if (pageContext != null && !pageContext.isEmpty()) {
            userMessage = "[当前页面: " + pageContext + "] " + question;
        }
        messages.add(Map.of("role", "user", "content", userMessage));

        return messages;
    }

    /**
     * 更新对话历史
     */
    private void updateHistory(List<Map<String, String>> history, String question, String answer) {
        history.add(Map.of("role", "user", "content", question));
        history.add(Map.of("role", "assistant", "content", answer));

        // 滑动窗口：保留最近 maxHistory 轮
        if (history.size() > maxHistory * 2) {
            int removeCount = history.size() - maxHistory * 2;
            for (int i = 0; i < removeCount; i++) {
                history.remove(0);
            }
        }
    }

    /**
     * 从 API 响应中提取回答
     */
    private String extractAnswer(Map<String, Object> response) {
        if (response == null || !response.containsKey("choices")) {
            return "抱歉，我暂时无法回答这个问题。请稍后再试或联系物业。";
        }

        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("解析大模型响应失败", e);
        }

        return "抱歉，我暂时无法回答这个问题。请稍后再试或联系物业。";
    }

    /**
     * 获取 FAQ 兜底答案
     */
    private AssistantChatResponse getFaqAnswer(String question) {
        for (Map.Entry<String, String> entry : FAQ_MAP.entrySet()) {
            if (question.contains(entry.getKey())) {
                return AssistantChatResponse.builder()
                        .answer(entry.getValue())
                        .suggestions(getDefaultSuggestions())
                        .build();
            }
        }

        // 默认回答
        return AssistantChatResponse.builder()
                .answer("抱歉，我不太理解你的问题。你可以尝试问：\n1. 怎么预约充电？\n2. 端口故障怎么办？\n3. 怎么查看订单？\n4. 充电时要注意什么？\n\n或者联系物业寻求帮助。")
                .suggestions(getDefaultSuggestions())
                .build();
    }

    /**
     * 获取默认推荐操作
     */
    private List<String> getDefaultSuggestions() {
        return Arrays.asList("查看车棚详情", "查看我的订单", "提交设备报修");
    }

    /**
     * 根据页面上下文获取推荐操作
     */
    private List<String> getSuggestions(String pageContext) {
        if (pageContext == null) {
            return getDefaultSuggestions();
        }

        return switch (pageContext) {
            case "parking-detail" -> Arrays.asList("预约充电", "查看端口状态", "提交报修");
            case "reservation" -> Arrays.asList("查看预约状态", "取消预约", "查看车棚详情");
            case "charging" -> Arrays.asList("结束充电", "查看订单", "提交报修");
            case "order" -> Arrays.asList("查看订单详情", "查看车棚", "提交报修");
            case "repair" -> Arrays.asList("查看报修进度", "查看车棚", "联系物业");
            default -> getDefaultSuggestions();
        };
    }

    /**
     * 保存问答记录
     */
    private void saveQaRecord(Long userId, String question, String answer, String pageContext, String sourceType) {
        try {
            AiQaRecord record = new AiQaRecord();
            record.setUserId(userId);
            record.setQuestion(question);
            record.setAnswer(answer);
            record.setPageContext(pageContext);
            record.setSourceType(sourceType);
            record.setCreatedTime(LocalDateTime.now());
            aiQaRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("保存问答记录失败", e);
        }
    }

    /**
     * 获取统计数据
     */
    @Override
    public AssistantStatsVO getStats() {
        // 今日问答数
        Long todayCount = aiQaRecordMapper.countToday();

        // 本周问答数
        Long weekCount = aiQaRecordMapper.countWeek();

        // 本月问答数
        Long monthCount = aiQaRecordMapper.countMonth();

        // 总问答数
        Long totalCount = todayCount + weekCount + monthCount;

        // 来源分布
        Map<String, Long> sourceDistribution = new HashMap<>();
        List<Map<String, Object>> sourceData = aiQaRecordMapper.countBySourceType();
        for (Map<String, Object> item : sourceData) {
            String source = (String) item.get("source_type");
            Long count = ((Number) item.get("count")).longValue();
            sourceDistribution.put(source, count);
        }

        // 高频问题 TOP10
        List<AssistantStatsVO.HotQuestion> hotQuestions = new ArrayList<>();
        List<Map<String, Object>> hotData = aiQaRecordMapper.getHotQuestions();
        for (Map<String, Object> item : hotData) {
            String question = (String) item.get("question");
            Long count = ((Number) item.get("count")).longValue();
            // 截断过长的问题
            if (question.length() > 30) {
                question = question.substring(0, 30) + "...";
            }
            hotQuestions.add(AssistantStatsVO.HotQuestion.builder()
                    .question(question)
                    .count(count)
                    .build());
        }

        // 近7天趋势
        List<AssistantStatsVO.DailyCount> dailyTrend = new ArrayList<>();
        List<Map<String, Object>> trendData = aiQaRecordMapper.getDailyTrend();
        for (Map<String, Object> item : trendData) {
            Object dateObj = item.get("date");
            String date = dateObj != null ? dateObj.toString() : "";
            Long count = ((Number) item.get("count")).longValue();
            dailyTrend.add(AssistantStatsVO.DailyCount.builder()
                    .date(date)
                    .count(count)
                    .build());
        }

        return AssistantStatsVO.builder()
                .todayCount(todayCount)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .totalCount(totalCount)
                .sourceDistribution(sourceDistribution)
                .hotQuestions(hotQuestions)
                .dailyTrend(dailyTrend)
                .build();
    }
}
