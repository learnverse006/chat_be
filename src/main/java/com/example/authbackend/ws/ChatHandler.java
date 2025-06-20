package com.example.authbackend.ws;

import com.example.authbackend.dto.MessagePayLoad;
import com.example.authbackend.dto.response.MessageResponse;
import com.example.authbackend.entity.Message;
import com.example.authbackend.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private final ObjectMapper mapper;
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public ChatHandler(MessageService messageService, ObjectMapper mapper) {
        this.messageService = messageService;
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromQuery(session);
        if (userId != null) {
            sessions.put(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        MessagePayLoad payload = mapper.readValue(textMessage.getPayload(), MessagePayLoad.class);
        Long senderId = getUserIdFromQuery(session);
        payload.setSenderId(senderId);  // Ghi đè

        String action = payload.getAction();
        if ("history".equalsIgnoreCase(action)) {
            System.out.println(">>> [ChatHandler] Loading history for " + payload.getSenderId() + " - " + payload.getReceiverId());

            var messages = messageService.loadConversationHistory(
                    payload.getSenderId(), payload.getReceiverId(), 20
            );

            var responses = new ArrayList<MessageResponse>();
            for (var message : messages) {
                String decrypted = messageService.decrypt(message);
                responses.add(toResponse(message, decrypted));
            }

            var responseWrapper = new HashMap<String, Object>();
            responseWrapper.put("type", "history");
            responseWrapper.put("messages", responses);

            session.sendMessage(new TextMessage(mapper.writeValueAsString(responseWrapper)));
            return;
        }

        //save message
        else if ("send".equalsIgnoreCase(action)) {
            var saved = messageService.sendMessage(
                    payload.getSenderId(),
                    payload.getReceiverId(),
                    payload.getType(),
                    payload.getContent()
            );

            //send
            String decryptedContent = messageService.decrypt(saved);
            var response = toResponse(saved, decryptedContent);

            WebSocketSession receiverSession = sessions.get(payload.getReceiverId());
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(mapper.writeValueAsString(response)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        sessions.values().removeIf(s -> s.equals(session));
    }

    private Long getUserIdFromQuery(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            return Long.parseLong(query.substring(7));
        }
        return null;
    }

    public MessageResponse toResponse(Message message, String decryptedContent) {
        MessageResponse res = new MessageResponse();
        res.setId(message.getId());
        res.setSenderId(message.getSender().getId());
        res.setConversationId(message.getConversation().getId());
        res.setContent(decryptedContent);
        res.setType(message.getType());
        res.setTimestamp(message.getTimestamp().toString());
        return res;
    }

}






//{
//        "action": "history",
//        "senderId": 1,
//        "receiverId": 2,
//        "type": "TEXT",
//        "content": "hello from Postman"
//        }
