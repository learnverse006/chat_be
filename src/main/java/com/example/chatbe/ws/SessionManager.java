package com.example.chatbe.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    // userId → WebSocketSession
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(Long userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void removeSession(Long userId) {
        sessions.remove(userId);
    }

    public boolean isOnline(Long userId) {
        return sessions.containsKey(userId);
    }
    public WebSocketSession getSession(Long userId) {
        return sessions.get(userId);
    }

    public Map<Long, WebSocketSession> getAllSessions() {
        return sessions;
    }

}
