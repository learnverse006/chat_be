package com.example.chatbe.ws.handler;

import com.example.chatbe.dto.ws.MessagePayLoad;
import com.example.chatbe.enums.ConversationType;

public interface MessageHandler {
    boolean supports(ConversationType conversationType);

    void handleMessage(MessagePayLoad messagePayLoad);
}
