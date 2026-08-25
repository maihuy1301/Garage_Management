package com.garage.repository;

import com.garage.entity.BaoGiaPhatSinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaoGiaPhatSinhRepository extends JpaRepository<BaoGiaPhatSinh, Integer> {

    List<BaoGiaPhatSinh> findByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    List<BaoGiaPhatSinh> findByPhieuSuaChuaChiNhanhMaChiNhanh(Integer maChiNhanh);
}
