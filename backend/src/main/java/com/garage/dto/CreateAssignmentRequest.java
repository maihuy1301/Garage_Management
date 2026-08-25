package com.garage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO để phân công kỹ thuật viên cho Phiếu sửa chữa.
 */
public class CreateAssignmentRequest {

    @NotNull(message = "Mã nhân viên kỹ thuật không được để trống")
    private Integer technicianId;

    @Size(max = 100, message = "Vai trò trong công việc không được vượt quá 100 ký tự")
    private String vaiTroTrongCongViec;

    public CreateAssignmentRequest() {}

    public CreateAssignmentRequest(Integer technicianId) {
        this.technicianId = technicianId;
    }

    public CreateAssignmentRequest(Integer technicianId, String vaiTroTrongCongViec) {
        this.technicianId = technicianId;
        this.vaiTroTrongCongViec = vaiTroTrongCongViec;
    }

    public Integer getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Integer technicianId) {
        this.technicianId = technicianId;
    }

    public String getVaiTroTrongCongViec() {
        return vaiTroTrongCongViec;
    }

    public void setVaiTroTrongCongViec(String vaiTroTrongCongViec) {
        this.vaiTroTrongCongViec = vaiTroTrongCongViec;
    }
}
