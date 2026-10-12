package com.garage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class BulkStockImportRequest {

    private Integer branchId;

    @Size(max = 500, message = "Ghi chú chung tối đa 500 ký tự")
    private String ghiChuChung;

    @NotEmpty(message = "Danh sách mặt hàng nhập kho không được rỗng")
    @Valid
    private List<BulkStockImportItem> items;

    public BulkStockImportRequest() {}

    public BulkStockImportRequest(Integer branchId, String ghiChuChung, List<BulkStockImportItem> items) {
        this.branchId = branchId;
        this.ghiChuChung = ghiChuChung;
        this.items = items;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getGhiChuChung() {
        return ghiChuChung;
    }

    public void setGhiChuChung(String ghiChuChung) {
        this.ghiChuChung = ghiChuChung;
    }

    public List<BulkStockImportItem> getItems() {
        return items;
    }

    public void setItems(List<BulkStockImportItem> items) {
        this.items = items;
    }
}
