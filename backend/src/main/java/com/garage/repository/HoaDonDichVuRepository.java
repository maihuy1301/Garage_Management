package com.garage.repository;

import com.garage.entity.HoaDonDichVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonDichVuRepository extends JpaRepository<HoaDonDichVu, Integer> {

    List<HoaDonDichVu> findByHoaDonMaHoaDon(Integer maHoaDon);
}
