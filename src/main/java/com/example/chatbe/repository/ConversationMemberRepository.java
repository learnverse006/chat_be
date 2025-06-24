package com.example.chatbe.repository;

import com.example.chatbe.entity.Conversation;
import com.example.chatbe.entity.ConversationMember;
import com.example.chatbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    List<ConversationMember> findAllByConversation(Conversation conversation);
    List<ConversationMember> findAllByUser(User user);

//find all conversation ids for a user
    @Query("SELECT cm.conversation.id from ConversationMember  cm where cm.user.id = :userId")
    List<Long> findAllConversationIdsByUserId(@Param("userId") Long userId);

    List<ConversationMember> findByConversation(Conversation conversation);

    boolean existsByConversationAndUser(Conversation group, User user);

    List<ConversationMember> findByUserId(Long userId);

    Optional<ConversationMember> findByConversationIdAndUserId(Long groupId, Long userId);
}
