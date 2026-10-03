package com.garage.repository;

import com.garage.entity.SupportMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    Optional<SupportMessage> findByConversationIdAndClientId(Integer conversation, String clientId);
    @Query("select m from SupportMessage m where m.conversation.id = :id and m.id < :before order by m.id desc")
    List<SupportMessage> history(Integer id, Long before, Pageable pageable);
    @Query("select count(m) from SupportMessage m where m.conversation.id = :id and m.id > :cursor and (m.sender is null or m.sender.maNguoiDung <> :userId)")
    long unread(Integer id, Long cursor, Integer userId);
    @Modifying
    @Query("delete from SupportMessage m where m.conversation.id = :id")
    void deleteByConversationId(Integer id);
}
