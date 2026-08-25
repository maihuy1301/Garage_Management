package com.garage.repository;

import com.garage.entity.PhieuSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuSuaChuaRepository extends JpaRepository<PhieuSuaChua, Integer> {

    /** Lấy danh sách phiếu sửa chữa theo chi nhánh */
    List<PhieuSuaChua> findByChiNhanhMaChiNhanh(Integer maChiNhanh);

    /** Lấy phiếu sửa chữa theo phiếu tiếp nhận */
    Optional<PhieuSuaChua> findByPhieuTiepNhanMaTiepNhan(Integer maTiepNhan);

    /** Kiểm tra xem phiếu tiếp nhận đã có phiếu sửa chữa hay chưa (idempotency check) */
    boolean existsByPhieuTiepNhanMaTiepNhan(Integer maTiepNhan);
}
