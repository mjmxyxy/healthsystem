package com.healthy.ws;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    // map userId -> session
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri != null) {
            String q = uri.getQuery();
            if (q != null && q.contains("userId=")) {
                String[] parts = q.split("userId=");
                String userId = parts[1].split("&")[0];
                sessions.put(userId, session);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // message expected JSON: {toUserId: "123", text: "hello"}
        String payload = message.getPayload();
        // simple parsing (no dependency)
        String to = null;
        String text = payload;
        try {
            if (payload.contains("toUserId")) {
                int i = payload.indexOf("toUserId");
                int c = payload.indexOf(':', i);
                int start = payload.indexOf('"', c) + 1;
                int end = payload.indexOf('"', start);
                to = payload.substring(start, end);
            }
            if (payload.contains("text")) {
                int i = payload.indexOf("text");
                int c = payload.indexOf(':', i);
                int start = payload.indexOf('"', c) + 1;
                int end = payload.indexOf('"', start);
                text = payload.substring(start, end);
            }
        } catch (Exception ignored) {}

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
    }
}
