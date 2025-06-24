package com.example.chatbe.repository;

import com.example.chatbe.entity.Conversation;
import com.example.chatbe.enums.ConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
    SELECT c
    FROM Conversation c
    WHERE c.type = :type
      AND EXISTS (SELECT m1 FROM ConversationMember m1 WHERE m1.conversation = c AND m1.user.id = :user1Id)
      AND EXISTS (SELECT m2 FROM ConversationMember m2 WHERE m2.conversation = c AND m2.user.id = :user2Id)
    ORDER BY c.id 
        """)
    default Optional<Conversation> findConversationBetweenUsers(
            Long user1Id, Long user2Id, ConversationType type) {
        return findConversationBetweenUsersPage(user1Id, user2Id, type, PageRequest.of(0,1))
                .stream().findFirst();
    }



    @Query(
            value = """
        SELECT c
        FROM Conversation c
        WHERE c.type = :type
          AND EXISTS (
              SELECT m1 FROM ConversationMember m1
              WHERE m1.conversation = c AND m1.user.id = :user1Id
          )
          AND EXISTS (
              SELECT m2 FROM ConversationMember m2
              WHERE m2.conversation = c AND m2.user.id = :user2Id
          )
        ORDER BY c.id
    """,
            countQuery = """
        SELECT COUNT(c)
        FROM Conversation c
        WHERE c.type = :type
          AND EXISTS (
              SELECT m1 FROM ConversationMember m1
              WHERE m1.conversation = c AND m1.user.id = :user1Id
          )
          AND EXISTS (
              SELECT m2 FROM ConversationMember m2
              WHERE m2.conversation = c AND m2.user.id = :user2Id
          )
    """
    )
    Page<Conversation> findConversationBetweenUsersPage(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id,
            @Param("type") ConversationType type,
            Pageable pageable);

}
