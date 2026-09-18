package com.garage.dto;

public record VehicleImageDownload(byte[] content, String contentType, String fileName) {
}
