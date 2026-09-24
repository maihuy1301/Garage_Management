package com.garage.dto;

import jakarta.validation.constraints.Size;

public record HandoverRequest(@Size(max = 500) String ghiChu) {}
