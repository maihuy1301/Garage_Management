package com.garage.repository;

import com.garage.entity.ThietBiPush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ThietBiPushRepository extends JpaRepository<ThietBiPush, String> {
    // SQL Server range lock also serializes concurrent registration of a new token.
    @Query(value = "SELECT * FROM ThietBiPush WITH (UPDLOCK, HOLDLOCK) WHERE TokenHash = :hash", nativeQuery = true)
    Optional<ThietBiPush> findForUpdate(@Param("hash") String hash);
    List<ThietBiPush> findByMaNguoiDungAndCapNhatLucAfter(Integer userId, LocalDateTime cutoff);
    @org.springframework.data.jpa.repository.Modifying
    @Query("delete from ThietBiPush d where d.tokenHash = :hash and d.maNguoiDung = :userId")
    void deleteByTokenHashAndMaNguoiDung(@Param("hash") String hash, @Param("userId") Integer userId);
}
