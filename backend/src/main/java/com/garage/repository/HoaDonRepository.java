package com.garage.repository;

import com.garage.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Optional<HoaDon> findByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    boolean existsByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    List<HoaDon> findByChiNhanhMaChiNhanh(Integer maChiNhanh);

    List<HoaDon> findByKhachHangMaKhachHang(Integer maKhachHang);
}
