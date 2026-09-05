import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/auth/auth_exception.dart';
import 'package:garage_mobile/core/auth/auth_repository.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('lỗi kết nối không hiển thị URL hoặc chỉ dẫn kỹ thuật', () async {
    FlutterSecureStorage.setMockInitialValues({});
    final dio = Dio();
    const sessionStorage = SecureSessionStorage(FlutterSecureStorage());
    final apiClient = ApiClient(dio, sessionStorage);
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          handler.reject(
            DioException(
              requestOptions: options,
              type: DioExceptionType.connectionError,
            ),
          );
        },
      ),
    );
    final repository = AuthRepository(apiClient, sessionStorage);

    await expectLater(
      repository.registerCustomer(
        fullName: 'Nguyễn Văn Huy',
        phoneNumber: '0901234567',
        password: 'Password123@',
        acceptedTerms: true,
      ),
      throwsA(
        isA<AuthException>()
            .having(
              (error) => error.message,
              'message',
              'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.',
            )
            .having(
              (error) => error.message,
              'không chứa URL hoặc chỉ dẫn dev',
              allOf(
                isNot(contains('API_BASE_URL')),
                isNot(contains('adb reverse')),
                isNot(contains('127.0.0.1')),
              ),
            ),
      ),
    );
  });
}
