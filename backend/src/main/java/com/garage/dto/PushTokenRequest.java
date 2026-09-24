package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PushTokenRequest(
        @NotBlank @Size(max = 2048)
        @Pattern(regexp = "[A-Za-z0-9_:.\\-]+") String token) {}
