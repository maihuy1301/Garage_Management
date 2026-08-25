package com.garage.repository;

import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.NguoiDungVaiTroId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NguoiDungVaiTroRepository extends JpaRepository<NguoiDungVaiTro, NguoiDungVaiTroId> {
    List<NguoiDungVaiTro> findByNguoiDungMaNguoiDung(Integer maNguoiDung);

    @Modifying
    @Query("DELETE FROM NguoiDungVaiTro nv WHERE nv.nguoiDung.maNguoiDung = :maNguoiDung")
    void deleteByNguoiDungMaNguoiDung(@Param("maNguoiDung") Integer maNguoiDung);

    @Query("SELECT COUNT(nv) FROM NguoiDungVaiTro nv WHERE nv.vaiTro.tenVaiTro = :tenVaiTro AND nv.nguoiDung.trangThai = true")
    long countActiveUsersByRoleName(@Param("tenVaiTro") String tenVaiTro);
}
