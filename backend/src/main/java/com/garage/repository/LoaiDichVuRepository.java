package com.garage.repository;

import com.garage.entity.LoaiDichVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoaiDichVuRepository extends JpaRepository<LoaiDichVu, Integer> {

    List<LoaiDichVu> findByTrangThaiTrue();

    boolean existsByTenLoaiIgnoreCase(String tenLoai);

    boolean existsByTenLoaiIgnoreCaseAndMaLoaiDichVuNot(String tenLoai, Integer maLoaiDichVu);
}
