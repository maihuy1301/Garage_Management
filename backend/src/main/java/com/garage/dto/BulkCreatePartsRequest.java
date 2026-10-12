package com.garage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BulkCreatePartsRequest {

    @NotEmpty(message = "Danh sách phụ tùng không được rỗng")
    @Valid
    private List<CreatePartRequest> parts;

    public BulkCreatePartsRequest() {}

    public BulkCreatePartsRequest(List<CreatePartRequest> parts) {
        this.parts = parts;
    }

    public List<CreatePartRequest> getParts() {
        return parts;
    }

    public void setParts(List<CreatePartRequest> parts) {
        this.parts = parts;
    }
}
