package com.garage.repository;

import com.garage.entity.PhieuTiepNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuTiepNhanRepository extends JpaRepository<PhieuTiepNhan, Integer> {

    /** Lấy danh sách phiếu tiếp nhận theo chi nhánh */
    List<PhieuTiepNhan> findByChiNhanhMaChiNhanh(Integer maChiNhanh);

    /** Lấy danh sách phiếu tiếp nhận theo xe */
    List<PhieuTiepNhan> findByXeMaXe(Integer maXe);

    /** Kiểm tra xem lịch hẹn đã có phiếu tiếp nhận hay chưa (idempotency check) */
    boolean existsByDatLichMaDatLich(Integer maDatLich);

    /** Tìm phiếu tiếp nhận theo lịch hẹn */
    Optional<PhieuTiepNhan> findByDatLichMaDatLich(Integer maDatLich);
}
