package com.example.chatbe.dto.group;

import lombok.Data;

@Data
public class ChangeAdminRequest {
    private Long currentAdminId;
    private Long newAdminId;
}
