import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/notifications/data/notification_service.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Dio dio;
  late NotificationService service;
  setUp(() {
    FlutterSecureStorage.setMockInitialValues({'garage_access_token': 'test-token'});
    dio = Dio(BaseOptions(baseUrl: 'http://localhost/api'));
    service = NotificationService(
      ApiClient(dio, const SecureSessionStorage(FlutterSecureStorage())),
    );
  });

  test(
    'GET list/count and PATCH read/read-all use JWT without owner or branch',
    () async {
      final requests = <String>[];
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (options, handler) {
            requests.add('${options.method} ${options.path}');
            expect(options.headers['Authorization'], 'Bearer test-token');
            expect(options.queryParameters, isEmpty);
            expect(options.data, isNull);
            final Object? data = options.path.endsWith('unread-count')
                ? {'unreadCount': 7}
                : options.method == 'PATCH'
                ? null
                : [
                    {
                      'maThongBao': 42,
                      'tieuDe': 'Đã xác nhận lịch',
                      'noiDung': 'Nội dung',
                      'daDoc': false,
                      'ngayTao': '2026-09-17T09:30:00',
                    },
                  ];
            handler.resolve(
              Response<Map<String, dynamic>>(
                requestOptions: options,
                data: {'success': true, 'data': data},
              ),
            );
          },
        ),
      );
      final snapshot = await service.load();
      expect(snapshot.items.single.id, 42);
      expect(snapshot.items.single.isRead, false);
      expect(snapshot.items.single.createdAt, DateTime(2026, 9, 17, 9, 30));
      expect(snapshot.unreadCount, 7); // Server count, not derived from list.
      await service.markRead(42);
      await service.markAllRead();
      expect(
        requests,
        containsAll([
          'GET /notifications',
          'GET /notifications/unread-count',
          'PATCH /notifications/42/read',
          'PATCH /notifications/read-all',
        ]),
      );
    },
  );

  for (final status in [401, 403, 500]) {
    test('HTTP $status is surfaced for reads and mutations', () async {
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (options, handler) {
            handler.reject(
              DioException(
                requestOptions: options,
                response: Response<Map<String, dynamic>>(
                  requestOptions: options,
                  statusCode: status,
                ),
                type: DioExceptionType.badResponse,
              ),
            );
          },
        ),
      );
      final matcher = isA<NotificationException>().having(
        (e) => e.message,
        'message',
        contains(
          status == 401
              ? 'đăng nhập'
              : status == 403
              ? 'quyền'
              : 'kết nối',
        ),
      );
      await expectLater(service.load(), throwsA(matcher));
      await expectLater(service.markRead(42), throwsA(matcher));
      await expectLater(service.markAllRead(), throwsA(matcher));
    });
  }

  for (final malformed in [
    null,
    {'unreadCount': -1},
    {'unreadCount': 'bad'},
  ]) {
    test('rejects invalid snapshot $malformed', () async {
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (options, handler) {
            handler.resolve(
              Response<Map<String, dynamic>>(
                requestOptions: options,
                data: {
                  'success': true,
                  'data': options.path.endsWith('unread-count')
                      ? malformed
                      : [],
                },
              ),
            );
          },
        ),
      );
      await expectLater(service.load(), throwsA(isA<NotificationException>()));
    });
  }

  test('unsuccessful envelope does not become a successful mutation', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              data: {'success': false, 'message': 'Không tìm thấy thông báo'},
            ),
          );
        },
      ),
    );
    await expectLater(
      service.markRead(42),
      throwsA(isA<NotificationException>()),
    );
  });
}
