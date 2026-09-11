package com.garage.repository;

import com.garage.entity.HinhAnhSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HinhAnhSuaChuaRepository extends JpaRepository<HinhAnhSuaChua, Integer> {

    List<HinhAnhSuaChua> findByPhieuSuaChuaMaPhieuSuaChuaOrderByThoiGianChupDesc(Integer maPhieuSuaChua);

    List<HinhAnhSuaChua> findByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);
}
