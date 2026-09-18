import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/theme/app_colors.dart';
import '../data/notification_events.dart';
import '../data/notification_service.dart';
import '../domain/notification_models.dart';

class NotificationsPage extends StatefulWidget {
  const NotificationsPage({super.key, this.gateway, this.events});
  final NotificationGateway? gateway;
  final NotificationEvents? events;

  @override
  State<NotificationsPage> createState() => _NotificationsPageState();
}

class _NotificationsPageState extends State<NotificationsPage>
    with WidgetsBindingObserver {
  late NotificationGateway _gateway;
  late NotificationEvents _events;
  NotificationSnapshot? _snapshot;
  StreamSubscription<void>? _subscription;
  Timer? _timer;
  bool _initialized = false;
  bool _busy = false;
  bool _pending = false;
  bool _active = true;
  String? _error;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_initialized) return;
    _initialized = true;
    _gateway = widget.gateway ?? context.read<NotificationGateway>();
    _events = widget.events ?? context.read<NotificationEvents>();
    WidgetsBinding.instance.addObserver(this);
    _startUpdates();
    _refresh();
  }

  void _startUpdates() {
    _subscription = _events.watch().listen((_) => _refresh(), onError: (_) {});
    // Fallback for missed events or an unavailable WebSocket connection.
    _timer = Timer.periodic(const Duration(seconds: 30), (_) => _refresh());
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    final active = state == AppLifecycleState.resumed;
    if (active == _active) return;
    _active = active;
    if (active) {
      _startUpdates();
      _refresh();
    } else {
      _subscription?.cancel();
      _timer?.cancel();
    }
  }

  Future<void> _refresh() async {
    if (!mounted || !_active) return;
    if (_busy) {
      _pending = true;
      return;
    }
    await _run();
  }

  Future<void> _run({Future<void> Function()? mutation}) async {
    if (_busy || !mounted) return;
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      if (mutation != null) await mutation();
      if (!mounted) return;
      final result = await _gateway.load();
      if (mounted) setState(() => _snapshot = result);
    } on NotificationException catch (error) {
      if (mounted) setState(() => _error = error.message);
    } on Object {
      if (mounted) {
        setState(() => _error = 'Không thể tải thông báo. Vui lòng thử lại.');
      }
    } finally {
      if (mounted) {
        setState(() => _busy = false);
        if (_pending) {
          _pending = false;
          unawaited(_refresh());
        }
      }
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _subscription?.cancel();
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final snapshot = _snapshot;
    return SafeArea(
      child: RefreshIndicator(
        onRefresh: _refresh,
        child: ListView(
          key: const ValueKey('notifications-page'),
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(20),
          children: [
            Container(
              padding: const EdgeInsets.all(22),
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(20),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Icon(
                    Icons.notifications_active_rounded,
                    color: Colors.white,
                    size: 34,
                  ),
                  const SizedBox(height: 14),
                  Text(
                    'Thông báo của tôi',
                    style: Theme.of(
                      context,
                    ).textTheme.headlineMedium?.copyWith(color: Colors.white),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    snapshot == null
                        ? 'Thông báo lịch hẹn và dịch vụ'
                        : '${snapshot.unreadCount} chưa đọc',
                    style: const TextStyle(color: Colors.white),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            Align(
              alignment: Alignment.centerRight,
              child: TextButton.icon(
                onPressed:
                    _busy || snapshot == null || snapshot.unreadCount == 0
                    ? null
                    : () => _run(mutation: _gateway.markAllRead),
                icon: const Icon(Icons.done_all),
                label: const Text('Đọc tất cả'),
              ),
            ),
            if (_busy)
              const LinearProgressIndicator(
                key: ValueKey('notifications-loading'),
              ),
            if (_error != null)
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
                      const Icon(Icons.cloud_off_rounded),
                      Text(_error!, key: const ValueKey('notifications-error')),
                      if (snapshot != null)
                        const Text('Đang hiển thị dữ liệu lần tải trước.'),
                      TextButton(
                        onPressed: _busy ? null : _refresh,
                        child: const Text('Thử lại'),
                      ),
                    ],
                  ),
                ),
              ),
            if (snapshot != null && snapshot.items.isEmpty)
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 48),
                child: Column(
                  children: [
                    Icon(Icons.notifications_none_rounded, size: 48),
                    SizedBox(height: 12),
                    Text(
                      'Chưa có thông báo',
                      key: ValueKey('notifications-empty'),
                    ),
                    Text('Kéo xuống để cập nhật.'),
                  ],
                ),
              ),
            if (snapshot != null)
              for (final item in snapshot.items)
                Card(
                  key: ValueKey('notification-${item.id}'),
                  color: item.isRead
                      ? null
                      : AppColors.primary.withValues(alpha: 0.06),
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Icon(
                              item.isRead
                                  ? Icons.notifications_none
                                  : Icons.notifications_active,
                              color: AppColors.primary,
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                item.title,
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Text(item.content),
                        if (item.createdAt != null) ...[
                          const SizedBox(height: 8),
                          Text(
                            _date(item.createdAt!),
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ],
                        if (item.isRead)
                          const Text('Đã đọc')
                        else
                          TextButton(
                            onPressed: _busy
                                ? null
                                : () => _run(
                                    mutation: () => _gateway.markRead(item.id),
                                  ),
                            child: const Text('Đánh dấu đã đọc'),
                          ),
                      ],
                    ),
                  ),
                ),
          ],
        ),
      ),
    );
  }

  String _date(DateTime date) {
    String two(int value) => value.toString().padLeft(2, '0');
    return '${two(date.day)}/${two(date.month)}/${date.year} · ${two(date.hour)}:${two(date.minute)}';
  }
}
