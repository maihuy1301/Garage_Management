package com.garage.repository;

import com.garage.entity.HinhAnhXe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HinhAnhXeRepository extends JpaRepository<HinhAnhXe, Integer> {
    Optional<HinhAnhXe> findByXeMaXe(Integer maXe);
}
