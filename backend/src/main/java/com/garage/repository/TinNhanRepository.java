package com.garage.repository;

import com.garage.entity.TinNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TinNhanRepository extends JpaRepository<TinNhan, Integer> {

    List<TinNhan> findByCuocHoiThoaiMaCuocHoiThoaiOrderByThoiGianGuiAsc(Integer maCuocHoiThoai);

    List<TinNhan> findByCuocHoiThoaiMaCuocHoiThoaiAndDaDocFalseAndNguoiGuiMaNguoiDungNot(Integer maCuocHoiThoai, Integer maNguoiDung);

    long countByCuocHoiThoaiMaCuocHoiThoaiAndDaDocFalseAndNguoiGuiMaNguoiDungNot(Integer maCuocHoiThoai, Integer maNguoiDung);
}
