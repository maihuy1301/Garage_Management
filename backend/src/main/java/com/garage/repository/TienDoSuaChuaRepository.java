package com.garage.repository;

import com.garage.entity.TienDoSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TienDoSuaChuaRepository extends JpaRepository<TienDoSuaChua, Integer> {

    /** Lấy lịch sử tiến độ sửa chữa theo phiếu sửa chữa */
    List<TienDoSuaChua> findByPhieuSuaChuaMaPhieuSuaChuaOrderByThoiGianDesc(Integer maPhieuSuaChua);
}
