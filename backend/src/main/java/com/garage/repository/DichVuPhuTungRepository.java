package com.garage.repository;

import com.garage.entity.DichVuPhuTung;
import com.garage.entity.DichVuPhuTungId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DichVuPhuTungRepository extends JpaRepository<DichVuPhuTung, DichVuPhuTungId> {

    List<DichVuPhuTung> findByDichVuMaDichVu(Integer maDichVu);

    List<DichVuPhuTung> findByPhuTungMaPhuTung(Integer maPhuTung);

    void deleteByDichVuMaDichVu(Integer maDichVu);
}
