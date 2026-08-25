package com.garage.repository;

import com.garage.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {

    List<ThongBao> findByNguoiDungMaNguoiDungOrderByNgayTaoDesc(Integer maNguoiDung);

    List<ThongBao> findByNguoiDungMaNguoiDungAndDaDocFalse(Integer maNguoiDung);

    long countByNguoiDungMaNguoiDungAndDaDocFalse(Integer maNguoiDung);
}
