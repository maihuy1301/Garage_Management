package com.garage.repository;

import com.garage.entity.HangXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HangXeRepository extends JpaRepository<HangXe, Integer> {

    List<HangXe> findByTrangThaiTrue();

    Optional<HangXe> findByTenHangXeIgnoreCase(String tenHangXe);

    boolean existsByTenHangXeIgnoreCase(String tenHangXe);
}
