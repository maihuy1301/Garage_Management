import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/features/notifications/data/notification_events.dart';
import 'package:garage_mobile/features/notifications/data/notification_service.dart';
import 'package:garage_mobile/features/notifications/domain/notification_models.dart';
import 'package:garage_mobile/features/notifications/presentation/notifications_page.dart';

class FakeEvents implements NotificationEvents {
  final controller = StreamController<void>.broadcast();
  @override
  Stream<void> watch() => controller.stream;
}

class FakeGateway implements NotificationGateway {
  int loads = 0;
  int reads = 0;
  int allReads = 0;
  bool failLoad = false;
  bool failMutation = false;
  bool read = false;
  bool empty = false;
  Completer<NotificationSnapshot>? pending;
  @override
  Future<NotificationSnapshot> load() async {
    loads++;
    if (pending != null) return pending!.future;
    if (failLoad) throw const NotificationException('Lỗi tải thông báo');
    return snapshot;
  }

  NotificationSnapshot get snapshot => NotificationSnapshot(
    empty
        ? []
        : [
            GarageNotification(
              id: 1,
              title: 'Lịch hẹn đã xác nhận',
              content: 'Hẹn gặp bạn tại garage',
              isRead: read,
              createdAt: DateTime(2026, 9, 17),
            ),
          ],
    empty || read ? 0 : 3,
  );
  @override
  Future<void> markRead(int id) async {
    reads++;
    if (failMutation) throw const NotificationException('Không có quyền');
    read = true;
  }

  @override
  Future<void> markAllRead() async {
    allReads++;
    if (failMutation) throw const NotificationException('Không có quyền');
    read = true;
  }
}

void main() {
  late FakeGateway gateway;
  late FakeEvents events;
  setUp(() {
    gateway = FakeGateway();
    events = FakeEvents();
  });
  tearDown(() async => events.controller.close());
  Future<void> mount(WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: NotificationsPage(gateway: gateway, events: events),
        ),
      ),
    );
  }

  Future<void> unmount(WidgetTester tester) async =>
      tester.pumpWidget(const SizedBox());

  testWidgets('loading then list, server unread count, and mark read', (
    tester,
  ) async {
    gateway.pending = Completer();
    await mount(tester);
    expect(find.byKey(const ValueKey('notifications-loading')), findsOneWidget);
    gateway.pending!.complete(gateway.snapshot);
    gateway.pending = null;
    await tester.pumpAndSettle();
    expect(find.text('3 chưa đọc'), findsOneWidget);
    await tester.tap(find.text('Đánh dấu đã đọc'));
    await tester.pumpAndSettle();
    expect(gateway.reads, 1);
    expect(find.text('0 chưa đọc'), findsOneWidget);
    expect(find.text('Đã đọc'), findsOneWidget);
    await unmount(tester);
  });

  testWidgets('read all reloads server state and disables when no unread', (
    tester,
  ) async {
    await mount(tester);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Đọc tất cả'));
    await tester.pumpAndSettle();
    expect(gateway.allReads, 1);
    expect(find.text('0 chưa đọc'), findsOneWidget);
    await tester.tap(find.text('Đọc tất cả'));
    await tester.pumpAndSettle();
    expect(gateway.allReads, 1);
    await unmount(tester);
  });

  testWidgets('error retry and empty state support pull to refresh', (
    tester,
  ) async {
    gateway.failLoad = true;
    await mount(tester);
    await tester.pumpAndSettle();
    expect(find.text('Lỗi tải thông báo'), findsOneWidget);
    gateway.failLoad = false;
    gateway.empty = true;
    await tester.tap(find.text('Thử lại'));
    await tester.pumpAndSettle();
    expect(find.text('Chưa có thông báo'), findsOneWidget);
    gateway.empty = false;
    await tester.drag(find.byType(ListView), const Offset(0, 400));
    await tester.pumpAndSettle();
    expect(find.text('Lịch hẹn đã xác nhận'), findsOneWidget);
    await unmount(tester);
  });

  testWidgets('mutation and refresh errors preserve unread data', (
    tester,
  ) async {
    await mount(tester);
    await tester.pumpAndSettle();
    gateway.failMutation = true;
    await tester.tap(find.text('Đánh dấu đã đọc'));
    await tester.pumpAndSettle();
    expect(find.text('Không có quyền'), findsOneWidget);
    expect(find.text('3 chưa đọc'), findsOneWidget);
    gateway.failLoad = true;
    events.controller.add(null);
    await tester.pumpAndSettle();
    expect(find.text('Lỗi tải thông báo'), findsOneWidget);
    expect(find.text('Lịch hẹn đã xác nhận'), findsOneWidget);
    await unmount(tester);
  });

  testWidgets(
    'events during loading queue one reload without overlapping requests',
    (tester) async {
      gateway.pending = Completer();
      await mount(tester);
      events.controller.add(null);
      events.controller.add(null);
      await tester.pump();
      expect(gateway.loads, 1);
      gateway.pending!.complete(gateway.snapshot);
      gateway.pending = null;
      gateway.empty = true;
      await tester.pumpAndSettle();
      expect(gateway.loads, 2);
      expect(find.text('Chưa có thông báo'), findsOneWidget);
      await unmount(tester);
      expect(events.controller.hasListener, false);
    },
  );

  testWidgets(
    'pause stops updates; resume refreshes; dispose ignores in-flight result',
    (tester) async {
      await mount(tester);
      await tester.pumpAndSettle();
      tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.paused);
      await tester.pump();
      expect(events.controller.hasListener, false);
      await tester.pump(const Duration(seconds: 31));
      expect(gateway.loads, 1);
      tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.resumed);
      await tester.pumpAndSettle();
      expect(gateway.loads, 2);
      gateway.pending = Completer();
      events.controller.add(null);
      await tester.pump();
      await unmount(tester);
      gateway.pending!.complete(gateway.snapshot);
      await tester.pump();
      expect(tester.takeException(), isNull);
    },
  );
}
