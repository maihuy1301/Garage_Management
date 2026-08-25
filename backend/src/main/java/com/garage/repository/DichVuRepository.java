package com.garage.repository;

import com.garage.entity.DichVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DichVuRepository extends JpaRepository<DichVu, Integer> {
    List<DichVu> findByTrangThaiTrue();
}
