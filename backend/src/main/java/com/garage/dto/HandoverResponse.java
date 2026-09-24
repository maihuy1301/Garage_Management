package com.garage.dto;

import java.time.LocalDateTime;

public record HandoverResponse(boolean eligible, String reason, LocalDateTime thoiGianBanGiao,
                               String tenNguoiBanGiao, String ghiChu) {}
