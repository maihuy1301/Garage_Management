package com.garage.repository;

import com.garage.entity.PhanCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhanCongRepository extends JpaRepository<PhanCong, Integer> {

    /** Lấy danh sách phân công theo phiếu sửa chữa */
    List<PhanCong> findByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    /** Lấy danh sách phân công theo phiếu sửa chữa và trạng thái */
    List<PhanCong> findByPhieuSuaChuaMaPhieuSuaChuaAndTrangThai(Integer maPhieuSuaChua, String trangThai);

    /** Kiểm tra kỹ thuật viên đã được phân công cho phiếu sửa chữa chưa (chống duplicate) */
    boolean existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(Integer maPhieuSuaChua, Integer maNhanVien);

    /** Kiểm tra kỹ thuật viên đã được phân công và duyệt cho phiếu sửa chữa chưa */
    boolean existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVienAndTrangThai(Integer maPhieuSuaChua, Integer maNhanVien, String trangThai);

    /** Tìm phân công theo ID và mã phiếu sửa chữa */
    Optional<PhanCong> findByMaPhanCongAndPhieuSuaChuaMaPhieuSuaChua(Integer maPhanCong, Integer maPhieuSuaChua);

    /** Lấy danh sách phân công của một kỹ thuật viên */
    List<PhanCong> findByNhanVienDuocPhanCongMaNhanVien(Integer maNhanVien);

    /** Lấy danh sách phân công của một kỹ thuật viên theo trạng thái */
    List<PhanCong> findByNhanVienDuocPhanCongMaNhanVienAndTrangThai(Integer maNhanVien, String trangThai);

    /** Lấy danh sách phân công theo trạng thái trên toàn hệ thống */
    List<PhanCong> findByTrangThai(String trangThai);

    /** Lấy danh sách phân công theo trạng thái và chi nhánh */
    List<PhanCong> findByTrangThaiAndPhieuSuaChuaChiNhanhMaChiNhanh(String trangThai, Integer maChiNhanh);

    /** Đếm số lượng phân công của phiếu sửa chữa */
    long countByPhieuSuaChuaMaPhieuSuaChua(Integer maPhieuSuaChua);

    /** Đếm số lượng phân công của phiếu sửa chữa theo trạng thái */
    long countByPhieuSuaChuaMaPhieuSuaChuaAndTrangThai(Integer maPhieuSuaChua, String trangThai);
}
