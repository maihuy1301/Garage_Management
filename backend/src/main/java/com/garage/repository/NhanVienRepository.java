package com.garage.repository;

import com.garage.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    Optional<NhanVien> findByNguoiDungMaNguoiDung(Integer maNguoiDung);
    Optional<NhanVien> findByMaNhanVienCode(String maNhanVienCode);
    List<NhanVien> findByChiNhanhMaChiNhanh(Integer maChiNhanh);
    boolean existsByMaNhanVienCode(String maNhanVienCode);
    boolean existsByNguoiDungMaNguoiDung(Integer maNguoiDung);
}
