package com.healthy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AiController {

    private final ObjectMapper json = new ObjectMapper();

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.model:gpt-4o-mini}")
    private String model;

    @Value("${ai.timeout-seconds:30}")
    private int timeoutSeconds;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        try {
            String message = (String) body.getOrDefault("message", "");
            if (message == null || message.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "message required"));

            if (apiKey == null || apiKey.isBlank()) {
                return ResponseEntity.status(503).body(Map.of(
                        "error", "ai_not_configured",
                        "detail", "未配置真实大模型密钥，请设置 AI_API_KEY（或 ARK_API_KEY / OPENAI_API_KEY）后重试"
                ));
            }

            String finalApiUrl = buildApiUrl(apiUrl);
            boolean isResponsesApi = finalApiUrl.endsWith("/responses");

            Map<String, Object> payload = buildPayload(message, isResponsesApi);

            String reqBody = json.writeValueAsString(payload);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(finalApiUrl))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 400) {
                return ResponseEntity.status(resp.statusCode()).body(Map.of("error", "model api error", "detail", resp.body()));
            }

            JsonNode root = json.readTree(resp.body());
            String reply = extractReply(root, isResponsesApi);
            if (reply.isEmpty()) reply = "(模型未返回可用文本)";
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "internal", "detail", ex.getMessage()));
        }
    }

    private String buildApiUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return "https://api.openai.com/v1/chat/completions";
        }
        String trimmed = rawUrl.trim();
        if (trimmed.endsWith("/responses") || trimmed.endsWith("/chat/completions") || trimmed.endsWith("/v1/chat/completions")) {
            return trimmed;
        }
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        if (trimmed.contains("volces.com/api/v3")) {
            return trimmed + "/responses";
        }
        return trimmed + "/chat/completions";
    }

    private Map<String, Object> buildPayload(String message, boolean isResponsesApi) {
        if (isResponsesApi) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "input_text");
            textPart.put("text", message);

            Map<String, Object> userItem = new HashMap<>();
            userItem.put("role", "user");
            userItem.put("content", new Object[]{textPart});

            payload.put("input", new Object[]{userItem});
            return payload;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("max_tokens", 800);
        payload.put("temperature", 0.8);

        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", "你是一名温和、专业、简洁的校园心理支持助手。避免诊断，优先情绪支持与可执行建议。遇到危机信号时建议尽快联系校心理中心或紧急热线。");

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", message);

        payload.put("messages", new Object[]{system, user});
        return payload;
    }

    private String extractReply(JsonNode root, boolean isResponsesApi) {
        if (isResponsesApi) {
            if (root.has("output_text") && !root.get("output_text").isNull()) {
                String outputText = root.get("output_text").asText("");
                if (!outputText.isBlank()) return outputText;
            }

            if (root.has("output") && root.get("output").isArray()) {
                List<String> parts = new ArrayList<>();
                for (JsonNode outputItem : root.get("output")) {
                    JsonNode content = outputItem.get("content");
                    if (content != null && content.isArray()) {
                        for (JsonNode contentItem : content) {
                            if (contentItem.has("text")) {
                                String t = contentItem.get("text").asText("");
                                if (!t.isBlank()) parts.add(t);
                            }
                        }
                    }
                }
                if (!parts.isEmpty()) return String.join("\n", parts);
            }
            return "";
        }

        if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
            JsonNode first = root.get("choices").get(0);
            if (first.has("message") && first.get("message").has("content")) {
                return first.get("message").get("content").asText("");
            }
            if (first.has("text")) {
                return first.get("text").asText("");
            }
        }
        return "";
    }
}
