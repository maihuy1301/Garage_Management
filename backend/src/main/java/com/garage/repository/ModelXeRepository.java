package com.garage.repository;

import com.garage.entity.ModelXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelXeRepository extends JpaRepository<ModelXe, Integer> {

    List<ModelXe> findByHangXeMaHangXeAndTrangThaiTrue(Integer maHangXe);

    List<ModelXe> findByHangXeMaHangXe(Integer maHangXe);

    Optional<ModelXe> findByHangXeMaHangXeAndTenModelIgnoreCase(Integer maHangXe, String tenModel);

    boolean existsByHangXeMaHangXeAndTenModelIgnoreCase(Integer maHangXe, String tenModel);
}
