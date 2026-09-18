import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/technician/data/technician_service.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late Dio dio;
  late TechnicianService service;

  setUp(() {
    FlutterSecureStorage.setMockInitialValues({'access_token': 'test-token'});
    dio = Dio(BaseOptions(baseUrl: 'http://localhost/api'));
    const storage = SecureSessionStorage(FlutterSecureStorage());
    service = TechnicianService(ApiClient(dio, storage));
  });

  test('tải và ưu tiên công việc đang sửa', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          expect(options.method, 'GET');
          expect(options.path, '/technician/repair-orders');
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 200,
              data: {
                'success': true,
                'data': [
                  {
                    'maPhieuSuaChua': 10,
                    'trangThai': 'HOAN_TAT',
                    'bienSoXe': '51A-10000',
                  },
                  {
                    'maPhieuSuaChua': 11,
                    'trangThai': 'DANG_SUA',
                    'bienSoXe': '51A-11000',
                    'tenHangXe': 'Toyota',
                    'tenModel': 'Camry',
                  },
                ],
              },
            ),
          );
        },
      ),
    );

    final orders = await service.loadRepairOrders();

    expect(orders.first.id, 11);
    expect(orders.first.vehicleDescription, 'Toyota Camry');
    expect(orders.last.isLocked, isTrue);
  });

  test('tải chi tiết qua ba endpoint assigned-only', () async {
    final requestedPaths = <String>[];
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          requestedPaths.add(options.path);
          final data = switch (options.path) {
            '/technician/repair-orders/11' => {
              'maPhieuSuaChua': 11,
              'trangThai': 'DANG_SUA',
              'bienSoXe': '51A-11000',
              'tenKhachHang': 'Nguyễn Văn A',
            },
            '/technician/repair-orders/11/items' => [
              {
                'maChiTiet': 3,
                'maPhieuSuaChua': 11,
                'tenDichVu': 'Kiểm tra phanh',
                'soLuong': 1,
                'thanhTien': 250000,
                'trangThai': 'CHO_XU_LY',
              },
            ],
            '/technician/repair-orders/11/progress-history' => [
              {
                'trangThai': 'DANG_SUA',
                'phanTramHoanThanh': 50,
                'moTa': 'Đang kiểm tra',
              },
            ],
            _ => null,
          };
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 200,
              data: {'success': true, 'data': data},
            ),
          );
        },
      ),
    );

    final detail = await service.loadRepairDetail(11);

    expect(requestedPaths, hasLength(3));
    expect(detail.order.customerName, 'Nguyễn Văn A');
    expect(detail.items.single.name, 'Kiểm tra phanh');
    expect(detail.progressHistory.single.percent, 50);
  });

  test(
    'payload cập nhật tiến độ không gửi kỹ thuật viên hoặc chi nhánh',
    () async {
      Map<String, dynamic>? submittedData;
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (options, handler) {
            expect(options.method, 'PATCH');
            expect(options.path, '/technician/repair-orders/11/progress');
            submittedData = Map<String, dynamic>.from(options.data as Map);
            handler.resolve(
              Response<Map<String, dynamic>>(
                requestOptions: options,
                statusCode: 200,
                data: {
                  'success': true,
                  'data': {
                    'trangThai': 'DANG_SUA',
                    'phanTramHoanThanh': 60,
                    'moTa': 'Đã kiểm tra hệ thống phanh',
                  },
                },
              ),
            );
          },
        ),
      );

      final progress = await service.updateProgress(
        repairOrderId: 11,
        status: 'DANG_SUA',
        percent: 60,
        description: '  Đã kiểm tra hệ thống phanh  ',
      );

      expect(submittedData, isNot(contains('maNhanVien')));
      expect(submittedData, isNot(contains('maChiNhanh')));
      expect(submittedData?['moTa'], 'Đã kiểm tra hệ thống phanh');
      expect(progress.percent, 60);
    },
  );

  test('cập nhật trạng thái hạng mục theo endpoint kỹ thuật viên', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          expect(options.path, '/technician/repair-orders/11/items/3');
          expect(options.data, {'trangThai': 'HOAN_TAT'});
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 200,
              data: {
                'success': true,
                'data': {
                  'maChiTiet': 3,
                  'maPhieuSuaChua': 11,
                  'tenDichVu': 'Kiểm tra phanh',
                  'trangThai': 'HOAN_TAT',
                },
              },
            ),
          );
        },
      ),
    );

    final item = await service.updateItemStatus(
      repairOrderId: 11,
      itemId: 3,
      status: 'HOAN_TAT',
    );

    expect(item.status, 'HOAN_TAT');
  });
}
