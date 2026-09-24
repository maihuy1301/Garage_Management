package com.garage.service;

import com.garage.dto.NotificationResponse;
import com.google.firebase.messaging.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class FcmGateway {
    public enum Result { SENT, INVALID_TOKEN, UNAVAILABLE }
    private final ObjectProvider<FirebaseMessaging> messaging;
    public FcmGateway(ObjectProvider<FirebaseMessaging> messaging) { this.messaging = messaging; }
    public boolean isEnabled() { return messaging.getIfAvailable() != null; }

    public Result send(String token, String account, NotificationResponse notification) {
        var client = messaging.getIfAvailable();
        if (client == null) return Result.UNAVAILABLE;
        try {
            client.send(message(token, account, notification));
            return Result.SENT;
        } catch (FirebaseMessagingException e) {
            return e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    ? Result.INVALID_TOKEN : Result.UNAVAILABLE;
        }
    }

    static Message message(String token, String account, NotificationResponse notification) {
        // Keep personal vehicle details in the authenticated inbox, not the lock screen.
        return Message.builder().setToken(token)
                .setNotification(Notification.builder().setTitle("Thông báo từ AutoCare")
                        .setBody("Bạn có cập nhật mới về xe. Mở ứng dụng để xem chi tiết.").build())
                .putData("account", account)
                .putData("notificationId", notification.getMaThongBao().toString())
                .setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH)
                        .setTtl(24 * 60 * 60 * 1000L)
                        .setNotification(AndroidNotification.builder().setChannelId("customer_progress")
                                .setIcon("ic_notification")
                                .setTag("garage-" + notification.getMaThongBao()).build()).build())
                .build();
    }
}
