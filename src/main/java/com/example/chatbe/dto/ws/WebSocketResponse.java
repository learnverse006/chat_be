package com.example.chatbe.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebSocketResponse {
    private String type;
    private Object data;
}