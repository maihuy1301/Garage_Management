package com.garage.repository;

import com.garage.entity.PhuTung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhuTungRepository extends JpaRepository<PhuTung, Integer> {

    List<PhuTung> findByTrangThaiTrue();

    List<PhuTung> findByChiNhanhMaChiNhanhAndTrangThaiTrue(Integer maChiNhanh);

    List<PhuTung> findByChiNhanhMaChiNhanh(Integer maChiNhanh);

    Optional<PhuTung> findByMaPhuTungCode(String maPhuTungCode);

    boolean existsByMaPhuTungCode(String maPhuTungCode);
}
