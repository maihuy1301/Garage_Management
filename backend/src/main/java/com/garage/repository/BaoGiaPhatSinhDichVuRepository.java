package com.garage.repository;

import com.garage.entity.BaoGiaPhatSinhDichVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaoGiaPhatSinhDichVuRepository extends JpaRepository<BaoGiaPhatSinhDichVu, Integer> {

    List<BaoGiaPhatSinhDichVu> findByBaoGiaPhatSinhMaBaoGia(Integer maBaoGia);
}
