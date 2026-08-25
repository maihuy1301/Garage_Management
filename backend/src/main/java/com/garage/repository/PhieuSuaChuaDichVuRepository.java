package com.garage.repository;

import com.garage.entity.PhieuSuaChuaDichVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuSuaChuaDichVuRepository extends JpaRepository<PhieuSuaChuaDichVu, Integer> {

    /** Lấy danh sách hạng mục dịch vụ của một phiếu sửa chữa */
    List<PhieuSuaChuaDichVu> findByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    /** Kiểm tra xem dịch vụ đã có trong phiếu sửa chữa chưa (duplicate check) */
    boolean existsByPhieuSuaChuaMaPhieuSuaChuaAndDichVuMaDichVu(Integer maPhieuSuaChua, Integer maDichVu);

    /** Lấy chi tiết hạng mục dịch vụ thuộc phiếu sửa chữa */
    Optional<PhieuSuaChuaDichVu> findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(Integer maChiTiet, Integer maPhieuSuaChua);
}
