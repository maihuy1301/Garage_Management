package com.garage.repository;

import com.garage.entity.PhieuSuaChuaPhuTung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuSuaChuaPhuTungRepository extends JpaRepository<PhieuSuaChuaPhuTung, Integer> {

    List<PhieuSuaChuaPhuTung> findByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    Optional<PhieuSuaChuaPhuTung> findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(Integer maChiTiet, Integer maPhieuSuaChua);

    boolean existsByPhieuSuaChuaMaPhieuSuaChuaAndPhuTungMaPhuTung(Integer maPhieuSuaChua, Integer maPhuTung);

    int countByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);
}
