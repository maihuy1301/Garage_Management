package com.garage.repository;

import com.garage.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    Optional<KhachHang> findByNguoiDungMaNguoiDung(Integer maNguoiDung);
    Optional<KhachHang> findByMaKhachHangCode(String maKhachHangCode);
    boolean existsByMaKhachHangCode(String maKhachHangCode);
    boolean existsByNguoiDungMaNguoiDung(Integer maNguoiDung);
}
