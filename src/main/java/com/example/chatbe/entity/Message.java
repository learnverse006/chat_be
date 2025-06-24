package com.example.chatbe.entity;

import com.example.chatbe.enums.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private User sender;

    @ManyToOne
    private Conversation conversation;


    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column(length = 512)
    private String content;

    private LocalDateTime timestamp;

}
