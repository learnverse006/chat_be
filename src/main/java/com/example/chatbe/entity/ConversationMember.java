package com.example.chatbe.entity;

import com.example.chatbe.enums.GroupRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "conversation_members")
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Conversation conversation;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private GroupRole role;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    // Getter/Setter
}