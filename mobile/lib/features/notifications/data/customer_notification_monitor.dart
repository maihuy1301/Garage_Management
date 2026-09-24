import 'dart:async';

import 'device_notifications.dart';
import 'notification_events.dart';
import 'notification_service.dart';

/// Watches all customer tabs; persisted REST data remains the source of truth.
class CustomerNotificationMonitor {
  CustomerNotificationMonitor(
    this.gateway,
    this.events,
    this.alerts, {
    this.remotePushEnabled = false,
  });
  final bool remotePushEnabled;
  bool _reconciling = false;

  final NotificationGateway gateway;
  final NotificationEvents events;
  final NotificationAlerts alerts;
  StreamSubscription<void>? _subscription;
  Timer? _timer;
  String? _account;
  Set<int>? _seen;
  int _generation = 0;
  bool _active = true;
  bool _busy = false;
  bool _pending = false;
  Future<void> _alertQueue = Future<void>.value();

  void setAccount(String? account) {
    if (account == _account) return;
    _disconnect();
    _generation++;
    _account = account;
    _seen = null;
    _busy = false;
    _pending = false;
    _queueAlert(alerts.clear);
    if (account != null) {
      if (!remotePushEnabled) _queueAlert(alerts.requestPermission);
      _connect();
    }
  }

  void setActive(bool active) {
    if (active == _active) return;
    _active = active;
    if (active) {
      _reconciling = remotePushEnabled;
      _connect();
    } else {
      _disconnect();
    }
  }

  void _connect() {
    if (!_active || _account == null || _subscription != null) return;
    _subscription = events.watch().listen((_) => refresh(), onError: (_) {});
    _timer = Timer.periodic(const Duration(seconds: 30), (_) => refresh());
    unawaited(refresh());
  }

  Future<void> refresh() async {
    if (!_active || _account == null) return;
    if (_busy) {
      _pending = true;
      return;
    }
    final generation = _generation;
    final account = _account!;
    _busy = true;
    try {
      final snapshot = await gateway.load();
      if (generation != _generation) return;
      // First load is a baseline: do not replay the entire historical inbox.
      final previous = _seen;
      _seen = {...?previous, ...snapshot.items.map((item) => item.id)};
      if (previous != null && !_reconciling) {
        for (final item in snapshot.items.reversed) {
          if (!item.isRead && !previous.contains(item.id)) {
            _queueAlert(() async {
              if (generation == _generation) await alerts.show(item, account);
            });
          }
        }
      }
      _reconciling = false;
    } on Object {
      // A failed refresh must not discard the baseline or interrupt navigation.
    } finally {
      if (generation == _generation) {
        _busy = false;
        if (_pending) {
          _pending = false;
          unawaited(refresh());
        }
      }
    }
  }

  void _queueAlert(Future<void> Function() action) {
    // Serializes platform calls so logout clears any in-flight account alerts.
    _alertQueue = _alertQueue.then((_) => action()).catchError((Object _) {});
  }

  void _disconnect() {
    unawaited(_subscription?.cancel());
    _subscription = null;
    _timer?.cancel();
    _timer = null;
  }

  void dispose() {
    _generation++;
    _disconnect();
  }
}
