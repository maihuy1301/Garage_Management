package com.garage.repository;

import com.garage.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    @org.springframework.data.jpa.repository.Query("""
            select distinct e from NhanVien e join fetch e.nguoiDung u
            where e.chiNhanh.maChiNhanh = :branch and e.trangThai = true and u.trangThai = true
              and exists (select r from NguoiDungVaiTro r where r.nguoiDung = u and r.vaiTro.tenVaiTro = 'ROLE_FRONT_DESK')
            """)
    List<NhanVien> findActiveReceptionists(@org.springframework.data.repository.query.Param("branch") Integer branch);
    Optional<NhanVien> findByNguoiDungMaNguoiDung(Integer maNguoiDung);
    List<NhanVien> findByChiNhanhMaChiNhanh(Integer maChiNhanh);
    boolean existsByNguoiDungMaNguoiDung(Integer maNguoiDung);
    boolean existsByChiNhanhMaChiNhanh(Integer maChiNhanh);
}
