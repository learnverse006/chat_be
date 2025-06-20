package com.example.authbackend.repository;

import com.example.authbackend.entity.Conversation;
import com.example.authbackend.entity.ConversationMember;
import com.example.authbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    List<ConversationMember> findAllByConversation(Conversation conversation);
    List<ConversationMember> findAllByUser(User user);

//find all conversation ids for a user
    @Query("SELECT cm.conversation.id from ConversationMember  cm where cm.user.id = :userId")
    List<Long> findAllConversationIdsByUserId(@Param("userId") Long userId);
}
