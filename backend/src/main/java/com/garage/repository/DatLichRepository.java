package com.garage.repository;

import com.garage.entity.DatLich;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatLichRepository extends JpaRepository<DatLich, Integer> {

    /** Lấy danh sách lịch hẹn của một khách hàng */
    List<DatLich> findByKhachHangMaKhachHang(Integer maKhachHang);

    /** Lấy chi tiết lịch hẹn theo ID và đảm bảo thuộc khách hàng đó */
    Optional<DatLich> findByMaDatLichAndKhachHangMaKhachHang(Integer maDatLich, Integer maKhachHang);

    /** Lấy danh sách lịch hẹn thuộc một chi nhánh */
    List<DatLich> findByChiNhanhMaChiNhanh(Integer maChiNhanh);

    /** Kiểm tra trùng lịch hẹn cho xe tại cùng thời điểm (loại trừ các trạng thái đã hủy/kết thúc) */
    boolean existsByXeMaXeAndThoiGianHenAndTrangThaiNotIn(Integer maXe, LocalDateTime thoiGianHen, Collection<String> excludedStatuses);
}
