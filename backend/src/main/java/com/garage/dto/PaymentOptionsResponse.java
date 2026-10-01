package com.garage.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaymentOptionsResponse(boolean available, String message, String environment,
        BigDecimal remainingAmount, List<PaymentSessionResponse.Receiver> receivers) {}
