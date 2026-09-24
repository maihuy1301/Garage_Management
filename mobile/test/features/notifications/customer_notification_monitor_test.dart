import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/features/notifications/data/customer_notification_monitor.dart';
import 'package:garage_mobile/features/notifications/data/device_notifications.dart';
import 'package:garage_mobile/features/notifications/data/notification_events.dart';
import 'package:garage_mobile/features/notifications/data/notification_service.dart';
import 'package:garage_mobile/features/notifications/domain/notification_models.dart';

GarageNotification item(int id, {bool read = false}) => GarageNotification(
  id: id,
  title: 'Stage $id',
  content: 'Vehicle progress',
  isRead: read,
  createdAt: null,
);

class Gateway implements NotificationGateway {
  List<GarageNotification> items = [];
  Completer<NotificationSnapshot>? pending;
  bool fail = false;
  int loads = 0;

  @override
  Future<NotificationSnapshot> load() async {
    loads++;
    if (fail) throw const NotificationException('offline');
    return pending?.future ?? NotificationSnapshot(items, items.length);
  }

  @override
  Future<void> markRead(int id) async {}
  @override
  Future<void> markAllRead() async {}
}

class Events implements NotificationEvents {
  final controller = StreamController<void>.broadcast();
  @override
  Stream<void> watch() => controller.stream;
}

class Alerts implements NotificationAlerts {
  final shown = <(String, int)>[];
  int permissions = 0;
  int clears = 0;
  @override
  Future<void> requestPermission() async => permissions++;
  @override
  Future<void> show(GarageNotification notification, String account) async {
    shown.add((account, notification.id));
  }

  @override
  Future<void> clear() async => clears++;
}

void main() {
  late Gateway gateway;
  late Events events;
  late Alerts alerts;
  late CustomerNotificationMonitor monitor;

  void initialize() {
    gateway = Gateway();
    events = Events();
    alerts = Alerts();
    monitor = CustomerNotificationMonitor(gateway, events, alerts);
  }

  tearDown(() async {
    monitor.dispose();
    await events.controller.close();
  });

  testWidgets(
    'requests permission once per login, alerts only new unread rows',
    (tester) async {
      initialize();
      gateway.items = [item(1)];
      monitor.setAccount('customer');
      await tester.pump();
      monitor.setAccount('customer');
      expect(alerts.permissions, 1);
      expect(alerts.shown, isEmpty);

      gateway.items = [item(3, read: true), item(2), item(1)];
      events.controller.add(null);
      await tester.pump();
      expect(alerts.shown, [('customer', 2)]);
      events.controller.add(null);
      await tester.pump();
      expect(alerts.shown, hasLength(1));
      monitor.dispose();
    },
  );

  testWidgets(
    'polls other tabs and reconciles when returning from background',
    (tester) async {
      initialize();
      monitor.setAccount('customer');
      await tester.pump();
      gateway.items = [item(1)];
      await tester.pump(const Duration(seconds: 30));
      expect(alerts.shown, [('customer', 1)]);

      monitor.setActive(false);
      final loads = gateway.loads;
      gateway.items = [item(2), item(1)];
      events.controller.add(null);
      await tester.pump(const Duration(seconds: 31));
      expect(gateway.loads, loads);
      monitor.setActive(true);
      await tester.pump();
      expect(alerts.shown, [('customer', 1), ('customer', 2)]);
      monitor.dispose();
    },
  );

  testWidgets('ignores old account response after switching account', (
    tester,
  ) async {
    initialize();
    monitor.setAccount('first');
    await tester.pump();
    final oldResponse = Completer<NotificationSnapshot>();
    gateway.pending = oldResponse;
    unawaited(monitor.refresh());
    gateway.pending = null;
    monitor.setAccount('second');
    await tester.pump();
    oldResponse.complete(NotificationSnapshot([item(10)], 1));
    await tester.pump();
    expect(alerts.shown, isEmpty);
    gateway.items = [item(11)];
    await monitor.refresh();
    await tester.pump();
    expect(alerts.shown, [('second', 11)]);
    monitor.setAccount(null);
    await tester.pump();
    expect(alerts.clears, 3);
    expect(events.controller.hasListener, isFalse);
    monitor.dispose();
  });

  testWidgets('failed REST refresh keeps baseline for retry', (tester) async {
    initialize();
    monitor.setAccount('customer');
    await tester.pump();
    gateway.fail = true;
    await monitor.refresh();
    gateway.fail = false;
    gateway.items = [item(1)];
    await monitor.refresh();
    await tester.pump();
    expect(alerts.shown, [('customer', 1)]);
    monitor.dispose();
  });

  testWidgets(
    'FCM mode reconciles background inbox without a duplicate local banner',
    (tester) async {
      initialize();
      monitor.dispose();
      monitor = CustomerNotificationMonitor(
        gateway,
        events,
        alerts,
        remotePushEnabled: true,
      );
      monitor.setAccount('customer');
      await tester.pump();
      expect(alerts.permissions, 0);
      monitor.setActive(false);
      gateway.items = [item(1)];
      monitor.setActive(true);
      await tester.pump();
      expect(alerts.shown, isEmpty);
      gateway.items = [item(2), item(1)];
      events.controller.add(null);
      await tester.pump();
      expect(alerts.shown, [('customer', 2)]);
      monitor.dispose();
    },
  );

  testWidgets('events during request queue one follow-up refresh', (
    tester,
  ) async {
    initialize();
    monitor.setAccount('customer');
    await tester.pump();
    final pending = Completer<NotificationSnapshot>();
    gateway.pending = pending;
    unawaited(monitor.refresh());
    events.controller.add(null);
    events.controller.add(null);
    await tester.pump();
    final loads = gateway.loads;
    gateway.pending = null;
    gateway.items = [item(2), item(1)];
    pending.complete(NotificationSnapshot([item(1)], 1));
    await tester.pump();
    expect(gateway.loads, loads + 1);
    expect(alerts.shown, [('customer', 1), ('customer', 2)]);
    monitor.dispose();
  });
}
