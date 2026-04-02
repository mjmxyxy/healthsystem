package com.healthy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AiController {

    private final ObjectMapper json = new ObjectMapper();

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        try {
            String message = (String) body.getOrDefault("message", "");
            if (message == null || message.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "message required"));

            String apiKey = System.getenv().getOrDefault("OPENAI_API_KEY", "");
            String apiUrl = System.getenv().getOrDefault("OPENAI_API_URL", "https://api.openai.com/v1/chat/completions");
            String model = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-3.5-turbo");

            if (apiKey.isEmpty()) {
                // fallback for demo without paid API key
                String reply = "我在这里陪你。听起来你现在有些压力，可以先做三次缓慢深呼吸，然后把最困扰你的一件事告诉我，我们一步一步来。";
                return ResponseEntity.ok(Map.of("reply", reply));
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

            String reqBody = json.writeValueAsString(payload);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 400) {
                return ResponseEntity.status(resp.statusCode()).body(Map.of("error", "model api error", "detail", resp.body()));
            }

            JsonNode root = json.readTree(resp.body());
            String reply = "";
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                JsonNode first = root.get("choices").get(0);
                if (first.has("message") && first.get("message").has("content")) {
                    reply = first.get("message").get("content").asText();
                } else if (first.has("text")) {
                    reply = first.get("text").asText();
                }
            }
            if (reply.isEmpty()) reply = "(模型未返回可用文本)";
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "internal", "detail", ex.getMessage()));
        }
    }
}
