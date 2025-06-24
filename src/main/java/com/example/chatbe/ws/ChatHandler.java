package com.example.chatbe.ws;

import com.example.chatbe.dto.ws.MessagePayLoad;
import com.example.chatbe.dto.ws.WebSocketResponse;
import com.example.chatbe.ws.handler.MessageDispatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.springframework.web.socket.TextMessage;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final MessageDispatcher messageDispatcher;
    private final SessionManager sessionManager;


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String text = message.getPayload();
            MessagePayLoad payload = objectMapper.readValue(text, MessagePayLoad.class);
            messageDispatcher.dispatch(payload);

        } catch (Exception e) {
            e.printStackTrace(); // Có thể log sau
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userIdStr = session.getUri().getQuery().replace("userId=", "");
        Long userId = Long.parseLong(userIdStr);

        sessionManager.registerSession(userId, session);

        try {
            WebSocketResponse response = new WebSocketResponse("ONLINE_CONFIRM", userId);
            String json = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userIdStr = session.getUri().getQuery().replace("userId=", "");
        Long userId = Long.parseLong(userIdStr);
        sessionManager.removeSession(userId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        exception.printStackTrace();
    }
}
