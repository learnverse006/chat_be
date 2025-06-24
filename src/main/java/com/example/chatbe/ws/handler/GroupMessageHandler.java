package com.example.chatbe.ws.handler;

import com.example.chatbe.dto.ws.MessagePayLoad;
import com.example.chatbe.enums.ConversationType;
import com.example.chatbe.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupMessageHandler implements MessageHandler {

    private final MessageService messageService;

    @Override
    public boolean supports(ConversationType conversationType) {
        return ConversationType.GROUP.equals(conversationType);
    }

    @Override
    public void handleMessage(MessagePayLoad messagePayLoad) {
        messageService.handleGroupMessage(messagePayLoad);
    }
}
