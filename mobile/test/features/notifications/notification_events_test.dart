import 'dart:async';
import 'dart:io';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/notifications/data/notification_events.dart';

class RealHttpOverrides extends HttpOverrides {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  test(
    'socket URL preserves deployment prefix and uses secure websocket for HTTPS',
    () {
      expect(
        StompNotificationEvents.socketUrl('https://garage.test/prefix/api/'),
        'wss://garage.test/prefix/ws',
      );
      expect(
        StompNotificationEvents.socketUrl('http://localhost:8080/api'),
        'ws://localhost:8080/ws',
      );
    },
  );
  test('only appointment and notification payloads invalidate REST data', () {
    expect(
      StompNotificationEvents.shouldRefresh('{"entityType":"DAT_LICH"}'),
      true,
    );
    expect(
      StompNotificationEvents.shouldRefresh('{"entityType":"THONG_BAO"}'),
      true,
    );
    for (final body in [null, 'invalid', '[]', '{"entityType":"TIN_NHAN"}']) {
      expect(StompNotificationEvents.shouldRefresh(body), false);
    }
  });

  test(
    'STOMP authenticates, subscribes private queue and delivers appointment events',
    () async {
      FlutterSecureStorage.setMockInitialValues({'garage_access_token': 'test-token'});
      final previousOverrides = HttpOverrides.current;
      HttpOverrides.global = RealHttpOverrides();
      addTearDown(() => HttpOverrides.global = previousOverrides);
      final server = await HttpServer.bind(InternetAddress.loopbackIPv4, 0);
      final subscribed = Completer<void>();
      final receivedEvent = Completer<void>();
      WebSocket? socket;
      final serverSubscription = server.listen((request) async {
        expect(request.uri.path, '/ws');
        socket = await WebSocketTransformer.upgrade(request);
        socket!.listen((dynamic data) {
          final frame = data.toString();
          if (frame.startsWith('CONNECT')) {
            expect(frame, contains('Authorization:Bearer test-token'));
            socket!.add('CONNECTED\nversion:1.2\nheart-beat:0,0\n\n\u0000');
          } else if (frame.startsWith('SUBSCRIBE')) {
            expect(frame, contains('destination:/user/queue/notifications'));
            final id = RegExp(
              r'(?:^|\n)id:([^\n]+)',
            ).firstMatch(frame)!.group(1)!;
            subscribed.complete();
            socket!.add(
              'MESSAGE\nsubscription:$id\nmessage-id:1\ncontent-type:application/json\n\n{"entityType":"DAT_LICH"}\u0000',
            );
          }
        });
      });
      final source = StompNotificationEvents(
        'http://127.0.0.1:${server.port}/api',
        const SecureSessionStorage(FlutterSecureStorage()),
      );
      var updates = 0;
      final subscription = source.watch().listen((_) {
        updates++;
        if (updates == 2) receivedEvent.complete();
      });
      try {
        await subscribed.future.timeout(const Duration(seconds: 5));
        await receivedEvent.future.timeout(const Duration(seconds: 5));
        expect(updates, 2); // Connect reconciliation + appointment event.
      } finally {
        await subscription.cancel();
        await socket?.close();
        await serverSubscription.cancel();
        await server.close(force: true);
      }
    },
  );
}
