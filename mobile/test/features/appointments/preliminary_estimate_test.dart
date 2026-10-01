import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/appointments/data/appointment_service.dart';
import 'package:garage_mobile/features/appointments/domain/appointment_models.dart';
import 'package:garage_mobile/features/appointments/presentation/appointment_detail_page.dart';
import 'package:garage_mobile/features/appointments/presentation/preliminary_estimate_card.dart';

Map<String, dynamic> _appointment() => {
  'maDatLich': 16,
  'trangThai': 'DA_TIEP_NHAN',
  'thoiGianHen': '2026-09-27T09:00:00',
  'bienSoXe': '51A-11111',
  'tenChiNhanh': 'Garage Trung tâm',
  'tongTienDichVuDuKien': 120000,
  'tongTienPhuTungDuKien': 230000.0,
  'tongChiPhiDuKien': 350000,
  // Deliberately different line values: the client must not replace API totals.
  'dichVu': [
    {
      'maDichVu': 1,
      'tenDichVu': 'Kiểm tra xe',
      'donGia': 10000,
      'tienPhuTungDuKien': 20000,
      'tongGiaDuKien': 30000,
      'parts': [
        {
          'maPhuTung': 2,
          'tenPhuTung': 'Lọc dầu',
          'soLuong': 2,
          'donGia': 10000,
          'thanhTien': 20000,
          'donViTinh': 'cái',
        },
      ],
    },
  ],
};

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('maps backend totals, parts and preserves missing versus zero', () {
    final data = _appointment();
    final appointment = AppointmentBookingResult.fromJson(data);
    expect(appointment.estimatedLaborCost, 120000);
    expect(appointment.estimatedPartsCost, 230000);
    expect(appointment.estimatedTotalCost, 350000);
    expect(appointment.services.single.parts.single.quantity, 2);
    data.remove('tongChiPhiDuKien');
    expect(AppointmentBookingResult.fromJson(data).estimatedTotalCost, isNull);
    data['tongChiPhiDuKien'] = 0;
    expect(AppointmentBookingResult.fromJson(data).estimatedTotalCost, 0);
  });

  testWidgets('owned appointment displays server totals and refreshes by GET', (
    tester,
  ) async {
    FlutterSecureStorage.setMockInitialValues({
      'garage_access_token': 'test-token',
    });
    final dio = Dio(BaseOptions(baseUrl: 'http://localhost/api'));
    addTearDown(dio.close);
    final requests = <RequestOptions>[];
    var total = 350000;
    final service = AppointmentService(
      ApiClient(dio, const SecureSessionStorage(FlutterSecureStorage())),
    );
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (request, handler) {
          requests.add(request);
          handler.resolve(
            Response(
              requestOptions: request,
              data: {
                'success': true,
                'data': {..._appointment(), 'tongChiPhiDuKien': total},
              },
            ),
          );
        },
      ),
    );
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: AppointmentDetailPage(appointmentId: 16, gateway: service),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(
      find.byType(PreliminaryEstimateCard),
      250,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('120.000 đ'), findsOneWidget);
    expect(find.text('230.000 đ'), findsOneWidget);
    expect(find.text('350.000 đ'), findsOneWidget);
    expect(find.textContaining('Chi phí tham khảo'), findsOneWidget);
    expect(find.text('Duyệt'), findsNothing);
    expect(find.text('Từ chối'), findsNothing);
    total = 410000;
    final refresh = tester.widget<RefreshIndicator>(
      find.byType(RefreshIndicator),
    );
    final refreshed = refresh.onRefresh();
    await tester.pumpAndSettle();
    await refreshed;
    expect(find.text('410.000 đ'), findsOneWidget);
    expect(requests.length, 2);
    for (final request in requests) {
      expect(request.method, 'GET');
      expect(request.path, '/appointments/16');
      expect(request.headers['Authorization'], 'Bearer test-token');
      expect(request.queryParameters, isEmpty);
      expect(request.data, isNull);
    }
    await tester.pumpWidget(const SizedBox());
  });

  for (final status in [401, 403, 404]) {
    test('propagates API $status without manufacturing an estimate', () async {
      FlutterSecureStorage.setMockInitialValues({});
      final dio = Dio(BaseOptions(baseUrl: 'http://localhost/api'));
      addTearDown(dio.close);
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (request, handler) {
            handler.reject(
              DioException(
                requestOptions: request,
                response: Response(requestOptions: request, statusCode: status),
                type: DioExceptionType.badResponse,
              ),
            );
          },
        ),
      );
      final service = AppointmentService(
        ApiClient(dio, const SecureSessionStorage(FlutterSecureStorage())),
      );
      await expectLater(
        service.loadAppointment(16),
        throwsA(isA<AppointmentException>()),
      );
    });
  }

  testWidgets('missing estimate is not shown as free; zero remains valid', (
    tester,
  ) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: PreliminaryEstimateCard(labor: null, parts: 0, total: 350000.5),
        ),
      ),
    );
    expect(find.text('Chưa có dự toán'), findsOneWidget);
    expect(find.text('0 đ'), findsOneWidget);
    expect(find.text('350.000,50 đ'), findsOneWidget);
  });

  testWidgets('estimate card fits a narrow screen with large text', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(320, 1000);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(
      MaterialApp(
        builder: (context, child) => MediaQuery(
          data: MediaQuery.of(
            context,
          ).copyWith(textScaler: const TextScaler.linear(2)),
          child: child!,
        ),
        home: const Scaffold(
          body: SingleChildScrollView(
            padding: EdgeInsets.all(20),
            child: PreliminaryEstimateCard(
              labor: 120000,
              parts: 230000,
              total: 350000,
            ),
          ),
        ),
      ),
    );
    expect(tester.takeException(), isNull);
    expect(find.text('350.000 đ'), findsOneWidget);
  });
}
