package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateModelRequest {

    @NotBlank(message = "Tên model xe không được để trống")
    @Size(max = 100, message = "Tên model xe không được vượt quá 100 ký tự")
    private String tenModel;

    public CreateModelRequest() {}

    public CreateModelRequest(String tenModel) {
        this.tenModel = tenModel;
    }

    public String getTenModel() {
        return tenModel;
    }

    public void setTenModel(String tenModel) {
        this.tenModel = tenModel;
    }
}
