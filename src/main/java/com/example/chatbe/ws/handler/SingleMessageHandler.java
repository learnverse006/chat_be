package com.example.chatbe.ws.handler;

import com.example.chatbe.dto.ws.MessagePayLoad;
import com.example.chatbe.enums.ConversationType;
import com.example.chatbe.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SingleMessageHandler implements MessageHandler{

    private final MessageService messageService;

    @Override
    public boolean supports(ConversationType conversationType) {
        return ConversationType.SINGLE.equals(conversationType);
    }

    @Override
    public void handleMessage(MessagePayLoad messagePayLoad) throws IOException {
        // Handle single message logic here
        messageService.handleSingleMessage(messagePayLoad);
    }
}
