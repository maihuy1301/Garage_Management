package com.garage.repository;

import com.garage.entity.GiaoDichKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiaoDichKhoRepository extends JpaRepository<GiaoDichKho, Integer> {

    List<GiaoDichKho> findByChiNhanhMaChiNhanh(Integer maChiNhanh);

    List<GiaoDichKho> findByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);
}
