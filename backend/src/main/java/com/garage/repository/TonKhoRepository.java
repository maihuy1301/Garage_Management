package com.garage.repository;

import com.garage.entity.TonKho;
import com.garage.entity.TonKhoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TonKhoRepository extends JpaRepository<TonKho, TonKhoId> {

    List<TonKho> findByIdMaChiNhanh(Integer maChiNhanh);

    Optional<TonKho> findByIdMaChiNhanhAndIdMaPhuTung(Integer maChiNhanh, Integer maPhuTung);

    List<TonKho> findByIdMaChiNhanhAndPhuTungTrangThaiTrue(Integer maChiNhanh);
}
