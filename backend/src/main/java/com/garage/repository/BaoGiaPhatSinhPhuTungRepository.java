package com.garage.repository;

import com.garage.entity.BaoGiaPhatSinhPhuTung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaoGiaPhatSinhPhuTungRepository extends JpaRepository<BaoGiaPhatSinhPhuTung, Integer> {

    List<BaoGiaPhatSinhPhuTung> findByBaoGiaPhatSinhMaBaoGia(Integer maBaoGia);
}
