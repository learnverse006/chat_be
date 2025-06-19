package com.example.authbackend.entity;
import com.example.authbackend.enums.ConversationType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ConversationType type;

    @OneToMany(mappedBy = "conversation")
    private List<Message> messages;
}
