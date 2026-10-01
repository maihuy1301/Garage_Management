import 'dart:io';
import 'dart:ui' as ui;

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/app/router.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/auth/auth_controller.dart';
import 'package:garage_mobile/core/auth/auth_repository.dart';
import 'package:garage_mobile/core/auth/auth_session.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/core/theme/app_theme.dart';
import 'package:garage_mobile/features/public/presentation/home_page.dart';
import 'package:provider/provider.dart';

class _Auth extends AuthController {
  _Auth(this.current)
    : super(
        AuthRepository(
          ApiClient(Dio(), const SecureSessionStorage(FlutterSecureStorage())),
          const SecureSessionStorage(FlutterSecureStorage()),
        ),
      );
  final AuthSession? current;
  @override
  bool get isInitialized => true;
  @override
  bool get isAuthenticated => current != null;
  @override
  AuthSession? get session => current;
}

void main() {
  setUpAll(() async {
    const fontPath = String.fromEnvironment('HOME_PREVIEW_FONT');
    if (fontPath.isNotEmpty) {
      final bytes = ByteData.sublistView(await File(fontPath).readAsBytes());
      for (final family in ['Inter', 'Roboto', 'Ahem']) {
        await (FontLoader(family)..addFont(Future.value(bytes))).load();
      }
      await (FontLoader(
        'MaterialIcons',
      )..addFont(rootBundle.load('fonts/MaterialIcons-Regular.otf'))).load();
    }
  });
  testWidgets('guest quick actions preserve protected route returnTo', (
    tester,
  ) async {
    final auth = _Auth(null);
    final app = AppRouter(auth);
    addTearDown(auth.dispose);
    addTearDown(app.router.dispose);
    tester.view.physicalSize = const Size(800, 1000);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(
      ChangeNotifierProvider<AuthController>.value(
        value: auth,
        child: MaterialApp.router(
          theme: AppTheme.light,
          routerConfig: app.router,
        ),
      ),
    );
    await tester.pumpAndSettle();
    for (final path in [
      '/appointments',
      '/vehicles',
      '/tracking',
      '/invoices',
      '/notifications',
      '/account',
    ]) {
      app.router.go('/');
      await tester.pumpAndSettle();
      final action = find.byKey(ValueKey('home-action-$path'));
      await tester.ensureVisible(action);
      await tester.tap(action);
      await tester.pumpAndSettle();
      final uri = app.router.routeInformationProvider.value.uri;
      expect(uri.path, '/login');
      expect(uri.queryParameters['returnTo'], path);
      expect(tester.takeException(), isNull);
    }
  });

  testWidgets(
    'home fits narrow screens and large text with long customer name',
    (tester) async {
      final auth = _Auth(
        const AuthSession(
          accessToken: 'test',
          username: 'customer',
          fullName: 'Nguyễn Hoàng Minh Anh và gia đình',
          roles: ['ROLE_CUSTOMER'],
        ),
      );
      addTearDown(auth.dispose);
      tester.view.devicePixelRatio = 1;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);
      for (final width in [320.0, 390.0, 768.0]) {
        tester.view.physicalSize = Size(width, 844);
        await tester.pumpWidget(
          ChangeNotifierProvider<AuthController>.value(
            value: auth,
            child: MaterialApp(
              theme: AppTheme.light,
              home: MediaQuery(
                data: MediaQueryData(
                  size: Size(width, 844),
                  textScaler: const TextScaler.linear(2),
                ),
                child: const Scaffold(body: HomePage()),
              ),
            ),
          ),
        );
        await tester.pumpAndSettle();
        expect(find.text('Nguyễn Hoàng Minh Anh và gia đình'), findsOneWidget);
        await tester.ensureVisible(find.text('Chi phí rõ ràng'));
        await tester.pumpAndSettle();
        expect(tester.takeException(), isNull);
      }
    },
  );

  testWidgets('home preview retains five navigation destinations', (
    tester,
  ) async {
    final auth = _Auth(
      const AuthSession(
        accessToken: 'test',
        username: 'customer',
        fullName: 'Minh Anh',
        roles: ['ROLE_CUSTOMER'],
      ),
    );
    final app = AppRouter(auth);
    addTearDown(auth.dispose);
    addTearDown(app.router.dispose);
    tester.view.physicalSize = const Size(390, 844);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    final boundary = GlobalKey();
    await tester.pumpWidget(
      RepaintBoundary(
        key: boundary,
        child: ChangeNotifierProvider<AuthController>.value(
          value: auth,
          child: MaterialApp.router(
            debugShowCheckedModeBanner: false,
            theme: const String.fromEnvironment('HOME_PREVIEW_FONT').isEmpty
                ? AppTheme.light
                : AppTheme.light.copyWith(
                    textTheme: AppTheme.light.textTheme.apply(
                      fontFamily: 'Roboto',
                    ),
                  ),
            routerConfig: app.router,
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.runAsync(
      () => precacheImage(
        const AssetImage('asset/home_autocare_car.png'),
        tester.element(find.byType(HomePage)),
      ),
    );
    await tester.pumpAndSettle();
    expect(
      tester
          .widget<NavigationBar>(find.byType(NavigationBar))
          .destinations
          .length,
      5,
    );
    expect(tester.takeException(), isNull);
    const output = String.fromEnvironment('HOME_PREVIEW');
    if (output.isNotEmpty) {
      await tester.runAsync(() async {
        final image =
            await (boundary.currentContext!.findRenderObject()
                    as RenderRepaintBoundary)
                .toImage(pixelRatio: 2);
        final data = await image.toByteData(format: ui.ImageByteFormat.png);
        await File(output).writeAsBytes(data!.buffer.asUint8List());
        image.dispose();
      });
    }
  });
}
