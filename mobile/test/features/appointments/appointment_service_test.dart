import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/appointments/data/appointment_service.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late Dio dio;
  late AppointmentService service;
  late List<RequestOptions> requests;

  setUp(() {
    FlutterSecureStorage.setMockInitialValues({'access_token': 'test-token'});
    dio = Dio(BaseOptions(baseUrl: 'http://localhost/api'));
    requests = [];
    const storage = SecureSessionStorage(FlutterSecureStorage());
    service = AppointmentService(ApiClient(dio, storage));
  });

  test('tải xe thuộc customer và chi nhánh qua API tập trung', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          requests.add(options);
          final data = switch (options.path) {
            '/vehicles' => {
              'success': true,
              'data': [
                {
                  'maXe': 7,
                  'bienSo': '51A-11111',
                  'hangXe': 'Toyota',
                  'model': 'Camry',
                },
                {
                  'maXe': 8,
                  'bienSo': '51A-22222',
                  'hangXe': 'Honda',
                  'model': 'Civic',
                  'trangThai': false,
                },
              ],
            },
            '/branches' => {
              'success': true,
              'data': [
                {
                  'maChiNhanh': 2,
                  'tenChiNhanh': 'Chi nhánh Bình Thạnh',
                  'diaChi': 'Bình Thạnh, TP.HCM',
                },
              ],
            },
            _ => <String, dynamic>{},
          };
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 200,
              data: data,
            ),
          );
        },
      ),
    );

    final options = await service.loadBookingOptions();

    expect(requests.map((request) => request.path), {'/vehicles', '/branches'});
    expect(options.vehicles.single.licensePlate, '51A-11111');
    expect(options.branches.single.id, 2);
  });

  test('payload tạo lịch không gửi mã khách hàng từ client', () async {
    Map<String, dynamic>? submittedData;
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          submittedData = Map<String, dynamic>.from(options.data as Map);
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 201,
              data: {
                'success': true,
                'data': {
                  'maDatLich': 15,
                  'trangThai': 'CHO_XAC_NHAN',
                  'thoiGianHen': '2026-09-10T08:30:00',
                  'bienSoXe': '51A-11111',
                  'tenChiNhanh': 'Garage Trung tâm',
                },
              },
            ),
          );
        },
      ),
    );

    final result = await service.createAppointment(
      vehicleId: 7,
      branchId: 2,
      appointmentTime: DateTime(2026, 9, 10, 8, 30),
      note: '  Bảo dưỡng định kỳ  ',
    );

    expect(submittedData, isNot(contains('maKhachHang')));
    expect(submittedData?['maXe'], 7);
    expect(submittedData?['maChiNhanh'], 2);
    expect(submittedData?['ghiChu'], 'Bảo dưỡng định kỳ');
    expect(result.id, 15);
  });

  test('tải lịch của customer và ánh xạ quyền hủy theo trạng thái', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          expect(options.path, '/appointments');
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 200,
              data: {
                'success': true,
                'data': [
                  {
                    'maDatLich': 16,
                    'trangThai': 'DA_TIEP_NHAN',
                    'thoiGianHen': '2026-09-09T09:00:00',
                    'bienSoXe': '51A-11111',
                    'hangXe': 'Toyota',
                    'modelXe': 'Camry',
                    'tenChiNhanh': 'Garage Trung tâm',
                    'ghiChu': 'Kiểm tra phanh',
                  },
                  {
                    'maDatLich': 15,
                    'trangThai': 'CHO_XAC_NHAN',
                    'thoiGianHen': '2026-09-10T08:30:00',
                    'bienSoXe': '51A-11111',
                    'tenChiNhanh': 'Garage Trung tâm',
                  },
                ],
              },
            ),
          );
        },
      ),
    );

    final appointments = await service.loadMyAppointments();

    expect(appointments.first.id, 15);
    expect(appointments.first.canCancel, isTrue);
    expect(appointments.last.canCancel, isFalse);
    expect(appointments.last.vehicleDescription, 'Toyota Camry');
  });

  test('tải chi tiết lịch hẹn bằng endpoint ownership backend', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          expect(options.method, 'GET');
          expect(options.path, '/appointments/16');
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 200,
              data: {
                'success': true,
                'data': {
                  'maDatLich': 16,
                  'trangThai': 'DA_TIEP_NHAN',
                  'thoiGianHen': '2026-09-09T09:00:00',
                  'bienSoXe': '51A-11111',
                  'hangXe': 'Toyota',
                  'modelXe': 'Camry',
                  'tenChiNhanh': 'Garage Trung tâm',
                },
              },
            ),
          );
        },
      ),
    );

    final appointment = await service.loadAppointment(16);

    expect(appointment.id, 16);
    expect(appointment.status, 'DA_TIEP_NHAN');
    expect(appointment.progressStep, 2);
  });

  test(
    'hủy lịch bằng endpoint hiện có và nhận trạng thái từ backend',
    () async {
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (options, handler) {
            expect(options.method, 'PATCH');
            expect(options.path, '/appointments/15/cancel');
            handler.resolve(
              Response<Map<String, dynamic>>(
                requestOptions: options,
                statusCode: 200,
                data: {
                  'success': true,
                  'data': {
                    'maDatLich': 15,
                    'trangThai': 'HUY',
                    'thoiGianHen': '2026-09-10T08:30:00',
                    'bienSoXe': '51A-11111',
                    'tenChiNhanh': 'Garage Trung tâm',
                  },
                },
              ),
            );
          },
        ),
      );

      final result = await service.cancelAppointment(15);

      expect(result.status, 'HUY');
      expect(result.canCancel, isFalse);
    },
  );

  test('ưu tiên thông báo lỗi nghiệp vụ từ backend', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          handler.reject(
            DioException(
              requestOptions: options,
              response: Response<Map<String, dynamic>>(
                requestOptions: options,
                statusCode: 409,
                data: {
                  'success': false,
                  'message': 'Xe đã có lịch hẹn vào thời điểm này',
                },
              ),
            ),
          );
        },
      ),
    );

    await expectLater(
      service.createAppointment(
        vehicleId: 7,
        branchId: 2,
        appointmentTime: DateTime.now().add(const Duration(days: 1)),
      ),
      throwsA(
        isA<AppointmentException>().having(
          (error) => error.message,
          'message',
          'Xe đã có lịch hẹn vào thời điểm này',
        ),
      ),
    );
  });
}
