package com.garage.repository;

import com.garage.entity.SupportConversation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface SupportConversationRepository extends JpaRepository<SupportConversation, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from NguoiDung u where u.maNguoiDung = :id")
    Optional<com.garage.entity.NguoiDung> lockCustomer(Integer id);

    @Query("select distinct n.nguoiDung.tenDangNhap from NhanVien n, NguoiDungVaiTro r " +
            "where n.nguoiDung = r.nguoiDung and n.chiNhanh.maChiNhanh = :branch " +
            "and n.trangThai = true and n.nguoiDung.trangThai = true " +
            "and r.vaiTro.tenVaiTro in ('ROLE_FRONT_DESK', 'ROLE_MANAGER', 'ROLE_ADMIN')")
    List<String> supportUsernames(Integer branch);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from SupportConversation c where c.id = :id")
    Optional<SupportConversation> lockById(Integer id);
    Optional<SupportConversation> findByCustomerMaNguoiDungAndBranchMaChiNhanh(Integer customer, Integer branch);
    List<SupportConversation> findByCustomerMaNguoiDungOrderByUpdatedAtDesc(Integer customer);
    List<SupportConversation> findByBranchMaChiNhanhOrderByUpdatedAtDesc(Integer branch);
    List<SupportConversation> findAllByOrderByUpdatedAtDesc();
}
