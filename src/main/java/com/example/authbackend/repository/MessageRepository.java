package com.example.authbackend.repository;

import com.example.authbackend.entity.Conversation;
import com.example.authbackend.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// get latest message in each conversation
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByTimestampDesc(Conversation conversation, Pageable pageable);

    @Query(value = """
    SELECT DISTINCT ON (m.conversation_id) m.*
    FROM message m
    WHERE m.conversation_id IN (:conversationIds)
    ORDER BY m.conversation_id,           -- nhóm theo conversation
             m.timestamp DESC,            -- tin mới nhất
             m.id DESC                    -- ràng buộc duy nhất
""", nativeQuery = true)
    List<Message> findLatestMessages(@Param("conversationIds") List<Long> conversationIds);
}
