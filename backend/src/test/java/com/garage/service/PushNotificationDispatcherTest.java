package com.garage.service;

import com.garage.dto.NotificationResponse;
import com.garage.entity.ThietBiPush;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class PushNotificationDispatcherTest {
    final PushDeviceService devices = mock(PushDeviceService.class);
    final FcmGateway gateway = mock(FcmGateway.class);
    final PushNotificationDispatcher dispatcher = new PushNotificationDispatcher(devices, gateway);
    NotificationResponse notification() {
        return new NotificationResponse(1, 5, "Title", "Content", "VEHICLE_HANDED_OVER", 10, false, null);
    }
    @Test void disabledFcmDoesNotQueryDeviceTable() {
        dispatcher.send("customer", notification());
        verifyNoInteractions(devices);
    }
    @Test void sendsToAllDevicesAndPrunesOnlyUnregisteredTokens() {
        var first = new ThietBiPush(); first.setMaNguoiDung(5); first.setFcmToken("old-token");
        var second = new ThietBiPush(); second.setMaNguoiDung(5); second.setFcmToken("new-token");
        when(gateway.isEnabled()).thenReturn(true);
        when(devices.forUser(5)).thenReturn(List.of(first, second));
        when(gateway.send(eq("old-token"), eq("customer"), any())).thenReturn(FcmGateway.Result.INVALID_TOKEN);
        when(gateway.send(eq("new-token"), eq("customer"), any())).thenReturn(FcmGateway.Result.SENT);
        dispatcher.send("customer", notification());
        verify(devices).removeInvalid("old-token", 5);
        verify(devices, never()).removeInvalid("new-token", 5);
        verify(gateway).send(eq("new-token"), eq("customer"), any());
    }
    @Test void transientFailureDoesNotDeleteTokenOrThrowIntoBusinessOperation() {
        var device = new ThietBiPush(); device.setFcmToken("token"); device.setMaNguoiDung(5);
        when(gateway.isEnabled()).thenReturn(true);
        when(devices.forUser(5)).thenReturn(List.of(device));
        when(gateway.send(any(), any(), any())).thenReturn(FcmGateway.Result.UNAVAILABLE);
        assertThatCode(() -> dispatcher.send("customer", notification())).doesNotThrowAnyException();
        verify(devices, never()).removeInvalid(any(), any());
    }
}
