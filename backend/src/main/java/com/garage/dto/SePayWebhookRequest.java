package com.garage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SePayWebhookRequest(
        @NotNull @Positive Long id,
        @NotBlank @Size(max = 50) String gateway,
        @NotBlank @Size(max = 50) String accountNumber,
        @Size(max = 100) String code,
        @NotBlank @Size(max = 1000) String content,
        @NotBlank @Pattern(regexp = "in|out") String transferType,
        @NotNull @DecimalMin("1") @Digits(integer = 16, fraction = 0) BigDecimal transferAmount,
        @Size(max = 100) String referenceCode) {}
