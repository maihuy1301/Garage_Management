package com.garage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class CreateQuotationRequest {

    @Size(max = 1000, message = "Lý do phát sinh không được vượt quá 1000 ký tự")
    private String lyDoPhatSinh;

    @Valid
    private List<QuotationServiceItemRequest> services = new ArrayList<>();

    @Valid
    private List<QuotationPartItemRequest> parts = new ArrayList<>();

    public CreateQuotationRequest() {}

    public CreateQuotationRequest(String lyDoPhatSinh,
                                  List<QuotationServiceItemRequest> services,
                                  List<QuotationPartItemRequest> parts) {
        this.lyDoPhatSinh = lyDoPhatSinh;
        this.services = services != null ? services : new ArrayList<>();
        this.parts = parts != null ? parts : new ArrayList<>();
    }

    public String getLyDoPhatSinh() {
        return lyDoPhatSinh;
    }

    public void setLyDoPhatSinh(String lyDoPhatSinh) {
        this.lyDoPhatSinh = lyDoPhatSinh;
    }

    public List<QuotationServiceItemRequest> getServices() {
        return services;
    }

    public void setServices(List<QuotationServiceItemRequest> services) {
        this.services = services;
    }

    public List<QuotationPartItemRequest> getParts() {
        return parts;
    }

    public void setParts(List<QuotationPartItemRequest> parts) {
        this.parts = parts;
    }
}
