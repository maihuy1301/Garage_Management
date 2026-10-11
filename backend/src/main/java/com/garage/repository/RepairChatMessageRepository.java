package com.garage.repository;

import com.garage.entity.RepairChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface RepairChatMessageRepository extends JpaRepository<RepairChatMessage, Long> {
    @Query("select m from RepairChatMessage m join fetch m.sender where m.receptionId = :room and m.id < :before order by m.id desc")
    List<RepairChatMessage> older(@Param("room") Integer room, @Param("before") Long before, Pageable page);
    @Query("select m from RepairChatMessage m join fetch m.sender where m.receptionId = :room and m.id > :after order by m.id asc")
    List<RepairChatMessage> newer(@Param("room") Integer room, @Param("after") Long after, Pageable page);
    Optional<RepairChatMessage> findByReceptionIdAndSenderMaNguoiDungAndClientId(Integer room, Integer sender, String clientId);
    @Modifying
    @Query("delete from RepairChatMessage m where m.receptionId = :room")
    int purge(@Param("room") Integer room);
}
