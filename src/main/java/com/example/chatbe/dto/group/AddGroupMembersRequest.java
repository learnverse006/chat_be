package com.example.chatbe.dto.group;

import lombok.Data;

import java.util.List;

@Data
public class AddGroupMembersRequest {
    private List<Long> userIds;
}