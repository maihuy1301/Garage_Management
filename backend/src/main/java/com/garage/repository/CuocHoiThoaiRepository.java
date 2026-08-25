package com.garage.repository;

import com.garage.entity.CuocHoiThoai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuocHoiThoaiRepository extends JpaRepository<CuocHoiThoai, Integer> {

    List<CuocHoiThoai> findByKhachHangMaKhachHangOrderByNgayCapNhatCuoiDesc(Integer maKhachHang);

    List<CuocHoiThoai> findByNhanVienMaNhanVienOrderByNgayCapNhatCuoiDesc(Integer maNhanVien);

    @Query("SELECT c FROM CuocHoiThoai c WHERE c.phieuTiepNhan.chiNhanh.maChiNhanh = :maChiNhanh ORDER BY c.ngayCapNhatCuoi DESC")
    List<CuocHoiThoai> findByBranchId(@Param("maChiNhanh") Integer maChiNhanh);

    Optional<CuocHoiThoai> findByKhachHangMaKhachHangAndPhieuTiepNhanMaTiepNhan(Integer maKhachHang, Integer maTiepNhan);

    List<CuocHoiThoai> findAllByOrderByNgayCapNhatCuoiDesc();
}
