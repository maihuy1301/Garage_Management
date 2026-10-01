package com.garage.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSessionResponse(String sessionId, Integer invoiceId, String status,
        String environment, BigDecimal amount, String currency, String transferContent,
        Instant expiresAt, Receiver receiver, String qrImageUrl, String invoiceStatus,
        BigDecimal remainingAmount) {
    public record Receiver(String bankCode, String bankName, String accountNumber, String accountName) {}
}
