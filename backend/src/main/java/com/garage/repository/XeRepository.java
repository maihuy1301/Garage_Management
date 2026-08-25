package com.garage.repository;

import com.garage.entity.Xe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface XeRepository extends JpaRepository<Xe, Integer> {

    /** Lấy tất cả xe thuộc một khách hàng (by KhachHang PK). */
    List<Xe> findByKhachHangMaKhachHang(Integer maKhachHang);

    /** Lấy xe theo ID và đồng thời đảm bảo xe thuộc customer đó (tránh data leakage). */
    Optional<Xe> findByMaXeAndKhachHangMaKhachHang(Integer maXe, Integer maKhachHang);

    boolean existsByBienSo(String bienSo);

    boolean existsBySoVIN(String soVIN);
}
