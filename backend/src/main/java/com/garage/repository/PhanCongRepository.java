package com.garage.repository;

import com.garage.entity.PhanCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhanCongRepository extends JpaRepository<PhanCong, Integer> {

    /** Lấy danh sách phân công kỹ thuật viên theo phiếu sửa chữa */
    List<PhanCong> findByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    /** Kiểm tra kỹ thuật viên đã được phân công cho phiếu sửa chữa chưa (chống duplicate) */
    boolean existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(Integer maPhieuSuaChua, Integer maNhanVien);

    /** Tìm phân công theo ID và mã phiếu sửa chữa */
    Optional<PhanCong> findByMaPhanCongAndPhieuSuaChuaMaPhieuSuaChua(Integer maPhanCong, Integer maPhieuSuaChua);

    /** Lấy danh sách phân công của một kỹ thuật viên */
    List<PhanCong> findByNhanVienDuocPhanCongMaNhanVien(Integer maNhanVien);

    /** Lấy danh sách phân công do một quản lý tạo */
    List<PhanCong> findByQuanLyMaNhanVien(Integer maQuanLy);

    /** Đếm số lượng phân công của phiếu sửa chữa */
    long countByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);
}
