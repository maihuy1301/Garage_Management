import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

import '../domain/notification_models.dart';

abstract interface class NotificationAlerts {
  Future<void> requestPermission();
  Future<void> show(GarageNotification notification, String account);
  Future<void> clear();
}

class AndroidNotificationAlerts implements NotificationAlerts {
  static const channel = MethodChannel('com.garage/notifications');
  bool get _supported =>
      !kIsWeb && defaultTargetPlatform == TargetPlatform.android;

  @override
  Future<void> requestPermission() async {
    if (!_supported) return;
    await Permission.notification.request();
  }

  @override
  Future<void> show(GarageNotification notification, String account) async {
    if (!_supported) return;
    await channel.invokeMethod<void>('show', {
      'id': notification.id,
      'title': notification.title,
      'content': notification.content,
      'account': account,
    });
  }

  @override
  Future<void> clear() async {
    if (_supported) await channel.invokeMethod<void>('clear');
  }

  Future<String?> takePendingTap() async =>
      _supported ? await channel.invokeMethod<String>('takePendingTap') : null;
}
