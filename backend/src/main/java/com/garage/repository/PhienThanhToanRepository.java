package com.garage.repository;

import com.garage.entity.PhienThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PhienThanhToanRepository extends JpaRepository<PhienThanhToan, String> {
    Optional<PhienThanhToan> findByContent(String content);
    @org.springframework.data.jpa.repository.Query("select s.invoice.maHoaDon from PhienThanhToan s where s.content = :content")
    Optional<Integer> findInvoiceIdByContent(@org.springframework.data.repository.query.Param("content") String content);
    List<PhienThanhToan> findByInvoiceMaHoaDonOrderByCreatedAtDesc(Integer invoiceId);
}
