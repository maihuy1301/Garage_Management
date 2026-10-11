import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/auth/auth_controller.dart';
import 'package:garage_mobile/core/auth/auth_repository.dart';
import 'package:garage_mobile/core/auth/auth_session.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/core/theme/app_theme.dart';
import 'package:garage_mobile/features/repair_chat/data/repair_chat_service.dart';
import 'package:garage_mobile/features/repair_chat/presentation/repair_chat_page.dart';
import 'repair_chat_controller_test.dart' show FakeGateway, page;

class _Auth extends AuthController {
  _Auth()
    : super(
        AuthRepository(
          ApiClient(Dio(), const SecureSessionStorage(FlutterSecureStorage())),
          const SecureSessionStorage(FlutterSecureStorage()),
        ),
      );
  @override
  AuthSession? get session => const AuthSession(
    accessToken: 'tech-token',
    username: 'tech',
    roles: ['ROLE_TECHNICIAN'],
  );
}

void main() {
  Future<void> mount(WidgetTester tester, FakeGateway api) async {
    tester.view.resetPhysicalSize();
    tester.view.physicalSize = const Size(390, 844);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(
      MultiProvider(
        providers: [
          ChangeNotifierProvider<AuthController>(create: (_) => _Auth()),
          Provider<RepairChatGateway>.value(value: api),
        ],
        child: MaterialApp(
          theme: AppTheme.light,
          home: const RepairChatPage(orderId: 20),
        ),
      ),
    );
    await tester.pumpAndSettle();
  }

  testWidgets('disabled scaffold never offers a working send action', (
    tester,
  ) async {
    final api = FakeGateway()..available = false;
    await mount(tester, api);
    expect(find.text('Trao đổi sửa chữa'), findsOneWidget);
    expect(find.textContaining('chưa được bật'), findsOneWidget);
    expect(tester.widget<TextField>(find.byType(TextField)).enabled, false);
    expect(api.historyCalls, 0);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('vehicle identity and text chat render at phone width', (
    tester,
  ) async {
    final api = FakeGateway();
    await mount(tester, api);
    expect(find.text('51A-12345'), findsOneWidget);
    expect(find.text('Toyota · Phiếu #20'), findsOneWidget);
    expect(find.text('Tin 1'), findsOneWidget);
    expect(find.byIcon(Icons.camera_alt), findsNothing);
    await tester.enterText(find.byType(TextField), 'Nhờ tiếp tân kiểm tra');
    await tester.pump();
    await tester.tap(find.byTooltip('Gửi tin nhắn'));
    await tester.pumpAndSettle();
    expect(api.keys, hasLength(1));
    expect(
      tester.widget<TextField>(find.byType(TextField)).controller!.text,
      isEmpty,
    );
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('archived room has no send or technician delete action', (
    tester,
  ) async {
    final api = FakeGateway()..result = page(state: 'ARCHIVED');
    await mount(tester, api);
    expect(find.textContaining('Lưu trữ'), findsWidgets);
    expect(tester.widget<TextField>(find.byType(TextField)).enabled, false);
    expect(find.text('Xóa vĩnh viễn'), findsNothing);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  });
}
