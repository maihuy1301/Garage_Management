import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'dart:async';
import 'package:firebase_messaging/firebase_messaging.dart';

import '../core/auth/auth_controller.dart';
import '../core/theme/app_theme.dart';
import '../features/notifications/data/customer_notification_monitor.dart';
import '../features/notifications/data/device_notifications.dart';
import '../features/notifications/data/notification_events.dart';
import '../features/notifications/data/notification_service.dart';
import '../features/notifications/data/push_registration.dart';
import 'router.dart';

class GarageApp extends StatefulWidget {
  const GarageApp({super.key});

  @override
  State<GarageApp> createState() => _GarageAppState();
}

class _GarageAppState extends State<GarageApp> with WidgetsBindingObserver {
  late final AppRouter _appRouter;
  late final AuthController _auth;
  late final CustomerNotificationMonitor _notifications;
  final _alerts = AndroidNotificationAlerts();
  late final PushRegistration _push;
  StreamSubscription<RemoteMessage>? _foregroundPush;
  StreamSubscription<RemoteMessage>? _pushTap;
  String? _pendingPushAccount;

  @override
  void initState() {
    super.initState();
    _auth = context.read<AuthController>();
    _appRouter = AppRouter(_auth);
    _push = context.read<PushRegistration>();
    _notifications = CustomerNotificationMonitor(
      context.read<NotificationGateway>(),
      context.read<NotificationEvents>(),
      _alerts,
      remotePushEnabled: _push.available,
    );
    if (_push.available) {
      _foregroundPush = FirebaseMessaging.onMessage.listen((message) {
        if (message.data['account'] == _auth.session?.username) {
          unawaited(_notifications.refresh());
        }
      });
      _pushTap = FirebaseMessaging.onMessageOpenedApp.listen((message) {
        _openNotifications(message.data['account']);
      });
      _loadInitialPush();
    }
    WidgetsBinding.instance.addObserver(this);
    _auth.addListener(_syncNotifications);
    AndroidNotificationAlerts.channel.setMethodCallHandler((call) async {
      if (call.method == 'notificationTap') _openNotifications(call.arguments);
    });
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) _syncNotifications();
    });
  }

  void _syncNotifications() {
    final session = _auth.session;
    _push.setSession(session);
    _notifications.setAccount(
      session?.isCustomer == true ? session!.username : null,
    );
    if (_auth.isInitialized) _handlePendingTap();
    if (_auth.isInitialized && _pendingPushAccount != null) {
      _openNotifications(_pendingPushAccount);
      _pendingPushAccount = null;
    }
  }

  Future<void> _loadInitialPush() async {
    try {
      final message = await FirebaseMessaging.instance.getInitialMessage();
      if (!mounted || message == null) return;
      _pendingPushAccount = message.data['account'] as String?;
      if (_auth.isInitialized) _syncNotifications();
    } on Object {
      /* The inbox is still available if launch data cannot be read. */
    }
  }

  Future<void> _handlePendingTap() async {
    try {
      final account = await _alerts.takePendingTap();
      if (mounted && account != null) _openNotifications(account);
    } on Object {
      // Platforms without native notifications still use the in-app inbox.
    }
  }

  void _openNotifications(Object? account) {
    final session = _auth.session;
    if (session?.isCustomer == true && session!.username == account) {
      _appRouter.router.go('/notifications');
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    _notifications.setActive(state == AppLifecycleState.resumed);
    if (state == AppLifecycleState.resumed) unawaited(_push.retry());
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _auth.removeListener(_syncNotifications);
    _notifications.dispose();
    unawaited(_foregroundPush?.cancel());
    unawaited(_pushTap?.cancel());
    _push.dispose();
    AndroidNotificationAlerts.channel.setMethodCallHandler(null);
    _appRouter.router.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'AutoCare Garage',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      routerConfig: _appRouter.router,
    );
  }
}
