package com.example.chatbe.ws.handler;

import com.example.chatbe.dto.ws.MessagePayLoad;
import com.example.chatbe.enums.ConversationType;

import java.io.IOException;

public interface MessageHandler {
    boolean supports(ConversationType conversationType);

    void handleMessage(MessagePayLoad messagePayLoad) throws IOException;
}
