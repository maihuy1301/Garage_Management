package com.garage.repository;

import com.garage.entity.ChiNhanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiNhanhRepository extends JpaRepository<ChiNhanh, Integer> {
    Optional<ChiNhanh> findByMaChiNhanhCode(String maChiNhanhCode);
    List<ChiNhanh> findByTrangThaiTrue();
}

