package com.garage.repository;

import com.garage.entity.ChiNhanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiNhanhRepository extends JpaRepository<ChiNhanh, Integer> {
    List<ChiNhanh> findByTrangThaiTrue();
}
