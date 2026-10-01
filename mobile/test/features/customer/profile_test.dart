import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/customer/data/profile_service.dart';
import 'package:garage_mobile/features/customer/presentation/profile_page.dart';

Map<String, dynamic> profileData() => {
  'tenDangNhap': 'customer',
  'hoTen': 'Khách thử',
  'email': 'test@example.com',
  'soDienThoai': '0900000000',
  'diaChi': 'Hà Nội',
  'ngaySinh': '2000-05-20',
};

class FakeProfile implements ProfileGateway {
  Map<String, dynamic>? saved;
  String? error;
  @override
  Future<CustomerProfile> load() async =>
      CustomerProfile.fromJson(profileData());
  @override
  Future<CustomerProfile> save(Map<String, dynamic> fields) async {
    if (error != null) throw ProfileException(error!);
    saved = fields;
    return CustomerProfile.fromJson({...profileData(), ...fields});
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  test(
    'profile uses own JWT endpoint and strips identity/role fields',
    () async {
      FlutterSecureStorage.setMockInitialValues({
        'garage_access_token': 'test-token',
      });
      final dio = Dio(BaseOptions(baseUrl: 'http://localhost/api'));
      final calls = <String>[];
      final api = ApiClient(
        dio,
        const SecureSessionStorage(FlutterSecureStorage()),
      );
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (request, handler) {
            expect(request.path, '/customers/me');
            expect(request.headers['Authorization'], 'Bearer test-token');
            calls.add(request.method);
            if (request.method == 'PUT') {
              expect(request.data, {'hoTen': 'Tên mới'});
            }
            handler.resolve(
              Response(
                requestOptions: request,
                data: {'success': true, 'data': profileData()},
              ),
            );
          },
        ),
      );
      final service = ProfileService(api);
      await service.load();
      await service.save({
        'hoTen': 'Tên mới',
        'maKhachHang': 999,
        'roles': ['ADMIN'],
      });
      expect(calls, ['GET', 'PUT']);
    },
  );
  Future<void> show(
    WidgetTester tester,
    FakeProfile gateway, {
    ValueChanged<CustomerProfile>? onSaved,
  }) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: ProfilePage(gateway: gateway, onSaved: onSaved),
        ),
      ),
    );
    await tester.pumpAndSettle();
  }

  Finder field(String label) => find.widgetWithText(TextFormField, label);
  Future<void> save(WidgetTester tester) async {
    await tester.ensureVisible(find.text('Lưu thay đổi'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Lưu thay đổi'));
    await tester.pumpAndSettle();
  }

  testWidgets(
    'loads profile, saves trimmed fields and updates displayed name',
    (tester) async {
      final gateway = FakeProfile();
      CustomerProfile? updated;
      await show(tester, gateway, onSaved: (p) => updated = p);
      expect(find.text('customer'), findsOneWidget);
      await tester.enterText(field('Họ và tên'), '  Tên mới  ');
      await save(tester);
      expect(gateway.saved?['hoTen'], 'Tên mới');
      expect(gateway.saved?['ngaySinh'], '2000-05-20');
      expect(updated?.name, 'Tên mới');
      expect(find.text('Đã cập nhật thông tin cá nhân.'), findsOneWidget);
    },
  );
  testWidgets('invalid email prevents saving', (tester) async {
    final gateway = FakeProfile();
    await show(tester, gateway);
    await tester.enterText(field('Email'), 'invalid');
    await save(tester);
    expect(gateway.saved, isNull);
    expect(find.text('Email không hợp lệ.'), findsOneWidget);
  });
  testWidgets('save error retains edits and does not report success', (
    tester,
  ) async {
    final gateway = FakeProfile()..error = 'Email đã được sử dụng';
    await show(tester, gateway);
    await tester.enterText(field('Họ và tên'), 'Giữ chỉnh sửa');
    await save(tester);
    expect(find.text('Email đã được sử dụng'), findsOneWidget);
    await tester.ensureVisible(field('Họ và tên'));
    await tester.pumpAndSettle();
    expect(find.text('Giữ chỉnh sửa'), findsOneWidget);
    expect(find.text('Đã cập nhật thông tin cá nhân.'), findsNothing);
  });
}
