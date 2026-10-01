import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/invoices/data/invoice_service.dart';
import 'package:garage_mobile/features/invoices/presentation/invoices_page.dart';

import 'invoice_fixture.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Dio dio;
  late InvoiceService service;
  setUp(() {
    FlutterSecureStorage.setMockInitialValues({
      'garage_access_token': 'test-token',
    });
    dio = Dio(BaseOptions(baseUrl: 'http://localhost/api'));
    service = InvoiceService(
      ApiClient(dio, const SecureSessionStorage(FlutterSecureStorage())),
    );
  });

  void respond(Object? data, {bool success = true}) {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          handler.resolve(
            Response(
              requestOptions: options,
              data: {'success': success, 'data': data},
            ),
          );
        },
      ),
    );
  }

  test(
    'GET list/detail use JWT without client ownership or branch filters',
    () async {
      final paths = <String>[];
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (options, handler) {
            paths.add(options.path);
            expect(options.method, 'GET');
            expect(options.queryParameters, isEmpty);
            expect(options.data, isNull);
            expect(options.headers['Authorization'], 'Bearer test-token');
            handler.resolve(
              Response(
                requestOptions: options,
                data: {
                  'success': true,
                  'data': options.path == '/invoices'
                      ? [invoiceJson(id: 8), invoiceJson()]
                      : invoiceJson(),
                },
              ),
            );
          },
        ),
      );
      expect((await service.loadInvoices()).map((e) => e.id), [9, 8]);
      final detail = await service.loadInvoice(9);
      expect(paths, ['/invoices', '/invoices/9']);
      expect(detail.services.single.quantity, 2);
      expect(detail.parts.single.unit, 'lít');
      expect(detail.payments.length, 2);
      expect(detail.payments.last.status, 'THAT_BAI');
      expect(detail.paid, 100000);
      expect(detail.remaining, 319999.5);
      expect(detail.status, 'CHUA_THANH_TOAN');
      expect(invoiceMoney(detail.total), '419.999,50 đ');
    },
  );

  test('empty list is valid', () async {
    respond([]);
    expect(await service.loadInvoices(), isEmpty);
  });

  for (final status in [401, 403, 404, 500]) {
    test('HTTP $status becomes a readable error', () async {
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (options, handler) {
            handler.reject(
              DioException(
                requestOptions: options,
                response: Response(requestOptions: options, statusCode: status),
              ),
            );
          },
        ),
      );
      final message = switch (status) {
        401 => 'Phiên đăng nhập',
        403 => 'không có quyền',
        404 => 'Không tìm thấy',
        _ => 'kiểm tra kết nối',
      };
      await expectLater(
        service.loadInvoice(9),
        throwsA(
          isA<InvoiceException>().having(
            (e) => e.message,
            'message',
            contains(message),
          ),
        ),
      );
    });
  }

  test('network failure becomes retryable error', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          handler.reject(
            DioException(
              requestOptions: options,
              type: DioExceptionType.connectionTimeout,
            ),
          );
        },
      ),
    );
    await expectLater(service.loadInvoices(), throwsA(isA<InvoiceException>()));
  });

  for (final data in [
    null,
    {},
    [null],
    [invoiceJson()..remove('thanhTien')],
  ]) {
    test(
      'malformed list is rejected instead of showing zero/empty totals: $data',
      () async {
        respond(data);
        await expectLater(
          service.loadInvoices(),
          throwsA(isA<InvoiceException>()),
        );
      },
    );
  }

  test('unsuccessful envelope is rejected', () async {
    respond([], success: false);
    await expectLater(service.loadInvoices(), throwsA(isA<InvoiceException>()));
  });

  test('invalid invoice ID does not call API', () async {
    dio.interceptors.add(
      InterceptorsWrapper(onRequest: (_, _) => fail('Unexpected request')),
    );
    await expectLater(
      service.loadInvoice(-1),
      throwsA(isA<InvoiceException>()),
    );
  });
}
