package com.garage.repository;

import com.garage.entity.HoaDonPhuTung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonPhuTungRepository extends JpaRepository<HoaDonPhuTung, Integer> {

    List<HoaDonPhuTung> findByHoaDonMaHoaDon(Integer maHoaDon);
}
