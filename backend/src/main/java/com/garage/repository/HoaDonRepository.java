package com.garage.repository;

import com.garage.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select h from HoaDon h where h.maHoaDon = :id")
    Optional<HoaDon> findForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);

    Optional<HoaDon> findByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    boolean existsByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    List<HoaDon> findByChiNhanhMaChiNhanh(Integer maChiNhanh);

    boolean existsByChiNhanhMaChiNhanh(Integer maChiNhanh);

    List<HoaDon> findByKhachHangMaKhachHang(Integer maKhachHang);
}
