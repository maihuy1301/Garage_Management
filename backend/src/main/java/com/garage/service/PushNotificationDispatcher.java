package com.garage.service;

import com.garage.dto.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationDispatcher {
    private static final Logger log = LoggerFactory.getLogger(PushNotificationDispatcher.class);
    private final PushDeviceService devices;
    private final FcmGateway gateway;
    public PushNotificationDispatcher(PushDeviceService devices, FcmGateway gateway) {
        this.devices = devices;
        this.gateway = gateway;
    }

    @Async("pushExecutor")
    public void send(String account, NotificationResponse notification) {
        if (!gateway.isEnabled()) return;
        try {
            for (var device : devices.forUser(notification.getMaNguoiDung())) {
                var result = gateway.send(device.getFcmToken(), account, notification);
                if (result == FcmGateway.Result.INVALID_TOKEN) {
                    devices.removeInvalid(device.getFcmToken(), device.getMaNguoiDung());
                } else if (result == FcmGateway.Result.UNAVAILABLE) {
                    log.warn("Push unavailable for notification {}; inbox remains available", notification.getMaThongBao());
                }
            }
        } catch (Exception e) {
            log.warn("Push failed for notification {}; inbox remains available", notification.getMaThongBao());
        }
    }
}
