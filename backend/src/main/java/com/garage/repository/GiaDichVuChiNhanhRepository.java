package com.garage.repository;

import com.garage.entity.GiaDichVuChiNhanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GiaDichVuChiNhanhRepository extends JpaRepository<GiaDichVuChiNhanh, Integer> {

    /** Lấy đơn giá dịch vụ đang áp dụng tại một chi nhánh */
    List<GiaDichVuChiNhanh> findByChiNhanhMaChiNhanhAndDichVuMaDichVuAndTrangThaiTrue(Integer maChiNhanh, Integer maDichVu);
}
