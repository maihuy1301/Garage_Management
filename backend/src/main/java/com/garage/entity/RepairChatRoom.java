package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** One room per visit. A deletion marker prevents a deleted chat being recreated. */
@Entity
@Table(name = "PhongChatSuaChua")
public class RepairChatRoom {
    @Id
    @Column(name = "MaTiepNhan")
    private Integer receptionId;
    @Column(name = "ThoiDiemXoa")
    private LocalDateTime deletedAt;
    @Column(name = "MaNguoiXoa")
    private Integer deletedBy;

    public Integer getReceptionId() { return receptionId; }
    public void setReceptionId(Integer value) { receptionId = value; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime value) { deletedAt = value; }
    public Integer getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Integer value) { deletedBy = value; }
}
