import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/auth/auth_controller.dart';
import 'package:garage_mobile/core/auth/auth_repository.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/auth/presentation/register_page.dart';
import 'package:provider/provider.dart';

void main() {
  late AuthController authController;

  setUp(() {
    const sessionStorage = SecureSessionStorage(FlutterSecureStorage());
    final apiClient = ApiClient(Dio(), sessionStorage);
    authController = AuthController(AuthRepository(apiClient, sessionStorage));
  });

  testWidgets('hiển thị đầy đủ form đăng ký khách hàng', (tester) async {
    await tester.pumpWidget(
      ChangeNotifierProvider.value(
        value: authController,
        child: const MaterialApp(home: RegisterPage()),
      ),
    );

    expect(find.text('Tạo tài khoản mới'), findsOneWidget);
    expect(find.text('Họ và tên'), findsOneWidget);
    expect(find.text('Số điện thoại'), findsOneWidget);
    expect(find.textContaining('Không bắt buộc'), findsOneWidget);
    expect(find.text('Mật khẩu'), findsOneWidget);
    expect(
      find.byKey(const ValueKey('register-submit-button')),
      findsOneWidget,
    );
    expect(find.text('Đã có tài khoản?'), findsOneWidget);
  });

  testWidgets('chặn submit khi form trống và chưa đồng ý điều khoản', (
    tester,
  ) async {
    await tester.pumpWidget(
      ChangeNotifierProvider.value(
        value: authController,
        child: const MaterialApp(home: RegisterPage()),
      ),
    );

    final submit = find.byKey(const ValueKey('register-submit-button'));
    await tester.ensureVisible(submit);
    await tester.tap(submit);
    await tester.pump();

    expect(find.text('Vui lòng nhập họ và tên.'), findsOneWidget);
    expect(find.text('Vui lòng nhập số điện thoại.'), findsOneWidget);
    expect(find.text('Vui lòng nhập mật khẩu.'), findsOneWidget);
    expect(
      find.text('Bạn cần đồng ý với điều khoản và chính sách bảo mật.'),
      findsOneWidget,
    );
  });
}
