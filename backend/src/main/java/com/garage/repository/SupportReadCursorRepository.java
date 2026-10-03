package com.garage.repository;

import com.garage.entity.SupportReadCursor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SupportReadCursorRepository extends JpaRepository<SupportReadCursor, Long> {
    Optional<SupportReadCursor> findByConversationIdAndUserId(Integer conversation, Integer user);
    @Modifying
    @Query("delete from SupportReadCursor c where c.conversationId = :id")
    void deleteByConversationId(Integer id);
}
