package com.garage.repository;

import com.garage.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    Optional<NhanVien> findByNguoiDungMaNguoiDung(Integer maNguoiDung);
    List<NhanVien> findByChiNhanhMaChiNhanh(Integer maChiNhanh);
    boolean existsByNguoiDungMaNguoiDung(Integer maNguoiDung);
}
