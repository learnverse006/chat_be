package com.example.chatbe.ws.handler;

import com.example.chatbe.dto.ws.MessagePayLoad;
import com.example.chatbe.enums.ConversationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageDispatcher {

    private final List<MessageHandler> handlers;

    public void dispatch(MessagePayLoad payload) throws IOException {
        ConversationType type = payload.getConversationType();

        for (MessageHandler handler : handlers) {
            if (handler.supports(type)) {
                handler.handleMessage(payload);
                return;
            }
        }

        throw new IllegalArgumentException("Unsupported conversation type: " + type);

    }
}
