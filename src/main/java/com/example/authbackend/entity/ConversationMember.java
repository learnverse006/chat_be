package com.example.authbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Conversation conversation;

    @ManyToOne
    private User user;

    // Getter/Setter
}