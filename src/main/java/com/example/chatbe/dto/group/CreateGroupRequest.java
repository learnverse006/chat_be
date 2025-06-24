package com.example.chatbe.dto.group;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {
    private String name;
    private Long creatorId;
    private List<Long> memberIds;
}
