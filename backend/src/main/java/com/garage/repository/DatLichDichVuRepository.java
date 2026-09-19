package com.garage.repository;

import com.garage.entity.DatLichDichVu;
import com.garage.entity.DatLichDichVuId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatLichDichVuRepository extends JpaRepository<DatLichDichVu, DatLichDichVuId> {

    List<DatLichDichVu> findByDatLichMaDatLich(Integer maDatLich);

    void deleteByDatLichMaDatLich(Integer maDatLich);
}
