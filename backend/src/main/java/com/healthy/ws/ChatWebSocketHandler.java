package com.healthy.ws;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthy.entity.AppointmentRecord;
import com.healthy.mapper.AppointmentMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final AppointmentMapper appointmentMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatWebSocketHandler(AppointmentMapper appointmentMapper) {
        this.appointmentMapper = appointmentMapper;
    }

    // map userId -> session
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri != null) {
            String q = uri.getQuery();
            if (q != null && q.contains("userId=")) {
                String[] parts = q.split("userId=");
                String userId = parts[1].split("&")[0];
                sessions.put(userId, session);
                sessionUserMap.put(session.getId(), userId);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String to = null;
        String from = sessionUserMap.get(session.getId());
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root.has("toUserId")) to = root.get("toUserId").asText();
            if (root.has("fromUserId")) from = root.get("fromUserId").asText(from);
        } catch (Exception ignored) {
        }

        if (from == null || to == null) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage("{\"error\":\"invalid payload\"}"));
            }
            return;
        }

        if (!canChat(from, to)) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage("{\"error\":\"chat not allowed: appointment not confirmed\"}"));
            }
            return;
        }

        if (to != null && sessions.containsKey(to)) {
            WebSocketSession target = sessions.get(to);
            if (target != null && target.isOpen()) {
                target.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.values().removeIf(s -> s.getId().equals(session.getId()));
        sessionUserMap.remove(session.getId());
    }

    private boolean canChat(String fromUserId, String toUserId) {
        try {
            long a = Long.parseLong(fromUserId);
            long b = Long.parseLong(toUserId);
            long count = appointmentMapper.selectCount(new QueryWrapper<AppointmentRecord>()
                    .eq("status", 1)
                    .and(q -> q
                            .and(x -> x.eq("user_id", a).eq("counselor_id", b))
                            .or(x -> x.eq("user_id", b).eq("counselor_id", a))
                    ));
            return count > 0;
        } catch (Exception ex) {
            return false;
        }
    }
}
