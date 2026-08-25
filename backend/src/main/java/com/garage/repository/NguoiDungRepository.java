package com.garage.repository;

import com.garage.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);
    Optional<NguoiDung> findByEmail(String email);
    Optional<NguoiDung> findByTenDangNhapOrEmail(String tenDangNhap, String email);
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmail(String email);
}
