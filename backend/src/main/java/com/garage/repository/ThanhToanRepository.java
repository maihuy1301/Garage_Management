package com.garage.repository;

import com.garage.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {

    List<ThanhToan> findByHoaDonMaHoaDon(Integer maHoaDon);

    boolean existsByMaGiaoDichAndTrangThai(String maGiaoDich, String trangThai);
}
