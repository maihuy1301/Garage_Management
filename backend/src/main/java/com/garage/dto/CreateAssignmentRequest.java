package com.garage.dto;

import jakarta.validation.constraints.NotNull;

public class CreateAssignmentRequest {

    private Integer maQuanLy;

    @NotNull(message = "Mã nhân viên được phân công không được để trống")
    private Integer maNhanVienDuocPhanCong;

    public CreateAssignmentRequest() {}

    public CreateAssignmentRequest(Integer maNhanVienDuocPhanCong) {
        this.maNhanVienDuocPhanCong = maNhanVienDuocPhanCong;
    }

    public CreateAssignmentRequest(Integer maQuanLy, Integer maNhanVienDuocPhanCong) {
        this.maQuanLy = maQuanLy;
        this.maNhanVienDuocPhanCong = maNhanVienDuocPhanCong;
    }

    public Integer getMaQuanLy() {
        return maQuanLy;
    }

    public void setMaQuanLy(Integer maQuanLy) {
        this.maQuanLy = maQuanLy;
    }

    public Integer getMaNhanVienDuocPhanCong() {
        return maNhanVienDuocPhanCong;
    }

    public void setMaNhanVienDuocPhanCong(Integer maNhanVienDuocPhanCong) {
        this.maNhanVienDuocPhanCong = maNhanVienDuocPhanCong;
    }

    // Alias for backward compatibility
    public Integer getTechnicianId() {
        return maNhanVienDuocPhanCong;
    }

    public void setTechnicianId(Integer technicianId) {
        this.maNhanVienDuocPhanCong = technicianId;
    }
}
