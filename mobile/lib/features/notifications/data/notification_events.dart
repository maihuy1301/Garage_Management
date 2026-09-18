import 'dart:async';
import 'dart:convert';

import 'package:stomp_dart_client/stomp_dart_client.dart';

import '../../../core/storage/secure_session_storage.dart';

abstract interface class NotificationEvents {
  Stream<void> watch();
}

class StompNotificationEvents implements NotificationEvents {
  const StompNotificationEvents(this.apiBaseUrl, this.storage);
  final String apiBaseUrl;
  final SecureSessionStorage storage;

  static String socketUrl(String apiBaseUrl) {
    final uri = Uri.parse(apiBaseUrl);
    return Uri(
      scheme: uri.scheme == 'https' ? 'wss' : 'ws',
      host: uri.host,
      port: uri.hasPort ? uri.port : null,
      path: '${uri.path.replaceFirst(RegExp(r'/api/?$'), '')}/ws',
    ).toString();
  }

  static bool shouldRefresh(String? body) {
    try {
      final event = jsonDecode(body ?? '');
      return event is Map &&
          (event['entityType'] == 'DAT_LICH' ||
              event['entityType'] == 'THONG_BAO');
    } on FormatException {
      return false;
    }
  }

  @override
  Stream<void> watch() {
    late StreamController<void> controller;
    StompClient? client;
    var cancelled = false;
    controller = StreamController<void>(
      onListen: () async {
        try {
          final token = await storage.readToken();
          if (cancelled || token == null || token.isEmpty) return;
          client = StompClient(
            config: StompConfig(
              url: socketUrl(apiBaseUrl),
              stompConnectHeaders: {'Authorization': 'Bearer $token'},
              connectionTimeout: const Duration(seconds: 12),
              onConnect: (_) {
                if (cancelled) return;
                client!.subscribe(
                  destination: '/user/queue/notifications',
                  callback: (frame) {
                    if (!cancelled && shouldRefresh(frame.body)) {
                      controller.add(null);
                    }
                  },
                );
                // Reconcile events missed while disconnected.
                controller.add(null);
              },
              onWebSocketError: (_) {},
              onStompError: (_) {},
            ),
          );
          client!.activate();
        } on Object {
          // REST refresh remains available when realtime cannot connect.
        }
      },
      onCancel: () {
        cancelled = true;
        client?.deactivate();
      },
    );
    return controller.stream;
  }
}
