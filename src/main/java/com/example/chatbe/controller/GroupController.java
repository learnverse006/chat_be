package com.example.chatbe.controller;

import com.example.chatbe.dto.group.AddGroupMembersRequest;
import com.example.chatbe.dto.group.ChangeAdminRequest;
import com.example.chatbe.dto.group.CreateGroupRequest;
import com.example.chatbe.dto.group.GroupInfoResponse;
import com.example.chatbe.entity.Conversation;
import com.example.chatbe.entity.ConversationMember;
import com.example.chatbe.entity.User;
import com.example.chatbe.enums.ConversationType;
import com.example.chatbe.enums.GroupRole;
import com.example.chatbe.repository.ConversationMemberRepository;
import com.example.chatbe.repository.ConversationRepository;
import com.example.chatbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> createGroup(@RequestBody CreateGroupRequest request) {
        Conversation group = new Conversation();
        group.setType(ConversationType.GROUP);
        group.setName(request.getName());
        conversationRepository.save(group);

        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        ConversationMember admin = new ConversationMember();
        admin.setConversation(group);
        admin.setUser(creator);
        admin.setRole(GroupRole.ADMIN);
        admin.setLastReadAt(LocalDateTime.now());

        List<ConversationMember> members = new ArrayList<>();
        members.add(admin);

        for (Long id : request.getMemberIds()) {
            if (Objects.equals(id, request.getCreatorId())) continue;

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ConversationMember member = new ConversationMember();
            member.setConversation(group);
            member.setUser(user);
            member.setRole(GroupRole.MEMBER);
            member.setLastReadAt(LocalDateTime.now());
            members.add(member);
        }

        conversationMemberRepository.saveAll(members);

        return ResponseEntity.ok("Group created with ID: " + group.getId());
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<String> addMembers(
            @PathVariable Long groupId,
            @RequestBody AddGroupMembersRequest request
    ){
        Conversation group = conversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (group.getType() != ConversationType.GROUP) {
            return ResponseEntity.badRequest().body("Not a group conversation");
        }

        List<ConversationMember> newMembers = new ArrayList<>();

        for (Long userId : request.getUserIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean alreadyMember = conversationMemberRepository
                    .existsByConversationAndUser(group, user);
            if (alreadyMember) continue;

            ConversationMember member = new ConversationMember();
            member.setConversation(group);
            member.setUser(user);
            member.setRole(GroupRole.MEMBER);
            member.setLastReadAt(LocalDateTime.now());
            newMembers.add(member);
        }

        conversationMemberRepository.saveAll(newMembers);
        return ResponseEntity.ok("Members added");
    }

    @GetMapping
    public ResponseEntity<List<GroupInfoResponse>> getGroups(@RequestParam Long userId) {
        List<ConversationMember> memberships = conversationMemberRepository.findByUserId(userId);

        List<GroupInfoResponse> groups = memberships.stream()
                .filter(cm -> cm.getConversation().getType() == ConversationType.GROUP)
                .map(cm -> {
                    GroupInfoResponse dto = new GroupInfoResponse();
                    dto.setId(cm.getConversation().getId());
                    dto.setName(cm.getConversation().getName());
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(groups);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<String> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        Conversation group = conversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (group.getType() != ConversationType.GROUP) {
            return ResponseEntity.badRequest().body("Not a group conversation");
        }

        Optional<ConversationMember> memberOpt = conversationMemberRepository
                .findByConversationIdAndUserId(groupId, userId);

        if (memberOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User is not a member of this group");
        }

        conversationMemberRepository.delete(memberOpt.get());

        return ResponseEntity.ok("User removed from group");
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<String> leaveGroup(
            @PathVariable Long groupId,
            @RequestParam Long userId
    ) {
        Conversation group = conversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (group.getType() != ConversationType.GROUP) {
            return ResponseEntity.badRequest().body("Not a group conversation");
        }

        Optional<ConversationMember> memberOpt =
                conversationMemberRepository.findByConversationIdAndUserId(groupId, userId);

        if (memberOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("You are not a member of this group");
        }

        conversationMemberRepository.delete(memberOpt.get());
        return ResponseEntity.ok("You have left the group");
    }

    @PutMapping("/{groupId}/change-admin")
    public ResponseEntity<String> changeGroupAdmin(
            @PathVariable Long groupId,
            @RequestBody ChangeAdminRequest request
    ) {
        Conversation group = conversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (group.getType() != ConversationType.GROUP) {
            return ResponseEntity.badRequest().body("Not a group conversation");
        }

        Optional<ConversationMember> currentAdminOpt =
                conversationMemberRepository.findByConversationIdAndUserId(groupId, request.getCurrentAdminId());

        Optional<ConversationMember> newAdminOpt =
                conversationMemberRepository.findByConversationIdAndUserId(groupId, request.getNewAdminId());

        if (currentAdminOpt.isEmpty() || newAdminOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("One or both users are not in the group");
        }

        ConversationMember currentAdmin = currentAdminOpt.get();
        if (currentAdmin.getRole() != GroupRole.ADMIN) {
            return ResponseEntity.badRequest().body("Only current ADMIN can transfer admin role");
        }

        currentAdmin.setRole(GroupRole.MEMBER);
        currentAdmin.setLastReadAt(LocalDateTime.now());
        newAdminOpt.get().setRole(GroupRole.ADMIN);
        newAdminOpt.get().setLastReadAt(LocalDateTime.now());

        conversationMemberRepository.saveAll(List.of(currentAdmin, newAdminOpt.get()));
        return ResponseEntity.ok("Admin role transferred successfully");
    }

}